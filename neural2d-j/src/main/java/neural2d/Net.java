package neural2d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.logging.Level;
import neural2d.Command.JoinableResult;
import neural2d.NetElementVisitor.Direction;
import neural2d.config.ConfigurationException;
import neural2d.config.LayerConfig;
import neural2d.config.NetConfig;
import neural2d.config.TopologyConfig;
import neural2d.config.WeightsConfig;
import neural2d.display.OutputFunctionDisplay;

/**
 * Copyright (c) 2015 Michael C. Whidden
 *
 * @author Michael C. Whidden
 */
public class Net implements NetElement
{

    private Trainer trainingData;

    private List<Layer> layers;
    private Layer biasLayer; // fake layer holding the bias neuron

    private long lastReportTime = 0L;

    private int totalNumberConnections; // Including 1 bias connection per neuron
    private int totalNumberNeurons; // except bias neuron
    private final Random rand;
    private final ForkJoinPool pool = new ForkJoinPool();
    private Map<Neuron, Set<Neuron>> sourceNeurons = new HashMap<>();

    public Net(NetConfig config, long randSeed) throws ConfigurationException
    {
        layers = new ArrayList<>();
        totalNumberConnections = 0;
        totalNumberNeurons = 0;
        trainingData = new Trainer(config.getTrainingConfig());
        rand = new Random(randSeed);

        // Initialize the dummy bias neuron to provide a weighted bias input for all other neurons.
        // This is a single special neuron that has no inputs of its own, and feeds a constant
        // 1.0 through weighted connections to every other neuron in the network except input
        // neurons:
        LayerConfig biasConfig = new LayerConfig();
        biasConfig.setSize(1, 1);
        biasConfig.setLayerName("$$bias");
        biasConfig.setTransferFunction(TransferFunction.IDENTITY);
        biasLayer = new BiasLayer(biasConfig);
        layers.add(biasLayer);
        biasLayer.createNeurons();

        // Set up the layers, create neurons, and connect them:
        configure(config);  // Throws an exception if any error
    }

    // Creates layer metadata from a config file
    // Returns true if the neural net was successfully created and connected. Returns
    // false for any error. See the GitHub wiki (https://github.com/davidrmiller/neural2d)
    // for more information about the format of the topology config file.
    // Throws an exception for any error.
    //
    private void configure(NetConfig config) throws ConfigurationException
    {
        TopologyConfig topology = config.getTopologyConfig();
        LayerConfig inputConfig, outputConfig, layerConfig;
        Layer newLayer;
        // Create the input layer
        inputConfig = topology.getInputLayerConfig();
        outputConfig = topology.getOutputLayerConfig();

        // To do: Add range check for sizeX, sizeY, radiusX, radiusY
        // Create input layer, and add to layers list.
        Neural2D.LOG.fine("Creating input layer '" + inputConfig.getLayerName() + "'.");
        layers.add(InputLayer.createLayer(inputConfig));

        // Create neurons and connect them:
        totalNumberNeurons = inputConfig.getNumRows() * inputConfig.getNumColumns();

        // Create other layers.
        for (LayerConfig hiddenConfig : topology.getHiddenLayerConfig()) {
            Neural2D.LOG.fine("Creating layer '" + hiddenConfig.getLayerName() + "'.");
            // Create layer and add to list.
            newLayer = HiddenLayer.createLayer(hiddenConfig);
            layers.add(newLayer);
            connectLayer(newLayer, hiddenConfig);
            totalNumberNeurons += hiddenConfig.getNumRows() * hiddenConfig.getNumColumns();
        }

        layerConfig = outputConfig;
        Neural2D.LOG.fine("Creating output layer '" + layerConfig.getLayerName() + "'.");
        newLayer = OutputLayer.createLayer(layerConfig);
        connectLayer(newLayer, layerConfig);
        totalNumberNeurons += layerConfig.getNumRows() * layerConfig.getNumColumns();

        // It's possible that some internal neurons don't feed any other neurons.
        // That's not a fatal error, but it's probably due to an unintentional mistake
        // in defining the net topology. Here we will find and report all neurons with
        // no forward connections so that the human can fix the topology configuration
        // if needed:
        Neural2D.LOG.fine("Checking for neurons with no sinks:");

        // Loop through all layers except the output layer, looking for unconnected neurons:
        int neuronsWithNoSink = 0;
        for (int lidx = 0; lidx < layers.size() - 1; ++lidx) {
            Layer layer = layers.get(lidx);
            for (Neuron neuron : layer.getNeurons()) {
                if (!neuron.hasForwardConnections()) {
                    ++neuronsWithNoSink;
                    Neural2D.LOG.warning("  neuron(" + neuron + ") on " + layer.getName()
                            + " has no forward connections.");
                }
            }
        }

        Neural2D.LOG.info("Found " + neuronsWithNoSink + " neurons with no sink.");
        Neural2D.LOG.info(totalNumberNeurons + " neurons total; " + totalNumberConnections + " back+bias connections.");
        Neural2D.LOG.info("About " + (int) ((float) totalNumberConnections / totalNumberNeurons + 0.5)
                + " connections per neuron on average.");
        if (config.isTrained()) {
            Neural2D.LOG.info("Network is trained. Loading weights.");
            accept(new LoadWeightConfigVisitor(config.getWeightsConfig()));
        }
        // Optionally enable the next line to display the resulting net topology:
        debugShowNet(true);
    }

