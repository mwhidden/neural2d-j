package neural2d;

import neural2d.config.LayerConfig;

/**
 * Copyright (c) 2015 Michael C. Whidden
 *
 * @author Michael C. Whidden
 */
public class HiddenLayer extends LayerImpl
{

    private HiddenLayer(LayerConfig params)
    {
        super(params);
    }

    public static Layer createLayer(LayerConfig params)
    {
        Layer l = new HiddenLayer(params);
        l.createNeurons();
        return l;
    }

    @Override
    public LayerType getLayerType()
    {
        return LayerType.HIDDEN;
    }

    @Override
    public Command<Neuron, Double> getFeedForwardCommand(Matrix inputData)
    {
        return new Neuron.FeedForwardCommand();
    }

    @Override
    public Command<Neuron, Double> getCalculateGradientsCommand(Matrix targeVals)
    {
        return new CalculateHiddenGradientsCommand();
    }

    private static class CalculateHiddenGradientsCommand implements Command<Neuron, Double>
    {

        public CalculateHiddenGradientsCommand()
        {
        }

        @Override
        public Command.DoubleResult execute(Neuron n)
        {
            n.calcHiddenGradients();
            return new Command.DoubleResult(0.0);
        }

        @Override
        public boolean canParallelize()
        {
            return true;
        }
    }
}
