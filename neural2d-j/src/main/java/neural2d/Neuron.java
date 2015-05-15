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
    void calcGradient(double target);

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

    /**
     * Returns true if this neuron has a forward connection to the given neuron.
     * @param n
     * @return true if this neuron has a forward connection to the given neuron.
     */
    boolean isForwardConnectedTo(Neuron n);

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

}
