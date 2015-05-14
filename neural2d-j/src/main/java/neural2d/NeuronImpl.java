package neural2d;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * Copyright Michael C. Whidden 2015
 *
 * @author Michael C. Whidden
 */
public abstract class NeuronImpl implements Neuron
{

    // The output of the neuron, after the transfer function is applied.
    protected double output;
    protected double inputSum; // sum of the inputs

    // This is the error gradient for the Neuron.
    // As the weight on a connection changes, the error in the
    // output varies. The gradient is how much we think we may
    // have to change the current output to arrive at the desired
    // output.
    //
    // On an output neuron, if the desired output (after transfer function)
    // is 1.0, and the current output (after transfer function) is -0.5, then
    // the error is 1.5 (because the output is -0.5, and the error is
    // desiredOutput - output). If we  were to infinitesimally change the
    // input, the gradient is the rate of change in the error, which is just
    // the derivative(slope) of the transfer function evaluated at the output.
    // Again, if the input changed by d, the new output is approximated by
    // tf(input) + d*tf'(output). The gradient is this change in the output,
    // which is the d*tf'(output) term.
    //
    // The idea for hidden neurons is the similar, but more complex.
    // Each hidden neuron contributes its output to one or more neurons
    // in the next forward layer. Its contribution is summed with the
    // outputs of one or more other neurons as well. We need to define
    // how much a change in the output of this neuron's will impact the
    // errors in the neurons in the next layer.
    // So we look at each neuron that this neuron are feeds into, and determine
    // the error in that forward neuron's output.
    //
    // See calcOutputGradient and calcHiddenGradients
    protected double gradient;

    // All the input and output connections for this neuron.
    protected final TransferFunction transferFunction;
    protected final Layer layer;
    protected final int row, col; // position of this Neuron in its layer.
    protected final List<Connection> backConnections;

    protected final List<Connection> forwardConnections;
    private static final AtomicInteger idSource = new AtomicInteger(1);
    protected final int id;

    protected NeuronImpl(TransferFunction tf,
            Layer layer,
            int row,
            int col)
    {
        this.output = 1.0;
        this.inputSum = 0.0;
        this.gradient = 0.0;
        this.transferFunction = tf;
        this.layer = layer;
        this.row = row;
        this.col = col;
        this.id = idSource.getAndIncrement();
        this.backConnections = new ArrayList<>();
        this.forwardConnections = new ArrayList<>();
    }

    @Override
    public double getGradient()
    {
        return gradient;
    }

    /**
     *
     * @return the layer in which this Neuron resides.
     */
    @Override
    public Layer getLayer()
    {
        return layer;
    }

    // For backprop training
    // The error gradient of a hidden-layer neuron is equal to the derivative
    // of the activation function of the hidden layer evaluated at the
    // local output of the neuron times the sum of the product of
    // the primary outputs times their associated hidden-to-output weights.
    //
    @Override
    public void calcHiddenGradients()
    {
        double dow = sumDOW_nextLayer();
        // neural 2d formula
        //gradient = dow * transferFunction.derivative().transfer(output);
        // heaton research formula
        gradient = dow * transferFunction.derivative().transfer(inputSum);
    }

