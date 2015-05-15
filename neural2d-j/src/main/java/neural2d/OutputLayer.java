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
public class OutputLayer extends LayerImpl
{

    private OutputLayer(Net net, LayerConfig params)
    {
        super(net, params);
    }

    public static Layer createLayer(Net net, LayerConfig params)
    {
        Layer l = new OutputLayer(net, params);
        l.createNeurons();
        return l;
    }

    @Override
    public boolean acceptsBias()
    {
        return true;
    }

    @Override
    public LayerType getLayerType()
    {
        return LayerType.OUTPUT;
    }

    @Override
    public Command<Neuron, Double> getFeedForwardCommand(Matrix inputData)
    {
        return new Neuron.FeedForwardCommand();
    }

    @Override
    public Command<Neuron, Double> getCalculateGradientsCommand(Matrix targetVals)
    {
        return new CalculateGradientsCommand(targetVals);
    }

    private static class CalculateGradientsCommand implements Command<Neuron, Double>
    {

        private final Matrix targets;

        public CalculateGradientsCommand(Matrix targets)
        {
            this.targets = targets;
        }

        @Override
        public Command.DoubleResult execute(Neuron n)
        {
            n.calcGradient(targets.get(n.getRow(), n.getColumn()));
            return new Command.DoubleResult(0.0);
        }

        @Override
        public boolean canParallelize()
        {
            return true;
        }
    }
}
