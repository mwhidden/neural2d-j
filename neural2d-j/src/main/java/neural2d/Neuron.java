package neural2d;


/**
 * Copyright Michael C. Whidden 2015
 * @author Michael C. Whidden
 */
public interface Neuron extends NetElement
{

    // For backprop training
    // The error gradient of a hidden-layer neuron is equal to the derivative
    // of the activation function of the hidden layer evaluated at the
    // local output of the neuron times the sum of the product of
    // the primary outputs times their associated hidden-to-output weights.
    //
    void calcHiddenGradients();

    // For backprop training
    // The error gradient of an output-layer neuron is equal to the target (desired)
    // value minus the computed output value, times the derivative of
    // the output-layer activation function evaluated at the computed output value.
    //
    void calcOutputGradients(float targetVal);

    // Propagate the net inputs to the outputs
    // To feed forward an individual neuron, we'll sum the weighted inputs, then pass that
    // sum through the transfer function.
    void feedForward();

    float getGradient();

    int getColumn();

    /**
     *
     * @return the layer in which this Neuron resides.
     */
    Layer getLayer();

    int getNumBackConnections();

    int getNumForwardConnections();

    float getOutput();

    int getRow();

    boolean hasBackConnections();

    boolean hasForwardConnections();

    void setGradient(float f);

    void setOutput(float f);

    // For backprop training
    void updateInputWeights(float eta, float alpha);

    void addBackConnection(Connection c);

    void addForwardConnection(Connection c);

    void setBiasConnection(Connection c);

    public static class AccumulateSquareWeightsCommand implements Command<Neuron,Float>
    {
        private static class SquareWeightsVisitor extends NetElementVisitor
        {
            float sqWeights = 0.0f;

            @Override
            public boolean visit(Connection conn)
            {
                float w = conn.getWeight();
                sqWeights += (w*w);
                return false;
            }


        }

        @Override
        public Command.FloatResult execute(Neuron n)
        {
            SquareWeightsVisitor v = new SquareWeightsVisitor();
            n.accept(v);
            return new Command.FloatResult(v.sqWeights);
        }

        @Override
        public boolean canParallelize()
        {
            return true; // not parellizable
        }
    }

    public static class FeedForwardCommand implements Command<Neuron,Float>
    {
        @Override
        public Command.FloatResult execute(Neuron n)
        {
            n.feedForward();
            return new Command.FloatResult(0.0f);
        }

        @Override
        public boolean canParallelize()
        {
            return true;
        }
    }

    public static class InputWeightsCommand implements Command<Neuron,Float>
    {
        private final float eta;
        private final float alpha;

        public InputWeightsCommand(float eta, float alpha)
        {
            this.eta = eta;
            this.alpha = alpha;
        }

        @Override
        public Command.FloatResult execute(Neuron n)
        {
            n.updateInputWeights(eta, alpha);
            return new Command.FloatResult(0.0f);
        }

        @Override
        public boolean canParallelize()
        {
            return true;
        }
    }

    public static class AccumulateSquareErrorCommand implements Command<Neuron,Float>
    {
        private final Sample sample;

        public AccumulateSquareErrorCommand(Sample sample)
        {
            this.sample = sample;
        }

        @Override
        public Command.FloatResult execute(Neuron n)
        {
            float delta = sample.getTargetVal(n.getRow(),n.getColumn()) - n.getOutput();
            return new Command.FloatResult(delta * delta);
        }

        @Override
        public boolean canParallelize()
        {
            return true; // not parellizable
        }
    }

    public static class MaxRowCol
    {
        float max;
        int row, col;
    }

    public static class MaxRowColResult implements Command.JoinableResult<MaxRowCol>
    {
        private final MaxRowCol result;

        public MaxRowColResult(MaxRowCol m)
        {
            this.result = m;
        }

        @Override
        public void join(MaxRowCol o)
        {
            if(o.max > result.max){
                result.max = o.max;
                result.row = o.row;
                result.col = o.col;
            }
        }

        @Override
        public MaxRowCol getResult()
        {
            return result;
        }

    }

    public static class MaxNeuronCommand implements Command<Neuron,MaxRowCol>
    {
        private int maxCol, maxRow;

        public int getMaxColumn()
        {
            return maxCol;
        }

        public int getMaxRow()
        {
            return maxRow;
        }

        @Override
        public MaxRowColResult execute(Neuron n)
        {
            MaxRowCol m = new MaxRowCol();
            m.max = n.getOutput();
            m.col = n.getColumn();
            m.row = n.getRow();
            return new MaxRowColResult(m);
        }

        @Override
        public boolean canParallelize()
        {
            return false; // not parellizable
        }
    }

    public static class AssignInputsCommand implements Command<Neuron,Float>
    {
        private final Matrix inputs;
        public AssignInputsCommand(Matrix inputs)
        {
            this.inputs = inputs;
        }

        @Override
        public Command.FloatResult execute(Neuron n)
        {
            n.setOutput(inputs.get(n.getRow(), n.getColumn()));
            return new Command.FloatResult(0.0f);
        }

        @Override
        public boolean canParallelize()
        {
            return true;
        }

    }

    public static class CalculateGradientsCommand implements Command<Neuron,Float>
    {
        private final Matrix targets;
        public CalculateGradientsCommand(Matrix targets)
        {
            this.targets = targets;
        }

        @Override
        public Command.FloatResult execute(Neuron n)
        {
            n.calcOutputGradients(targets.get(n.getRow(), n.getColumn()));
            return new Command.FloatResult(0.0f);
        }

        @Override
        public boolean canParallelize()
        {
            return true;
        }
    }


}
