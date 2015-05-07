package neural2d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import neural2d.Command.JoinableResult;
import neural2d.config.ConfigurationException;
import neural2d.config.LayerConfig;
import neural2d.config.NetConfig;
import neural2d.config.TopologyConfig;
import neural2d.config.TrainingParameters;
import neural2d.config.WeightsConfig;

/**
 * Copyright (c) 2015 Michael C. Whidden
 * @author Michael C. Whidden
 */
public class Net implements NetElement
{
    long lastReportTime = 0L;

    // To reduce screen clutter during training, reportEveryNth can be set > 1. When
    // in VALIDATE or TRAINED mode, you'll want to set this to 1 so you can see every
    // result:
    int reportEveryNth;

    int inputSampleNumber; // Increments each time feedForward() is called
    float error;                // Overall net error
    float recentAverageError;   // Averaged over recentAverageSmoothingFactor samples
    float eta;
    TrainingParameters trainingParams;
    SampleSet sampleSet;

    private BiasNeuron biasNeuron;  // Fake neuron with constant output 1.0
    private List<Layer> layers;
    private Layer biasLayer; // fake layer holding the bias neuron

    private float lastRecentAverageError;    // Used for dynamically adjusting eta
    private int totalNumberConnections; // Including 1 bias connection per neuron
    private int totalNumberNeurons;
    private final Random rand = new Random();
    private final ForkJoinPool pool = new ForkJoinPool();

    public Net(NetConfig config) throws ConfigurationException
    {
        reportEveryNth = 1;
        inputSampleNumber = 0;         // Increments each time feedForward() is called
        error = 1.0f;
        recentAverageError = 1.0f;
        //connections = new ArrayList<>();
        layers = new ArrayList<>();
        lastRecentAverageError = 1.0f;
        totalNumberConnections = 0;
        totalNumberNeurons = 0;
        sampleSet = new SampleSet();
        trainingParams = config.getTrainingParameters();

        // Initialize the dummy bias neuron to provide a weighted bias input for all other neurons.
        // This is a single special neuron that has no inputs of its own, and feeds a constant
        // 1.0 through weighted connections to every other neuron in the network except input
        // neurons:
        LayerConfig biasConfig = new LayerConfig();
        biasConfig.setSize(1,1);
        biasConfig.setLayerName("$$bias");
        biasConfig.setTransferFunction(TransferFunction.IDENTITY);
        biasLayer = new Layer.BiasLayer(biasConfig);
        layers.add(biasLayer);
        biasNeuron = new BiasNeuron(biasLayer);
        biasLayer.addNeuron(biasNeuron, 0, 0);

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
        TopologyConfig topology =config.getTopologyConfig();
        LayerConfig inputConfig, outputConfig, layerConfig;
        int numNeurons;
        Layer newLayer, inputLayer;
        // Create the input layer
        inputConfig = topology.getInputLayerConfig();
        outputConfig = topology.getOutputLayerConfig();

        // To do: Add range check for sizeX, sizeY, radiusX, radiusY

        // Create input layer, and add to layers list.
        inputLayer = new Layer.InputLayer(inputConfig);
        layers.add(inputLayer);

        // Create neurons and connect them:

        layerConfig = inputConfig;
        newLayer = inputLayer;

        System.out.println("Creating input layer" + layerConfig.getLayerName() + ".");
        createNeurons(newLayer, null); // Input layer has no back connections
        numNeurons = layerConfig.getNumRows() * layerConfig.getNumColumns();

        // Create other layers.
        int idx = 1;
        Layer fromLayer;
        for(LayerConfig hiddenConfig: topology.getHiddenLayerConfig()){
            System.out.println("Creating layer" + hiddenConfig.getLayerName() + ".");
            // Create layer and add to list.
            newLayer = new Layer.HiddenLayer(hiddenConfig);
            layers.add(newLayer);
            // Create the neurons of this layer and connect
            // them to the previous layer.
            fromLayer = layers.get(idx++);
            createNeurons(newLayer, fromLayer);
            numNeurons += layerConfig.getNumRows() * hiddenConfig.getNumColumns();
        }

        layerConfig = outputConfig;
        System.out.println("Creating output layer" + layerConfig.getLayerName() + ".");
        newLayer = new Layer.OutputLayer(layerConfig);
        layers.add(newLayer);
        // Create the neurons of this layer and connect
        // them to the previous layer.
        createNeurons(newLayer, layers.get(idx));
        numNeurons += layerConfig.getNumRows() * layerConfig.getNumColumns();

        // It's possible that some internal neurons don't feed any other neurons.
        // That's not a fatal error, but it's probably due to an unintentional mistake
        // in defining the net topology. Here we will find and report all neurons with
        // no forward connections so that the human can fix the topology configuration
        // if needed:

        System.out.println("\nChecking for neurons with no sinks:");

        // Loop through all layers except the output layer, looking for unconnected neurons:
        int neuronsWithNoSink = 0;
        for (int lidx = 0; lidx < layers.size() - 1; ++lidx) {
            Layer layer = layers.get(lidx);
            for (Neuron neuron : layer.getNeurons()) {
                if (!neuron.hasForwardConnections()) {
                    ++neuronsWithNoSink;
                    System.out.println("  neuron(" + neuron + ") on " + layer.getName()
                    + " has no forward connections.");
                }
            }
        }

        // Optionally enable the next line to display the resulting net topology:
        debugShowNet(true);

        System.out.println("Found " + neuronsWithNoSink + " neurons with no sink.");
        System.out.println(numNeurons + " neurons total; " + totalNumberConnections + " back+bias connections.");
        System.out.println("About " + (int)((float)totalNumberConnections / numNeurons + 0.5)
             + " connections per neuron on average.");
        if(config.isTrained()){
            System.out.println("Network is trained. Loading weights.");
            accept(new LoadWeightConfigVisitor(config.getWeightsConfig()));
        }
        this.eta = trainingParams.getEta();
    }