    private void connectLayer(Layer newLayer, LayerConfig hiddenConfig)
    {
        Layer fromLayer;
        layers.add(newLayer);
        // Create the neurons of this layer and connect
        // them to their from layer(s). From layer should
        // already exist, because the layerConfig lists
        // are returned in the proper partial ordering.
        for (LayerConfig fromConfig : hiddenConfig.getFromLayerConfigs()) {
            fromLayer = findLayerByName(fromConfig.getLayerName());

            connectNeurons(fromLayer, newLayer);
        }
        if (!newLayer.isConvolutionLayer()) {
            connectNeurons(biasLayer, newLayer);
        }

    }

    private void connectNeurons(Layer fromLayer, Layer toLayer)
    {
        // If layerFrom is layerTo, it means we're making input neurons
        // that have no input connections to the neurons. Else, we must make connections
        // to the source neurons and, for classic neurons, to a bias input:

        for (int row = 0; row < toLayer.getNumRows(); ++row) {
            for (int col = 0; col < toLayer.getNumColumns(); ++col) {
                Neuron neuron = toLayer.getNeuron(row, col);
                if (toLayer.getLayerType() != LayerType.INPUT) {
                    if (fromLayer.getLayerType() == LayerType.BIAS) {
                        connectBias(neuron);
                    } else {
                        connectNeuron(toLayer, fromLayer, neuron,
                                row, col);
                    }
                }
            }
        }
    }

    // Assuming an ellipse centered at 0,0 and aligned with the global axes, returns
    // a positive value if x,y is outside the ellipse; 0.0 if on the ellipse;
    // negative if inside the ellipse.
    //
    private double elliptDist(double x, double y, double radiusX, double radiusY)
    {
        assert (radiusX >= 0.0 && radiusY >= 0.0);
        return radiusY * radiusY * x * x + radiusX * radiusX * y * y - radiusX * radiusX * radiusY * radiusY;
    }

