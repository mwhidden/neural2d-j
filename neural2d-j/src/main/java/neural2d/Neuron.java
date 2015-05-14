package neural2d;

import java.text.MessageFormat;
import java.util.logging.Level;

/**
 * Copyright Michael C. Whidden 2015
 *
 * @author Michael C. Whidden
 */
public interface Neuron extends NetElement
{

    // For backprop training
    // The error gradient of a hidden-layer neuron is equal to the derivative
    // of the activation function of the hidden layer evaluated at the
    // local output of the neuron times the sum of the product of
    // the primary outputs times their associated hidden-to-output weights.
    //
    void calcHiddenGradients();

    // For backprop training
    // The error gradient of an output-layer neuron is equal to the target (desired)
    // value minus the computed output value, times the derivative of
    // the output-layer activation function evaluated at the computed output value.
    //
    void calcOutputGradients(double targetVal);

    // Propagate the net inputs to the outputs
    // To feed forward an individual neuron, we'll sum the weighted inputs, then pass that
    // sum through the transfer function.
    void feedForward();

    double getGradient();

    int getColumn();

    /**
     *
     * @return the layer in which this Neuron resides.
     */
    Layer getLayer();

    int getNumBackConnections();

    int getNumForwardConnections();

    double getOutput();

    int getRow();

    boolean hasBackConnections();

    boolean hasForwardConnections();

    void setGradient(double f);

    // For backprop training
    void updateInputWeights(double eta, double alpha);

    void addBackConnection(Connection c);

    void addForwardConnection(Connection c);

    String getName();

    public static class AccumulateSquareWeightsCommand implements Command<Neuron, Double>
    {

        private static class SquareWeightsVisitor extends NetElementVisitor
        {

            double sqWeights = 0.0;

            @Override
            public boolean visit(Connection conn)
            {
                double w = conn.getWeight();
                sqWeights += (w * w);
                return false;
            }

        }

        @Override
        public Command.DoubleResult execute(Neuron n)
        {
            SquareWeightsVisitor v = new SquareWeightsVisitor();
            n.accept(v);
            return new Command.DoubleResult(v.sqWeights);
        }

        @Override
        public boolean canParallelize()
        {
            return true; // not parellizable
        }
    }

    static class FeedForwardCommand implements Command<Neuron, Double>
    {

        @Override
        public Command.DoubleResult execute(Neuron n)
        {
            if (Neural2D.LOG.isLoggable(Level.FINEST)) {
                Neural2D.LOG.finest(MessageFormat.format("Feeding forward to neuron {0}",
                        n.getName()));
            }
            n.feedForward();
            return new Command.DoubleResult(0.0);
        }

        @Override
        public boolean canParallelize()
        {
            return true;
        }
    }

    static class InputWeightsCommand implements Command<Neuron, Double>
    {

        private final double eta;
        private final double alpha;

        public InputWeightsCommand(double eta, double alpha)
        {
            this.eta = eta;
            this.alpha = alpha;
        }

        @Override
        public Command.DoubleResult execute(Neuron n)
        {
            n.updateInputWeights(eta, alpha);
            return new Command.DoubleResult(0.0);
        }

        @Override
        public boolean canParallelize()
        {
            return true;
        }
    }
}
