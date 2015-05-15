package neural2d;

/**
 * <p>
 * <p>
 * <p>
 * Copyright (c) 2015 Michael C. Whidden
 *
 * @author Michael C. Whidden
 */
class InputNeuron extends NeuronImpl
{
    public InputNeuron(Layer layer, int row, int col)
    {
        super(TransferFunction.IDENTITY, layer, row, col);
    }

    public void setOutput(double f)
    {
        this.output = f;
    }

    @Override
    public void calcGradient(double target)
    {
        // No gradient on input neurons
    }
}
