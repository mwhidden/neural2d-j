package neural2d;

import java.util.Random;
import java.util.logging.Level;
import neural2d.config.TrainingConfig;
import neural2d.display.OutputFunctionDisplay;

/**
 * @author Michael C. Whidden
 */
class Trainer
{

    // To reduce screen clutter during training, reportEveryNth can be set > 1. When
    // in VALIDATE or TRAINED mode, you'll want to set this to 1 so you can see every
    // result:
    private int reportEveryNth;
    private double eta; // effective eta. May change if dynamicEta is active.
    private double recentAverageError;   // Averaged over recentAverageSmoothingFactor samples
    private double lastRecentAverageError;    // Used for dynamically adjusting eta
    private double error;                // Overall net error
    private int inputSampleNumber = 0;
    private final TrainingConfig trainingConfig;

    public Trainer(TrainingConfig config)
    {
        reportEveryNth = 1;
        error = 1.0;
        recentAverageError = 1.0;
        lastRecentAverageError = 1.0;
        trainingConfig = config;
        eta = trainingConfig.getEta();
    }

    public String getTrainingStatus()
    {
        StringBuilder result = new StringBuilder();
        // Optionally enable the following line to display the current eta value
        // (in case we're dynamically adjusting it):
        result.append("Pass #").append(inputSampleNumber).append("\n");
        result.append("  Eta=").append(eta).append(" ");

        // Show overall net error for this sample and for the last few samples averaged:
        result.append("net error = ").append(error)
                .append(", running average = ").append(recentAverageError).append("\n");
        return result.toString();

    }

    // for forward propagation
    // Given the set of target values for the output neurons, calculate
    // overall net error (RMS of the output neuron errors). This updates the
    // .error and .lastRecentAverageError members. If the container of target
    // values is empty, we'll return immediately, leaving the net error == 0.
    //
    void calculateOverallNetError(Net net, Matrix output, Sample sample) throws Net.SampleException
    {
        double lambda = trainingConfig.getLambda();
        int smoothingFactor = trainingConfig.getRecentAverageSmoothingFactor();
        error = 0.0;

        // Return if there are no known target values:
        if (sample.getTargetVals() == null) {
            return;
        }

        error = getRMS(output, sample.getTargetVals());

        // Regularization calculations -- if this experiment works, calculate the sum of weights
        // on the fly during backprop to see if that is better performance.
        // This adds an error term calculated from the sum of squared weights. This encourages
        // the net to find a solution using small weight values, which can be helpful for
        // multiple reasons.
        if (lambda != 0.0) {
            Layer.AccumulateForwardWeights weightAction = new Layer.AccumulateForwardWeights();
            double sqWeight = net.executeCommand(weightAction);
            //for (int i = 0; i < connections.size(); ++i) {
            //    sumWeightsSquared_ += connections.get(i).getWeight();
            //}

            error += (sqWeight * lambda) / (2.0 * (net.getNumConnections() - net.getNumNeurons()));
        }

        // Implement a recent average measurement -- average the net errors over N samples:
        lastRecentAverageError = recentAverageError;
        recentAverageError
                = (recentAverageError * smoothingFactor + error)
                / (smoothingFactor + 1.0);
    }

    private double getRMS(Matrix output, Matrix targets)
    {
        double sqSum = 0.0, delta;
        for (int r = 0; r < output.getNumRows(); r++) {
            for (int c = 0; c < output.getNumRows(); c++) {
                delta = targets.get(r, c) - output.get(r, c);
                sqSum += (delta * delta);
            }
        }
        return sqSum / (2.0 * output.getNumRows() * output.getNumColumns());
    }

    // Calculate a new eta parameter based on the current and last average net error.
    //
    private double adjustEta()
    {
        double thresholdUp = 0.001;       // Ignore error increases less than this magnitude
        double thresholdDown = 0.01;      // Ignore error decreases less than this magnitude
        double factorUp = 1.005;          // Factor to incrementally increase eta
        double factorDown = 0.999;        // Factor to incrementally decrease eta

        // assert(thresholdUp > 0.0 && thresholdDown > 0.0 && factorUp >= 1.0 && factorDown >= 0.0 && factorDown <= 1.0);
        double errorGradient = (recentAverageError - lastRecentAverageError) / recentAverageError;
        if (errorGradient > thresholdUp) {
            return factorDown * eta;
        } else if (errorGradient < -thresholdDown) {
            return factorUp * eta;
        } else {
            return eta;
        }
    }

