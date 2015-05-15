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

    public BiasLayer(Net net, LayerConfig params)
    {
        super(net, params);
    }

    @Override
    public LayerType getLayerType()
    {
        return LayerType.BIAS;
    }

    @Override
    public boolean acceptsBias()
    {
        return false;
    }

    // Add a weighted bias input, modeled as a back-connection to a fake neuron:
    //
    @Override
    public int connectTo(Layer layer)
    {
        BiasConnectorVisitor v = new BiasConnectorVisitor();
        layer.accept(v);
        return v.numConnections;
    }

    private class BiasConnectorVisitor extends NetElementVisitor
    {
        int numConnections = 0;
        @Override
        public boolean visit(Neuron neuron)
        {
            Neuron biasNeuron = getNeuron(0, 0);
            // Create a new Connection record and get its index:
            Connection c = new Connection(biasNeuron, neuron);
            // connections.add(c);
            // int connectionIdx = connections.size() - 1;

            c.setWeight(randomDouble() - 0.5); // Review this !!!
            c.setDeltaWeight(0.0);

            // Record the back connection with the destination neuron:
            neuron.addBackConnection(c);
            numConnections++;

            // Record the forward connection with the fake bias neuron:
            biasNeuron.addForwardConnection(c);
            return false;
        }
    }

    @Override
    public Command<Neuron, Double> getCalculateGradientsCommand(Matrix targeVals)
    {
        return new Command.NoOpCommand<>();
    }

    @Override
    public Command<Neuron, Double> getFeedForwardCommand(Matrix inputData)
    {
        // Return a no-op.
        return new Command.NoOpCommand<>();
    }
}
