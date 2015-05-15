package neural2d;

/**
 * <p>
 * <p>
 * <p>
 * Copyright (c) 2015 Michael C. Whidden
 *
 * @author Michael C. Whidden
 */
public class BiasNeuron extends NeuronImpl
{

    public BiasNeuron(Layer layer)
    {
        super(TransferFunction.IDENTITY, layer, 0, 0);
        this.gradient = 0.0;
        this.output = 1.0;
    }

    @Override
    public String toString()
    {
        return "BiasNeuron(" + id + ")";
    }

    @Override
    public void calcGradient(double target)
    {
        // no gradient on bias neurons
    }

}
