package neural2d;

import java.util.ArrayList;
import java.util.HashMap;
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
        biasLayer = new BiasLayer(this, biasConfig);
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
        layers.add(InputLayer.createLayer(this, inputConfig));

        // Create neurons and connect them:
        totalNumberNeurons = inputConfig.getNumRows() * inputConfig.getNumColumns();

        // Create other layers.
        for (LayerConfig hiddenConfig : topology.getHiddenLayerConfig()) {
            Neural2D.LOG.fine("Creating layer '" + hiddenConfig.getLayerName() + "'.");
            // Create layer and add to list.
            newLayer = HiddenLayer.createLayer(this, hiddenConfig);
            layers.add(newLayer);
            connectToLayers(newLayer, hiddenConfig);
            totalNumberNeurons += hiddenConfig.getNumRows() * hiddenConfig.getNumColumns();
        }

        layerConfig = outputConfig;
        Neural2D.LOG.fine("Creating output layer '" + layerConfig.getLayerName() + "'.");
        newLayer = OutputLayer.createLayer(this, layerConfig);
        layers.add(newLayer);
        connectToLayers(newLayer, layerConfig);
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

    private void connectToLayers(Layer newLayer, LayerConfig hiddenConfig)
    {
        Layer fromLayer;
        // Create the neurons of this layer and connect
        // them to their from layer(s). From layer should
        // already exist, because the layerConfig lists
        // are returned in the proper partial ordering.
        for (LayerConfig fromConfig : hiddenConfig.getFromLayerConfigs()) {
            fromLayer = findLayerByName(fromConfig.getLayerName());
            totalNumberConnections += fromLayer.connectTo(newLayer);
        }
        if (newLayer.acceptsBias()) {
            totalNumberConnections +=biasLayer.connectTo(newLayer);
        }

    }

    // Returns a random double in the range [0.0..1.0]
    //
    double randomDouble()
    {
        return (rand.nextInt(1073741824) / 1073741823.0);
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
