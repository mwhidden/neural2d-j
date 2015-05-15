package neural2d;

/**
 * <p>
 * <p>
 * <p>
 * Copyright (c) 2015 Michael C. Whidden
 *
 * @author Michael C. Whidden
 */
class HiddenNeuron extends NeuronImpl
{

    public HiddenNeuron(TransferFunction tf, Layer layer, int row, int col)
    {
        super(tf, layer, row, col);
    }

    // For backprop training
    // The error gradient of a hidden-layer neuron is equal to the derivative
    // of the activation function of the hidden layer evaluated at the
    // local output of the neuron times the sum of the product of
    // the primary outputs times their associated hidden-to-output weights.
    //
    @Override
    public void calcGradient(double target)
    {
        double dow = sumDOW_nextLayer();
        // neural 2d formula
        //gradient = dow * transferFunction.derivative().transfer(output);
        // heaton research formula
        gradient = dow * transferFunction.derivative().transfer(inputSum);
    }


}
