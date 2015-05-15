package neural2d;

import neural2d.config.LayerConfig;

/**
 * Copyright (c) 2015 Michael C. Whidden
 *
 * @author Michael C. Whidden
 */
public class HiddenLayer extends LayerImpl
{

    private HiddenLayer(Net net, LayerConfig params)
    {
        super(net, params);
    }

    @Override
    public boolean acceptsBias()
    {
        return true;
    }

    public static Layer createLayer(Net net, LayerConfig params)
    {
        Layer l = new HiddenLayer(net, params);
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
        return new CalculateGradientsCommand();
    }

    private static class CalculateGradientsCommand implements Command<Neuron, Double>
    {

        public CalculateGradientsCommand()
        {
        }

        @Override
        public Command.DoubleResult execute(Neuron n)
        {
            n.calcGradient(0.0); // parameter is ignored on hidden layer neurons
            return new Command.DoubleResult(0.0);
        }

        @Override
        public boolean canParallelize()
        {
            return true;
        }
    }
}