    // This creates the initial set of connections for a layer of neurons. (If the same layer
    // appears again in the topology config file, those additional connections must be added
    // to existing connections by calling addToLayer() instead of this function.
    //
    // TODO: implement the 'addToLayer'-type feature to allow multiple layers to
    // feed to a single forward layer.
    //
    // Neurons can be "regular" neurons, or convolution nodes. If a convolution matrix is
    // defined for the layer, the neurons in that layer will be connected to source neurons
    // in a rectangular pattern defined by the matrix dimensions. No bias connections are
    // created for convolution nodes. Convolution nodes ignore any radius parameter.
    // For convolution nodes, the transfer function is set to be the identity function.
    //
    // For regular neurons,
    // the location of the destination neuron is projected onto the neurons of the source
    // layer. A shape is defined by the radius parameters, and is considered to be either
    // rectangular or elliptical (depending on the value of projectRectangular below).
    // A radius of 0,0 connects a single source neuron in the source layer to this
    // destination neuron. E.g., a radius of 1,1, if projectRectangular is true, connects
    // nine neurons from the source layer in a 3x3 block to this destination neuron.
    //
    // Each Neuron object holds a container of Connection objects for all the source
    // inputs to the neuron. Each neuron also holds a container of pointers to Connection
    // objects in the forward direction. Those points point to Container objects in
    // and owned by neurons in other layers. I.e., the master copies of all connection are
    // in the containers of back connections; forward connection pointers refer to
    // back connection records in other neurons.
    //
    private void connectNeuron(Layer layerTo, Layer layerFrom, Neuron neuron,
            int nx, int ny)
    {
        int sizeX = layerTo.getNumColumns();
        int sizeY = layerTo.getNumRows();
        assert (sizeX > 0 && sizeY > 0);

        // Calculate the normalized [0..1] coordinates of our neuron:
        double normalizedX = ((float) nx / sizeX) + (1.0 / (2 * sizeX));
        double normalizedY = ((float) ny / sizeY) + (1.0 / (2 * sizeY));

        // Calculate the coords of the nearest neuron in the "from" layer.
        // The calculated coords are relative to the "from" layer:
        int lfromX = (int) (normalizedX * layerFrom.getNumColumns()); // should we round off instead of round down?
        int lfromY = (int) (normalizedY * layerFrom.getNumRows());

        // Calculate the rectangular window into the "from" layer:
        int xmin, xmax;
        int ymin, ymax;

        if (layerTo.isConvolutionLayer()) {
            //ymin = lfromY - params.convolveMatrix.get(0).size() / 2;
            //ymax = ymin + params.convolveMatrix.get(0).size() - 1;
            //xmin = lfromX - params.convolveMatrix.size() / 2;
            //xmax = xmin + params.convolveMatrix.size() - 1;
            throw new UnsupportedOperationException("TODO");
        } else {
            xmin = lfromX - layerTo.getRadiusX();
            xmax = lfromX + layerTo.getRadiusX();
            ymin = lfromY - layerTo.getRadiusY();
            ymax = lfromY + layerTo.getRadiusY();
        }

        // Clip to the layer boundaries:
        xmin = clip(xmin, 0, layerFrom.getNumColumns() - 1);
        ymin = clip(ymin, 0, layerFrom.getNumRows() - 1);
        xmax = clip(xmax, 0, layerFrom.getNumColumns() - 1);
        ymax = clip(ymax, 0, layerFrom.getNumRows() - 1);

        // Now (xmin,xmax,ymin,ymax) defines a rectangular subset of neurons in a previous layer.
        // We'll make a connection from each of those neurons in the previous layer to our
        // neuron in the current layer.
        // We will also check for and avoid duplicate connections. Duplicates are mostly harmless,
        // but unnecessary. Duplicate connections can be formed when the same layer name appears
        // more than once in the topology config file with the same "from" layer if the projected
        // rectangular or elliptical areas on the source layer overlap.
        double xcenter = (xmin + xmax) / 2.0;
        double ycenter = (ymin + ymax) / 2.0;
        int maxNumSourceNeurons = ((xmax - xmin) + 1) * ((ymax - ymin) + 1);

        if (!sourceNeurons.containsKey(neuron)) {
            sourceNeurons.put(neuron, new HashSet<Neuron>());
        }
        for (int y = ymin; y <= ymax; ++y) {
            for (int x = xmin; x <= xmax; ++x) {
                if (!layerTo.isConvolutionLayer() && !layerTo.isRectangular() && elliptDist(xcenter - x, ycenter - y,
                        layerTo.getRadiusX(), layerTo.getRadiusY()) >= 1.0) {
                    continue; // Skip this location, it's outside the ellipse
                }

                if (layerTo.isConvolutionLayer() && layerTo.getConvolveMatrix().get(x - xmin, y - ymin) == 0.0) {
                    // Skip this connection because the convolve matrix weight is zero:
                    continue;
                }
                Neuron fromNeuron = layerFrom.getNeuron(x, y);

                if (sourceNeurons.get(neuron).contains(fromNeuron)) {
                    Neural2D.LOG.finer("Skipping a dupe connection from " + fromNeuron + " to " + neuron);
                    break; // Skip this connection, proceed to the next
                } else {
                    // Add a new Connection record to the main container of connections:
                    Connection conn = new Connection(fromNeuron, neuron);
                    fromNeuron.addForwardConnection(conn);
                    neuron.addBackConnection(conn);
                    //connections.add(conn);
                    ++totalNumberConnections;

                    // Initialize the weight of the connection:
                    if (layerTo.isConvolutionLayer()) {
                        conn.setWeight(layerTo.getConvolveMatrix().get(x - xmin, y - ymin));
                    } else {
                        //connections.back().weight = (randomDouble() - 0.5) / maxNumSourceNeurons;
                        conn.setWeight(((randomDouble() * 2.0) - 1.0) / Math.sqrt(maxNumSourceNeurons));
                    }

                    // Remember the source neuron for detecting duplicate connections:
                    sourceNeurons.get(neuron).add(fromNeuron);
                }
            }
        }
    }

