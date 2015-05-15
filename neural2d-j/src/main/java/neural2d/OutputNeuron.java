package neural2d;

import java.text.MessageFormat;
import java.util.logging.Level;

/**
 * <p>
 * <p>
 * <p>
 * Copyright (c) 2015 Michael C. Whidden
 *
 * @author Michael C. Whidden
 */
class OutputNeuron extends NeuronImpl
{

    public OutputNeuron(TransferFunction tf, Layer layer, int row, int col)
    {
        super(tf, layer, row, col);
    }

    // For backprop training
    @Override
    public void calcGradient(double targetVal)
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

}