    // Backprop and update all weights
    // Here is where the weights are updated. This is called after every training
    // sample. The outputs of the neural net are compared to the target output
    // values, and the differences are used to adjust the weights in all the
    // connections for all the neurons.
    void backProp(Net net, Sample sample)
    {
        if (Neural2D.LOG.isLoggable(Level.FINEST)) {
            Neural2D.LOG.finest("Beginning back prop training");
        }

        // Calculate output and hidden layer gradients. Skip input, and bias
        // layers. Have to process the layers in backward order, so don't
        // parallelize the layer traversal.
        net.accept(new CalculateGradientsVisitor(sample));

        // For all layers from outputs to first hidden layer, in reverse order,
        // update connection weights for regular neurons. Skip the udpate in
        // convolution layers.
        net.accept(new InputWeightsVisitor(eta, trainingConfig.getAlpha()));

        // Adjust eta if dynamic eta adjustment is enabled:
        if (trainingConfig.isDynamicEta()) {
            eta = adjustEta();
        }
    }

    boolean validate(Matrix output, Matrix targets)
    {
        double rms = getRMS(output, targets);
        if (rms > trainingConfig.getErrorThreshold()) {

            Neural2D.LOG.severe("Validation failed with RMS error " + rms
                    + ". Expected " + targets + " but was " + output);
            return false;
        }
        return true;
    }

    void train(Net net, SampleSet samples, Random rand, OutputFunctionDisplay disp) throws Neural2DException
    {
        reportEveryNth = trainingConfig.getReportEveryNth();
        if (trainingConfig.shuffleInputSamples()) {
            // Provide rand for shuffling so we can have reproducible
            // results if a seed was specified.
            samples.shuffle(rand);
        }

        do {
            for (int sampleIdx = 0; sampleIdx < samples.getSamples().size(); ++sampleIdx) {
                Sample sample = samples.getSamples().get(sampleIdx);
                ++inputSampleNumber;
                Matrix output = net.compute(sample);
                // If target values are known, update the output neurons' errors and
                // update the overall net error:
                calculateOverallNetError(net, output, sample);
                backProp(net, sample);
                if (inputSampleNumber % reportEveryNth == 0) {
                    net.reportResults(sample, disp);
                }
                if (recentAverageError < trainingConfig.getErrorThreshold()) {
                    return;
                }
            }
        } while (trainingConfig.repeatInputSamples());
    }

    private static class InputWeightsVisitor extends NetElementVisitor
    {

        private final double eta, alpha;

        public InputWeightsVisitor(double eta, double alpha)
        {
            this.eta = eta;
            this.alpha = alpha;
        }

        @Override
        public boolean visit(Layer layer)
        {
            switch (layer.getLayerType()) {
                case INPUT:
                case BIAS:
                    break;
                default:
                    if (!layer.isConvolutionLayer()) {
                        if (Neural2D.LOG.isLoggable(Level.FINEST)) {
                            Neural2D.LOG.finest("Updating the weight for connections into layer " + layer.getName());
                        }

                        layer.executeCommand(new Neuron.InputWeightsCommand(eta, alpha));
                    }
            }
            return false; //don't deepen
        }

        @Override
        public NetElementVisitor.Direction getDirection()
        {
            // gradients are calculated from output layer toward input layer.
            return NetElementVisitor.Direction.BACKWARD;
        }

    }

    private static class CalculateGradientsVisitor extends NetElementVisitor
    {

        private final Sample sample;

        public CalculateGradientsVisitor(Sample sample)
        {
            this.sample = sample;
        }

        @Override
        public boolean visit(Layer layer)
        {
            switch (layer.getLayerType()) {
                case INPUT:
                case BIAS:
                    break;
                default:
                    if (Neural2D.LOG.isLoggable(Level.FINEST)) {
                        Neural2D.LOG.finest("Calculate gradients for layer " + layer.getName());
                    }
                    layer.executeCommand(layer.getCalculateGradientsCommand(sample.getTargetVals()));
            }
            return false; //don't deepen
        }

        @Override
        public NetElementVisitor.Direction getDirection()
        {
            // gradients are calculated from output layer toward input layer.
            return NetElementVisitor.Direction.BACKWARD;
        }
    }

}