    // Returns a random double in the range [0.0..1.0]
    //
    private double randomDouble()
    {
        return (rand.nextInt(1073741824) / 1073741823.0);
    }

    // Add a weighted bias input, modeled as a back-connection to a fake neuron:
    //
    private void connectBias(Neuron neuron)
    {
        Neuron biasNeuron = biasLayer.getNeuron(0, 0);
        // Create a new Connection record and get its index:
        Connection c = new Connection(biasNeuron, neuron);
        // connections.add(c);
        // int connectionIdx = connections.size() - 1;

        c.setWeight(randomDouble() - 0.5); // Review this !!!
        c.setDeltaWeight(0.0);

        // Record the back connection with the destination neuron:
        neuron.addBackConnection(c);

        ++totalNumberConnections;

        // Record the forward connection with the fake bias neuron:
        biasNeuron.addForwardConnection(c);
    }

    /**
     * This feeds the sample through the neural net and return the values from
     * the output layer.
     *
     * @param sample
     * @return
     * @throws neural2d.Neural2DException
     */
    public Matrix compute(Sample sample) throws Neural2DException
    {
        _feedForward(sample);

        return layers.get(layers.size() - 1).getOutput();
    }

    /**
     * This feeds the input through the neural net and return the values from
     * the output layer.
     *
     * @param input
     * @return
     * @throws neural2d.Neural2DException
     */
    public Matrix compute(Matrix input) throws Neural2DException
    {
        Sample s = Sample.createSample(input, null);
        return compute(s);
    }

    private void _feedForward(Sample sample) throws Neural2DException
    {

        // Move the input data from sample to the input neurons. We'll also
        // check that the number of components of the input sample equals
        // the number of input neurons:
        Layer inputLayer = layers.get(1);

        // Start the forward propagation at the input layer:
        if (Neural2D.LOG.isLoggable(Level.FINEST)) {
            Neural2D.LOG.finest("Beginning feed forward");
        }

        accept(new FeedForwardVisitor(sample.getData(inputLayer.getChannel())));
        for (int layerIdx = 1; layerIdx < layers.size(); ++layerIdx) {
            Layer layer = layers.get(layerIdx);
            if (Neural2D.LOG.isLoggable(Level.FINEST)) {
                Neural2D.LOG.finest("Feed forward on layer " + layer.getName());
            }
            layer.executeCommand(layer.getFeedForwardCommand(sample.getData(inputLayer.getChannel())));
        }
    }

    public void train(SampleSet samples, OutputFunctionDisplay disp) throws Neural2DException
    {
        trainingData.train(this, samples, rand, disp);
    }

    public void run(SampleSet samples) throws Neural2DException
    {
        run(samples, false);
    }

    public boolean validate(SampleSet samples) throws Neural2DException
    {
        return run(samples, true);
    }

    private boolean run(SampleSet sampleSet, boolean validate) throws Neural2DException
    {
        for (int sampleIdx = 0; sampleIdx < sampleSet.getSamples().size(); ++sampleIdx) {
            Sample sample = sampleSet.getSamples().get(sampleIdx);
            int[] inputSize = getInputSize();
            if (sample.getData().getNumRows()
                    != inputSize[0]
                    || sample.getData().getNumColumns() != inputSize[1]) {
                throw new Net.SampleException("Sample "
                        + (sampleIdx + 1) + " size does not match the size of "
                        + "the input layer (" + inputSize[0] + "x"
                        + inputSize[1] + ")");
            }
            Matrix output = compute(sample);
            reportResults(sample, null);
            if (validate) {
                return trainingData.validate(output, sample.getTargetVals());
            }
        }
        return true;
    }

