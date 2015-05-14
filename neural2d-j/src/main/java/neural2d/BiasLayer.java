package neural2d;

import neural2d.config.LayerConfig;

/**
 * <p>
 * <p>
 * <p>
 * Copyright (c) 2015 Michael C. Whidden
 *
 * @author Michael C. Whidden
 */
public class BiasLayer extends LayerImpl
{

    public BiasLayer(LayerConfig params)
    {
        super(params);
    }

    @Override
    public LayerType getLayerType()
    {
        return LayerType.BIAS;
    }

    @Override
    public Command<Neuron, Double> getFeedForwardCommand(Matrix inputData)
    {
        // Return a no-op.
        return new Command.NoOpCommand<>();
    }

    @Override
    public Command<Neuron, Double> getCalculateGradientsCommand(Matrix targeVals)
    {
        return new Command.NoOpCommand<>();
    }

}
