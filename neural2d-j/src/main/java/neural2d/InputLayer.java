package neural2d;

import java.text.MessageFormat;
import java.util.logging.Level;
import static neural2d.Neural2D.LOG;
import neural2d.config.LayerConfig;

/**
 * Copyright (c) 2015 Michael C. Whidden
 *
 * @author Michael C. Whidden
 */
public class InputLayer extends LayerImpl
{

    private InputLayer(Net net, LayerConfig params)
    {
        super(net, params);
    }

    @Override
    public boolean acceptsBias()
    {
        return false;
    }

    public static Layer createLayer(Net net, LayerConfig params)
    {
        Layer l = new InputLayer(net, params);
        l.createNeurons();
        return l;
    }

    @Override
    public LayerType getLayerType()
    {
        return LayerType.INPUT;
    }

    @Override
    public Command<Neuron, Double> getFeedForwardCommand(Matrix inputData)
    {
        return new AssignInputsCommand(inputData);
    }

    @Override
    public Command<Neuron, Double> getCalculateGradientsCommand(Matrix targeVals)
    {
        return new Command.NoOpCommand<>();
    }

    private static class AssignInputsCommand implements Command<Neuron, Double>
    {

        private final Matrix inputs;

        public AssignInputsCommand(Matrix inputs)
        {
            this.inputs = inputs;
        }

        @Override
        public Command.DoubleResult execute(Neuron n)
        {
            // Allow the ClassCastException to indicate some internal
            // error. All neurons provided to this command must be
            // input neurons.
            double input = inputs.get(n.getRow(), n.getColumn());
            if (Neural2D.LOG.isLoggable(Level.FINEST)) {
                Neural2D.LOG.finest(MessageFormat.format("- Feeding input {0} to Neuron {1}",
                        "" + input, n.getName()));
            }
            try {
                ((InputNeuron) n).setOutput(inputs.get(n.getRow(), n.getColumn()));
            } catch (ClassCastException e) {
                // whoa how'd that happen?
                LOG.severe("Internal error: InputLayer had a non-input neuron in it "
                        + "at row=" + n.getRow() + " column=" + n.getColumn()
                        + " on layer " + n.getLayer());
            }
            return new Command.DoubleResult(0.0);
        }

        @Override
        public boolean canParallelize()
        {
            return true;
        }

    }

}