    // for displaying the results when processing input samples
    // Assumes the net's output neuron errors and overall net error have already been
    // computed and saved in the case where the target output values are known.
    //
    void reportResults(Sample sample, OutputFunctionDisplay disp) throws Neural2DException
    {
        StringBuilder result = new StringBuilder();
        long time = System.currentTimeMillis() - lastReportTime;
        // We actually report only every Nth input sample:
        Layer lastLayer = layers.get(layers.size() - 1);
        if (disp != null) {
            disp.recalculate();
        }
        // Report actual and expected outputs:

        result.append("\nOutputs: ");
        for (Neuron n : lastLayer.getNeurons()) { // For all neurons in output layer
            result.append(n.getOutput()).append(" ");
        }
        result.append("\n");

        if (sample.getTargetVals() != null) {
            result.append("Expected: ");
            result.append(sample.getTargetVals().toString());
            result.append("\n");
            // Optional: when the net's
            // outputs are a classifier, where the output neuron with the largest output
            // value indicates which class was recognized. This can be used, e.g., for pattern
            // recognition where each output neuron corresponds to one pattern class,
            // and the output neurons are trained to be high to indicate a pattern match,
            // and low to indicate no match.

            if (lastLayer.isClassifier()) {
                MaxRowCol max = lastLayer.executeCommand(new MaxNeuronCommand());
                if (sample.getTargetVal(max.row, max.col) > 0.0) {
                    result.append(" Correct");
                } else {
                    result.append(" Wrong");
                }
                result.append("\n");
            }

            result.append(trainingData.getTrainingStatus());
            if (lastReportTime > 0) {
                result.append("Pass time: ").append(time).append("ms.\n ");
            }
        }
        Neural2D.LOG.info(result.toString());
        lastReportTime = System.currentTimeMillis();
    }

    public int[] getInputSize()
    {
        return new int[]{layers.get(1).getNumRows(),
            layers.get(1).getNumColumns()};
    }

    private Layer findLayerByName(String layerName)
    {
        for (Layer layer : layers) {
            if (layer.getName().equals(layerName)) {
                return layer;
            }
        }
        return null;
    }

    public int getNumConnections()
    {
        return totalNumberConnections;
    }

    public int getNumNeurons()
    {
        return totalNumberNeurons;
    }

    private int clip(int val, int min, int max)
    {
        val = Math.max(min, val);
        val = Math.min(max, val);
        return val;
    }

    private static class DebugVisitor extends NetElementVisitor
    {

        int neuronFwdConns, neuronBackConns;
        final boolean details;
        StringBuilder result = new StringBuilder();

        public DebugVisitor(boolean details)
        {
            this.details = details;
        }

        @Override
        public boolean visit(Net net)
        {
            result.append("Net configuration (incl. bias connection): --------------------------\n");
            return true;
        }

        @Override
        public boolean visit(Neuron n)
        {
            if (details) {
                result.append("  ").append(n).append(" output: ").append(n.getOutput()).append("\n");
            }

            neuronFwdConns += n.getNumForwardConnections();
            neuronBackConns += n.getNumBackConnections(); // Includes the bias connection

            return true;
        }

        @Override
        public boolean visit(Connection c)
        {
            if (details) {
                result.append("        ").append(c).append("\n");
            }
            return false;
        }

        @Override
        public boolean visit(Layer l)
        {
            neuronFwdConns = 0;
            neuronBackConns = 0;
            result.append("Layer '").append(l.getName()).append("' has ").append(l.size()).append(" neurons arranged in ").append(l.getNumRows()).append("x").append(l.getNumColumns()).append(":\n");
            if (!details) {
                result.append("   connections: ").append(l.getNumBackConnections()).append(" back, ").append(l.getNumFwdConnections()).append(" forward.\n");
            }
            return true;
        }

    }

    // for displaying the results when processing input samples
    // This is an optional way to display lots of information about the network
    // topology. Tweak as needed. The argument 'details' can be used to control
    // if all the connections are displayed in detail.
    void debugShowNet(boolean details)
    {
        DebugVisitor v = new DebugVisitor(details);
        accept(v);
        Neural2D.LOG.info(v.result.toString());
    }

    @Override
    public void accept(NetElementVisitor v)
    {
        if (v.visit(this)) {
            if (v.getDirection() == Direction.FORWARD) {
                for (Layer layer : layers) {
                    layer.accept(v);
                }
            } else {
                for (int i = layers.size() - 1; i >= 0; i--) {
                    layers.get(i).accept(v);
                }
            }
        }
    }

    public static class LayerException extends Neural2DException
    {

        public LayerException(String string)
        {
            super(string);
        }
    }

    public static class SampleException extends Neural2DException
    {

        public SampleException(String string)
        {
            super(string);
        }
    }

    public static class FileFormatException extends Neural2DException
    {

        public FileFormatException(String string)
        {
            super(string);
        }
    }