    // Create neurons and connect them. For the input layer, there are no incoming
    // connections and radius doesn't apply. Calling this function with layerFrom == null
    private void createNeurons(Layer layer, Layer layerFrom)
    {
        // Reserve enough space in layer.neurons to prevent reallocation (so that
        // we can form stable references to neurons):

        for (int row = 0; row < layer.getNumRows(); ++row) {
            for (int col = 0; col < layer.getNumColumns(); ++col) {
                // When we create a neuron, we have to give it a pointer to the
                // start of the array of Connection objects:
                Neuron neuron = layer.createNeuron(row, col, layer.getTransferFunction());
                ++totalNumberNeurons;

                // If layerFrom is layerTo, it means we're making input neurons
                // that have no input connections to the neurons. Else, we must make connections
                // to the source neurons and, for classic neurons, to a bias input:

                if (layer.getLayerType() != LayerType.INPUT) {
                    connectNeuron(layer, layerFrom, neuron,
                                  row, col);
                    if (!layer.isConvolutionLayer()) {
                        connectBias(neuron);
                    }
                }
            }
        }
    }

    // Assuming an ellipse centered at 0,0 and aligned with the global axes, returns
    // a positive value if x,y is outside the ellipse; 0.0 if on the ellipse;
    // negative if inside the ellipse.
    //
    private float elliptDist(float x, float y, float radiusX, float radiusY)
    {
        assert(radiusX >= 0.0 && radiusY >= 0.0);
        return radiusY*radiusY*x*x + radiusX*radiusX*y*y - radiusX*radiusX*radiusY*radiusY;
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
        assert(sizeX > 0 && sizeY > 0);

        // Calculate the normalized [0..1] coordinates of our neuron:
        float normalizedX = ((float)nx / sizeX) + (1.0f / (2 * sizeX));
        float normalizedY = ((float)ny / sizeY) + (1.0f / (2 * sizeY));

        // Calculate the coords of the nearest neuron in the "from" layer.
        // The calculated coords are relative to the "from" layer:
        int lfromX = (int)(normalizedX * layerFrom.getNumColumns()); // should we round off instead of round down?
        int lfromY = (int)(normalizedY * layerFrom.getNumRows());

    //    cout + "our neuron at " + nx + "," + ny + " covers neuron at "
    //         + lfromX + "," + lfromY + endl;

        // Calculate the rectangular window into the "from" layer:

        int xmin;
        int xmax;
        int ymin;
        int ymax;

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

        if (xmin < 0) xmin = 0;
        if (xmin >= (int)layerFrom.getNumColumns()) xmin = layerFrom.getNumColumns() - 1;
        if (ymin < 0) ymin = 0;
        if (ymin >= (int)layerFrom.getNumRows()) ymin = layerFrom.getNumRows() - 1;
        if (xmax < 0) xmax = 0;
        if (xmax >= (int)layerFrom.getNumColumns()) xmax = layerFrom.getNumColumns() - 1;
        if (ymax < 0) ymax = 0;
        if (ymax >= (int)layerFrom.getNumRows()) ymax = layerFrom.getNumRows() - 1;

        // Now (xmin,xmax,ymin,ymax) defines a rectangular subset of neurons in a previous layer.
        // We'll make a connection from each of those neurons in the previous layer to our
        // neuron in the current layer.

        // We will also check for and avoid duplicate connections. Duplicates are mostly harmless,
        // but unnecessary. Duplicate connections can be formed when the same layer name appears
        // more than once in the topology config file with the same "from" layer if the projected
        // rectangular or elliptical areas on the source layer overlap.

        float xcenter = ((float)xmin + (float)xmax) / 2.0f;
        float ycenter = ((float)ymin + (float)ymax) / 2.0f;
        int maxNumSourceNeurons = ((xmax - xmin) + 1) * ((ymax - ymin) + 1);

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

                if (neuron.sourceNeurons.contains(fromNeuron)) {
                    System.out.println("dup");
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
                        //connections.back().weight = (randomFloat() - 0.5) / maxNumSourceNeurons;
                        conn.setWeight(((randomFloat() * 2.0f) - 1.0f) / (float)Math.sqrt(maxNumSourceNeurons));
                    }

                    // Remember the source neuron for detecting duplicate connections:
                    neuron.sourceNeurons.add(fromNeuron);
                }
            }
        }
    }

    // Returns a random float in the range [0.0..1.0]
    //
    private float randomFloat()
    {
        return (float)((double)rand.nextInt(1073741824)/1073741823.0);
    }

    // Add a weighted bias input, modeled as a back-connection to a fake neuron:
    //
    private void connectBias(Neuron neuron)
    {
        // Create a new Connection record and get its index:
        Connection c = new Connection(biasNeuron, neuron);
        // connections.add(c);
        // int connectionIdx = connections.size() - 1;

        c.setWeight(randomFloat() - 0.5f); // Review this !!!
        c.setDeltaWeight(0.0f);

        // Record the back connection with the destination neuron:
        neuron.setBiasConnection(c);

        ++totalNumberConnections;

        // Record the forward connection with the fake bias neuron:
        biasNeuron.addForwardConnection(c);
    }

    // Calculate a new eta parameter based on the current and last average net error.
    //
    private float adjustedEta()
    {
        float thresholdUp = 0.001f;       // Ignore error increases less than this magnitude
        float thresholdDown = 0.01f;      // Ignore error decreases less than this magnitude
        float factorUp = 1.005f;          // Factor to incrementally increase eta
        float factorDown = 0.999f;        // Factor to incrementally decrease eta

        if (!trainingParams.isDynamicEta()) {
            return eta;
        }

        assert(thresholdUp > 0.0 && thresholdDown > 0.0 && factorUp >= 1.0 && factorDown >= 0.0 && factorDown <= 1.0);

        float errorGradient = (recentAverageError - lastRecentAverageError) / recentAverageError;
        if (errorGradient > thresholdUp) {
            eta = factorDown * eta;
        } else if (errorGradient < -thresholdDown) {
            eta = factorUp * eta;
        }

        return eta;

    }

    // Propagate inputs to outputs
    void feedForward()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    // This takes the values at the input layer and feeds them through the
    // neural net to produce new values at the output layer.
    void feedForward(Sample sample) throws SampleException
    {
        ++inputSampleNumber;

        // Move the input data from sample to the input neurons. We'll also
        // check that the number of components of the input sample equals
        // the number of input neurons:

        Layer inputLayer = layers.get(1);
        Command<Neuron,Float> command = new Neuron.AssignInputsCommand(sample.getData(inputLayer.getChannel()));
        inputLayer.executeCommand(command);

        // Start the forward propagation at the first hidden layer:

        for (int layerIdx = 2; layerIdx < layers.size(); ++layerIdx) {
            Layer layer = layers.get(layerIdx);
            layer.executeCommand(new Neuron.FeedForwardCommand());
        }

        // If target values are known, update the output neurons' errors and
        // update the overall net error:

        calculateOverallNetError(sample);
    }

    // Backprop and update all weights
    // Here is where the weights are updated. This is called after every training
    // sample. The outputs of the neural net are compared to the target output
    // values, and the differences are used to adjust the weights in all the
    // connections for all the neurons.
    void backProp(Sample sample)
    {
        // Calculate output layer gradients:

        Layer outputLayer = layers.get(layers.size()-1);
        outputLayer.executeCommand(new Neuron.CalculateGradientsCommand(sample.getTargetVals()));

        // Calculate hidden layer gradients. Skip output, input, and bias layers.
        for (int layerNum = layers.size() - 2; layerNum > 1; --layerNum) {
            Layer hiddenLayer = layers.get(layerNum); // Make a convenient name
            for(int row=0; row < hiddenLayer.getNumRows(); row++){
                for(int col=0; col < hiddenLayer.getNumColumns(); col++){
                    hiddenLayer.getNeuron(row,col).calcHiddenGradients();
                }
            }
        }

        // For all layers from outputs to first hidden layer, in reverse order,
        // update connection weights for regular neurons. Skip the udpate in
        // convolution layers.

        for (int layerNum = layers.size() - 1; layerNum > 1; --layerNum) {
            Layer layer = layers.get(layerNum);

            if (!layer.isConvolutionLayer()) {
                layer.executeCommand(new Neuron.InputWeightsCommand(eta,
                        trainingParams.getAlpha()));
            }
        }

        // Adjust eta if dynamic eta adjustment is enabled:

        if (trainingParams.isDynamicEta()) {
            eta = adjustedEta();
        }
    }

    // for forward propagation
    float getNetError()
    {
        return error;
    }

    // for forward propagation
    float getRecentAverageError()
    {
        return recentAverageError;
    }

    // for forward propagation
    // Given the set of target values for the output neurons, calculate
    // overall net error (RMS of the output neuron errors). This updates the
    // .error and .lastRecentAverageError members. If the container of target
    // values is empty, we'll return immediately, leaving the net error == 0.
    //
    void calculateOverallNetError(Sample sample) throws SampleException
    {
        float lambda = trainingParams.getLambda();
        int smoothingFactor = trainingParams.getRecentAverageSmoothingFactor();
        error = 0.0f;

        // Return if there are no known target values:

        if (sample.getTargetVals() == null) {
            return;
        }

        Layer outputLayer = layers.get(layers.size()-1);
        error = getRMS(outputLayer, sample);

        // Regularization calculations -- if this experiment works, calculate the sum of weights
        // on the fly during backprop to see if that is better performance.
        // This adds an error term calculated from the sum of squared weights. This encourages
        // the net to find a solution using small weight values, which can be helpful for
        // multiple reasons.

        if (lambda != 0.0) {
            Layer.AccumulateForwardWeights weightAction = new Layer.AccumulateForwardWeights();
            float sqWeight = executeCommand(weightAction);
            //for (int i = 0; i < connections.size(); ++i) {
            //    sumWeightsSquared_ += connections.get(i).getWeight();
            //}

            error += (sqWeight * lambda) / (2.0 * (totalNumberConnections - totalNumberNeurons));
        }

        // Implement a recent average measurement -- average the net errors over N samples:
        lastRecentAverageError = recentAverageError;
        recentAverageError =
                (recentAverageError * smoothingFactor + error)
                / (smoothingFactor + 1.0f);
    }

    private float getRMS(Layer layer, Sample sample)
    {
        Neuron.AccumulateSquareErrorCommand action = new Neuron.AccumulateSquareErrorCommand(sample);
        return layer.executeCommand(action)/(2.0f*layer.size());
    }

    public void train() throws SampleException
    {
        reportEveryNth = trainingParams.getReportEveryNth();
        if (trainingParams.shuffleInputSamples()) {
            sampleSet.shuffle();
        }
        do{
            for (int sampleIdx = 0; sampleIdx < sampleSet.getSamples().size(); ++sampleIdx) {
                Sample sample = sampleSet.getSamples().get(sampleIdx);
                feedForward(sample);
                backProp(sample);
                reportResults(sample);

                if (recentAverageError < trainingParams.getErrorThreshold()) {
                    return;
                }
            }
        } while(trainingParams.repeatInputSamples());
    }

    public void run() throws SampleException
    {
        run(false);
    }

    public boolean validate() throws SampleException
    {
        return run(true);
    }

    private boolean run(boolean validate) throws SampleException
    {
        reportEveryNth = 1;

        Layer lastLayer = layers.get(layers.size()-1);
        for (int sampleIdx = 0; sampleIdx < sampleSet.getSamples().size(); ++sampleIdx) {
            Sample sample = sampleSet.getSamples().get(sampleIdx);
            if (sample.getData().getNumRows() * sample.getData().getNumColumns() != getInputSize()) {
                throw new Net.SampleException("Sample "
                        + (sampleIdx + 1) + " size does not match the size of "
                        + "the input layer (" + getInputSize() + ")");
            }
            feedForward(sample);
            if(validate){
                Matrix targets = sample.getTargetVals();
                for (Neuron n : lastLayer.getNeurons()) { // For all neurons in output layer
                    float target = targets.get(n.getRow(), n.getColumn());
                    float rms = getRMS(lastLayer, sample);
                    if(rms > trainingParams.getErrorThreshold()){
                        System.out.println("Validation failed for neuron at row "
                                + n.getRow() + " column " + n.getColumn() + " in output layer. "
                        + "Expected " + target + " with RMS error " + rms
                        + " but got " + n.getOutput());
                        return false;
                    }
                }
            }

            reportResults(sample);
        }
        return true;
    }

    // for displaying the results when processing input samples
    // Assumes the net's output neuron errors and overall net error have already been
    // computed and saved in the case where the target output values are known.
    //
    void reportResults(Sample sample)
    {
        long time = System.currentTimeMillis() - lastReportTime;
        // We actually report only every Nth input sample:
        Layer lastLayer = layers.get(layers.size()-1);
        if (inputSampleNumber % reportEveryNth != 0) {
            return;
        }

        // Report actual and expected outputs:

        System.out.print( "\nPass #" + inputSampleNumber + "\nOutputs: ");
        for (Neuron n : lastLayer.getNeurons()) { // For all neurons in output layer
            System.out.print( n.output + " ");
        }
        System.out.println();

        if (sample.getTargetVals() != null) {
            System.out.print( "Expected: ");
            System.out.print(sample.getTargetVals().toString());

            // Optional: Enable the following block if you would like to report the net's
            // outputs as a classifier, where the output neuron with the largest output
            // value indicates which class was recognized. This can be used, e.g., for pattern
            // recognition where each output neuron corresponds to one pattern class,
            // and the output neurons are trained to be high to indicate a pattern match,
            // and low to indicate no match.

            if (lastLayer.isClassifier()) {
                //float maxOutput = (numeric_limits<float>::min)();
                int maxCol, maxRow;

                Neuron.MaxNeuronCommand action = new Neuron.MaxNeuronCommand();
                Neuron.MaxRowCol max = lastLayer.executeCommand(action);
                maxCol = max.col;
                maxRow = max.row;
                if (sample.getTargetVal(maxRow, maxCol) > 0.0) {
                    System.out.print( " Correct");
                } else {
                    System.out.print( " Wrong");
                }
                System.out.println();
            }

            // Optionally enable the following line to display the current eta value
            // (in case we're dynamically adjusting it):
            System.out.print( "  eta=" + eta + " ");

            // Show overall net error for this sample and for the last few samples averaged:
            System.out.println( "Net error = " + error + ", running average = " + recentAverageError);
            if(lastReportTime > 0){
                System.out.println("Pass time: " + time + "ms. ");
            }
        }
        lastReportTime = System.currentTimeMillis();
    }

    public int getInputSize()
    {
        return layers.get(1).size();
    }

    // for displaying the results when processing input samples
    // This is an optional way to display lots of information about the network
    // topology. Tweak as needed. The argument 'details' can be used to control
    // if all the connections are displayed in detail.
    void debugShowNet(boolean details)
    {
        int numFwdConnections;
        int numBackConnections;

        System.out.println( "\n\nNet configuration (incl. bias connection): --------------------------");

        for (Layer l : layers) {
            numFwdConnections = 0;
            numBackConnections = 0;
            System.out.println( "Layer '" + l.getName() + "' has " + l.size()
                 + " neurons arranged in " + l.getNumRows() + "x" + l.getNumColumns() + ":");

            for (Neuron  n : l.getNeurons()) {
                if (details) {
                    System.out.println( "  neuron(" + n + ")" + " output: " + n.output);
                }

                numFwdConnections += n.getNumForwardConnections();
                numBackConnections += n.getNumBackConnections(); // Includes the bias connection

                if (details && n.hasForwardConnections()) {
                    System.out.println( "    Fwd connections:" +
                            n.debugShowFwdNet());
                }

                if (details && n.hasBackConnections()) {
                    System.out.println( "    Back connections (incl. bias):" +
                            n.debugShowBackNet());
                }
            }

            if (!details) {
                System.out.println( "   connections: " + numBackConnections + " back, "
                     + numFwdConnections + " forward.");
            }
        }
    }

    @Override
    public void accept(NetElementVisitor v)
    {
        if(v.visit(this)){
            for(Layer layer: layers){
                layer.accept(v);
            }
        }
    }

    public static class LayerException extends Exception
    {
        public LayerException(String string)
        {
            super(string);
        }
    }

    public static class SampleException extends Exception
    {
        public SampleException(String string)
        {
            super(string);
        }
    }

    public static class FileFormatException extends Exception
    {
        public FileFormatException(String string)
        {
            super(string);
        }
    }

    /**
     * If the Command is parallelizable, the Commands may be
     * executed, once per Layer, in any order, in parallel. Otherwise,
     * the Commands will be executed, once per Layer, in any order,
     * serially.
     * @param <T>
     * @param action
     * @return
     */
    public <T> T executeCommand(Command<Layer,T> action)
    {
        NetTask<T> nAction = new NetTask<>(action);
        return pool.invoke(nAction).getResult();
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


    private class NetTask<T> extends RecursiveTask<Command.JoinableResult<T>>
    {
        private final int start;
        private final int len;
        private final int numSplits;
        private final Command<Layer,T> action;

        public NetTask(Command<Layer,T> action)
        {
            this(action, 0, layers.size());
        }

        protected NetTask(Command<Layer,T> action, int start, int len)
        {
            this(action, start, len, 0);
        }

        protected NetTask(Command<Layer,T> action, int start, int len, int numSplits)
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
            public boolean visit(Layer n){
                JoinableResult<T> res = action.execute(n);
                if(result == null){
                    result = res;
                } else {
                    result.join(res.getResult());
                }
                return false;
            }
        }

        @Override
        protected Command.JoinableResult<T> compute()
        {
            if(!action.canParallelize() || len < 128 /* && numSplits < numProcessors */){
                NetTaskVisitor v = new NetTaskVisitor();
                accept(v);
                return v.result;
            } else {
                NetTask<T> left, right;
                int split =len/2;
                left = new NetTask<>(action, start, split, numSplits+1);
                right = new NetTask<>(action, start + split, len-split, numSplits+1);
                left.fork();
                Command.JoinableResult<T> result = right.compute();
                result.join(left.join().getResult());
                return result;
            }
        }
    }
}