    // For backprop training
    // The error gradient of an output-layer neuron is equal to the target (desired)
    // value minus the computed output value, times the derivative of
    // the output-layer activation function evaluated at the computed output value.
    //
    // On an output neuron with a, say, LINEAR transfer function, if the
    // desired output (after transfer function)
    // is 1.0, and the current output (after transfer function) is -0.5, then
    // the error is 1.5 (because the output is -0.5, and the error is
    // desiredOutput - output). The gradient tells us how much we need to
    // change the output to achieve the desired output, extrapolating from the
    // slope (derivative) of the transfer function at the current output.
    // For the example neuron, the slope is 1, so we need to change the input
    // by (surprise!) 1.5 to achieve the desired output.
    //
    // If i is the input, d the desired output, o the current output,
    // g the gradient,
    // tf the transfer function, and tf' the derivative of tf, then:
    // o = tf(i)
    // d = tf(i+g)
    // We need to approximate g.
    // We can approximate g as tf(o) + tf'(d-o)
    // For the given example:
    // g = tf(o) + tf'(d-o)
    // g =
    // o = tf(i) = -0.5
    // d = tf(g-0.5) = g-0.5
    //
    // If we  were to infinitesimally change the
    // input, the gradient is the rate of change in the error, which is just
    // the derivative(slope) of the transfer function evaluated at the output.
    // Again, if the input changed by d, the new output is approximated by
    // tf(input) + d*tf'(output). The gradient is this change in the output,
    // which is the d*tf'(output) term. In the example output neuron above,
    // the gradient would be
    @Override
    public void calcOutputGradients(double targetVal)
    {
        if (Neural2D.LOG.isLoggable(Level.FINEST)) {
            Neural2D.LOG.finest("Calculate output gradient for neuron "
                    + getName());
        }
        double delta = targetVal - output;
        // Heaton Research value
        gradient = delta * transferFunction.derivative().transfer(inputSum);
        // neural2d formula
        //gradient = delta * transferFunction.derivative().transfer(output);
        if (Neural2D.LOG.isLoggable(Level.FINEST)) {
            Neural2D.LOG.finest(MessageFormat.format("gradient (node delta) is "
                    + "error * dTF(inputSum) = {0} * dTF({1}) = {0} x {2} = {3}",
                    delta, inputSum, transferFunction.derivative().transfer(inputSum),
                    gradient));
        }
    }

    @Override
    public void accept(NetElementVisitor v)
    {
        acceptForward(v);
    }

    /**
     * Like acceptForward(), but traverses the backConnections instead of the
     * forward ones.
     *
     * @param v
     */
    protected void acceptBackward(NetElementVisitor v)
    {
        if (v.visit(this)) {
            for (Connection conn : backConnections) {
                conn.accept(v);
            }
        }
    }

    /**
     * Like acceptBackward(), but traverses the fwdConnections instead of the
     * backward ones.
     *
     * @param v
     */
    protected void acceptForward(NetElementVisitor v)
    {
        if (v.visit(this)) {
            for (Connection conn : forwardConnections) {
                conn.accept(v);
            }
        }
    }

    /*
     * Calculate the product of the connection weight and the
     * target node gradient. This is the 2nd part of the gradient
     * formula for the gradient.
     */
    private static class SumDOWVisitor extends NetElementVisitor
    {

        double sum = 0.0;

        @Override
        public boolean visit(Neuron n)
        {
            return true;
        }

        @Override
        public boolean visit(Connection conn)
        {
            if (Neural2D.LOG.isLoggable(Level.FINEST)) {
                Neural2D.LOG.finest(MessageFormat.format("DOW contribution from {0} to {1} is "
                        + "{2} x {3} = {4}",
                        conn.getFromNeuron().getName(),
                        conn.getToNeuron().getName(),
                        conn.getWeight(),
                        conn.getToNeuron().getGradient(),
                        conn.getWeight() * conn.getToNeuron().getGradient()));
            }

            sum += conn.getWeight() * conn.getToNeuron().getGradient();
            return false;
        }
    }

    // Used in hidden layer backprop training
    protected double sumDOW_nextLayer()
    {
        SumDOWVisitor v = new SumDOWVisitor();
        accept(v);
        return v.sum;
    }

    @Override
    public boolean hasForwardConnections()
    {
        return !forwardConnections.isEmpty();
    }

    @Override
    public boolean hasBackConnections()
    {
        return !backConnections.isEmpty();
    }

    @Override
    public int getNumForwardConnections()
    {
        return forwardConnections.size();
    }

    @Override
    public int getNumBackConnections()
    {
        return backConnections.size();
    }

    @Override
    public String toString()
    {
        return "Neuron(" + id + ")";
    }

    private static class DebugVisitor extends NetElementVisitor
    {

        StringBuilder buff = new StringBuilder();

        @Override
        public boolean visit(Connection conn)
        {
            buff.append(conn);
            return false;
        }
    }

    public String debugShowFwdNet()
    {
        DebugVisitor v = new DebugVisitor();
        accept(v);
        return v.buff.toString();
    }

