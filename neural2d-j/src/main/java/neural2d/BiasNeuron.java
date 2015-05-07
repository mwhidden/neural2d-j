package neural2d;

/**
 * <p>
 * <p>
 * <p>
 * Copyright (c) 2015 Michael C. Whidden
 * @author Michael C. Whidden
 */
public class BiasNeuron extends NeuronImpl
{
    public BiasNeuron(Layer layer)
    {
        super(TransferFunction.IDENTITY, layer, 0, 0);
        this.gradient = 0.0f;
        this.output = 1.0f;
    }

    @Override
    public String toString()
    {
        return "BiasNeuron(" + id + ")";
    }

}