    /**
     * If the Command is parallelizable, the Commands may be executed, once per
     * Layer, in any order, in parallel. Otherwise, the Commands will be
     * executed, once per Layer, in any order, serially.
     *
     * @param <T>
     * @param action
     * @return
     */
    public <T> T executeCommand(Command<Layer, T> action)
    {
        NetTask<T> nAction = new NetTask<>(action);
        return pool.invoke(nAction).getResult();
    }

    private static class FeedForwardVisitor extends NetElementVisitor
    {

        private final Matrix inputs;

        public FeedForwardVisitor(Matrix inputs)
        {
            this.inputs = inputs;
        }

        @Override
        public boolean visit(Layer layer)
        {
            if (Neural2D.LOG.isLoggable(Level.FINEST)) {
                Neural2D.LOG.finest("Feed forward on layer " + layer.getName());
            }
            // Get the proper action to take on this layer and
            // do it, in parallel, on all the neurons on the layer.
            layer.executeCommand(layer.getFeedForwardCommand(inputs));

            // no need to visit neurons
            return false;
        }
    }

    private static class LoadWeightConfigVisitor extends NetElementVisitor
    {

        private final WeightsConfig cfg;

        public LoadWeightConfigVisitor(WeightsConfig cfg)
        {
            this.cfg = cfg;
        }

        @Override
        public boolean visit(Connection conn)
        {
            Neuron fromNeuron, toNeuron;
            fromNeuron = conn.getFromNeuron();
            toNeuron = conn.getToNeuron();

            conn.setWeight(cfg.getWeight(fromNeuron.getLayer().getName(),
                    fromNeuron.getRow(),
                    fromNeuron.getColumn(),
                    toNeuron.getLayer().getName(),
                    toNeuron.getRow(),
                    toNeuron.getColumn()));
            return false;
        }
    }

    private static class MaxRowCol
    {

        double max;
        int row, col;
    }

    private static class MaxRowColResult implements JoinableResult<MaxRowCol>
    {

        private final MaxRowCol result;

        public MaxRowColResult(MaxRowCol m)
        {
            this.result = m;
        }

        @Override
        public void join(JoinableResult<MaxRowCol> o)
        {
            if (o != null) {
                MaxRowCol oMax = o.getResult();
                if (oMax.max > result.max) {
                    result.max = oMax.max;
                    result.row = oMax.row;
                    result.col = oMax.col;
                }
            }
        }

        @Override
        public MaxRowCol getResult()
        {
            return result;
        }

    }

    private static class MaxNeuronCommand implements Command<Neuron, MaxRowCol>
    {

        @Override
        public MaxRowColResult execute(Neuron n)
        {
            MaxRowCol m = new MaxRowCol();
            m.max = n.getOutput();
            m.col = n.getColumn();
            m.row = n.getRow();
            return new MaxRowColResult(m);
        }

        @Override
        public boolean canParallelize()
        {
            return false; // not parellizable
        }
    }

    private class NetTask<T> extends RecursiveTask<Command.JoinableResult<T>>
    {

        private static final long serialVersionUID = 0L;
        private final int start;
        private final int len;
        private final int numSplits;
        private final Command<Layer, T> action;

        public NetTask(Command<Layer, T> action)
        {
            this(action, 0, layers.size());
        }

        protected NetTask(Command<Layer, T> action, int start, int len)
        {
            this(action, start, len, 0);
        }

        protected NetTask(Command<Layer, T> action, int start, int len, int numSplits)
        {
            this.start = start;
            this.len = len;
            this.numSplits = numSplits;
            this.action = action;
        }

        private class NetTaskVisitor extends NetElementVisitor
        {

            JoinableResult<T> result;

            @Override
            public boolean visit(Layer n)
            {
                JoinableResult<T> res = action.execute(n);
                if (result == null) {
                    result = res;
                } else {
                    result.join(res);
                }
                return false;
            }
        }

        @Override
        protected Command.JoinableResult<T> compute()
        {
            if (!action.canParallelize() || len < 128 /* && numSplits < numProcessors */) {
                NetTaskVisitor v = new NetTaskVisitor();
                accept(v);
                return v.result;
            } else {
                NetTask<T> left, right;
                int split = len / 2;
                left = new NetTask<>(action, start, split, numSplits + 1);
                right = new NetTask<>(action, start + split, len - split, numSplits + 1);
                left.fork();
                Command.JoinableResult<T> result = right.compute();
                if (result == null) {
                    result = left.join();
                } else {
                    result.join(left.join());
                }
                return result;
            }
        }
    }
}