    public String debugShowBackNet()
    {
        DebugVisitor v = new DebugVisitor();
        acceptBackward(v);
        return v.buff.toString();
    }

    private static class FeedForwardVisitor extends NetElementVisitor
    {

        double sum = 0.0;
        private final boolean finest = Neural2D.LOG.isLoggable(Level.FINEST);

        @Override
        public boolean visit(Connection conn)
        {
            sum += conn.getWeight() * conn.getFromNeuron().getOutput();
            if (finest) {
                Neural2D.LOG.finest(MessageFormat.format("- Neuron {0} contributes weight {1} x output {2} = {3} to Neuron {4}",
                        conn.getFromNeuron().getName(),
                        conn.getWeight(),
                        conn.getFromNeuron().getOutput(),
                        conn.getWeight() * conn.getFromNeuron().getOutput(),
                        conn.getToNeuron().getName()));
            }
            return false;
        }
    }

    // Propagate the net inputs to the outputs
    // To feed forward an individual neuron, we'll sum the weighted inputs, then pass that
    // sum through the transfer function.
    @Override
    public void feedForward()
    {
        FeedForwardVisitor v = new FeedForwardVisitor();
        acceptBackward(v);

        // Shape the output by passing it through the transfer function:
        inputSum = v.sum;
        output = transferFunction.transfer(v.sum);
    }

    public String getName()
    {
        return "" + id;
    }

    private static class UpdateWeightsVisitor extends NetElementVisitor
    {

        double eta, alpha, gradient;

        public UpdateWeightsVisitor(double eta, double alpha, double gradient)
        {
            this.eta = eta;
            this.alpha = alpha;
            this.gradient = gradient;
        }

        @Override
        public boolean visit(Connection conn)
        {
            Neuron fromNeuron = conn.getFromNeuron();
            double oldDeltaWeight = conn.getDeltaWeight();

            // Note: Heaton Research calls "fromNeuron*getOutput()" the "gradient",
            // and calls our "gradient" a "node delta" or "layer delta".
            double newDeltaWeight
                    = // Individual input, magnified by the gradient and train rate:
                    eta
                    * fromNeuron.getOutput()
                    * gradient
                    // Add momentum = a fraction of the previous delta weight;
                    + alpha
                    * oldDeltaWeight;

            if (Neural2D.LOG.isLoggable(Level.FINEST)) {
                Neural2D.LOG.finest(MessageFormat.format("Updating connection weight from "
                        + "neuron {0} to neuron {1}. Weight {2}, previous delta weight is {3} "
                        + " gradient of neuron {1} is {4}. Output from {0} is {5}. "
                        + " delta weight is eta({6}) * {5} * {4} + alpha({7}) * {3} = {8}."
                        + " New weight is {9}.",
                        fromNeuron.getName(),
                        conn.getToNeuron().getName(),
                        conn.getWeight(),
                        oldDeltaWeight,
                        gradient,
                        fromNeuron.getOutput(),
                        eta,
                        alpha,
                        newDeltaWeight,
                        conn.getWeight() + newDeltaWeight));
            }
            conn.setDeltaWeight(newDeltaWeight);
            conn.setWeight(conn.getWeight() + newDeltaWeight);
            return false;
        }
    }

    // For backprop training
    @Override
    public void updateInputWeights(double eta, double alpha)
    {
        // The weights to be updated are the weights from the neurons in the
        // preceding layer (the source layer) to this neuron:
        UpdateWeightsVisitor v = new UpdateWeightsVisitor(eta, alpha, gradient);
        acceptBackward(v);
    }

    @Override
    public double getOutput()
    {
        return output;
    }

    @Override
    public void setGradient(double f)
    {
        this.gradient = f;
    }

    @Override
    public void addBackConnection(Connection c)
    {
        backConnections.add(c);
    }

    @Override
    public void addForwardConnection(Connection c)
    {
        forwardConnections.add(c);
    }

    @Override
    public int getColumn()
    {
        return col;
    }

    @Override
    public int getRow()
    {
        return row;
    }

}
