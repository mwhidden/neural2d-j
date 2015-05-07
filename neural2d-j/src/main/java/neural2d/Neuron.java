package neural2d;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import neural2d.Command.JoinableResult;

/**
 * <p>
 *
 * <p>
 *
 * <p>
 * Copyright Michael C. Whidden 2015
 * @author Michael C. Whidden
 */
public abstract class Neuron implements NetElement
{
    protected float output, gradient;
    // All the input and output connections for this neuron.
    List<Connection> connections;   // the container of Connection records
    Set<Neuron> sourceNeurons;
    protected TransferFunction transferFunction;
    protected Layer layer;
    int row,col; // position of this Neuron in its layer.
    List<Connection> backConnections;

    List<Connection> forwardConnections;
    Connection biasConnection;
    private static final AtomicInteger idSource = new AtomicInteger(1);
    protected final int id;

    protected Neuron(TransferFunction tf,
            Layer layer,
            int row,
            int col)
    {
        this.output = 1.0f;
        this.gradient = 0.0f;
        this.connections = new ArrayList<>();
        this.transferFunction = tf;
        this.sourceNeurons = new HashSet<>();
        this.layer = layer;
        this.row = row;
        this.col = col;
        this.id = idSource.getAndIncrement();
    }


    /**
     *
     * @return the layer in which this Neuron resides.
     */
    public Layer getLayer()
    {
        return layer;
    }

    // For backprop training
    // The error gradient of a hidden-layer neuron is equal to the derivative
    // of the activation function of the hidden layer evaluated at the
    // local output of the neuron times the sum of the product of
    // the primary outputs times their associated hidden-to-output weights.
    //
    public void calcHiddenGradients(){
        float dow = sumDOW_nextLayer();
        gradient = dow * transferFunction.derivative().transfer(output);
    }

    // For backprop training
    // The error gradient of an output-layer neuron is equal to the target (desired)
    // value minus the computed output value, times the derivative of
    // the output-layer activation function evaluated at the computed output value.
    //
    public void calcOutputGradients(float targetVal){
        float delta = targetVal - output;
        gradient = delta * transferFunction.derivative().transfer(output);
    }

    @Override
    public void accept(NetElementVisitor v)
    {
        if(v.visit(this)){
            for(Connection conn: forwardConnections){
                conn.accept(v);
            }
        }
    }
    protected abstract float sumDOW_nextLayer();

    abstract boolean hasForwardConnections();
    abstract boolean hasBackConnections();

    abstract int getNumForwardConnections();

    abstract int getNumBackConnections();

    abstract String debugShowFwdNet();

    abstract String debugShowBackNet();


    // Propagate the net inputs to the outputs
    // To feed forward an individual neuron, we'll sum the weighted inputs, then pass that
    // sum through the transfer function.
    //
    abstract void feedForward();

    // For backprop training
    abstract void updateInputWeights(float eta, float alpha);

    public float getOutput()
    {
        return output;
    }

    public void setOutput(float f)
    {
        this.output = f;
    }

    public void setGradient(float f)
    {
        this.gradient = f;
    }

    abstract void addBackConnection(Connection c);
    abstract void setBiasConnection(Connection c);

    abstract void addForwardConnection(Connection c);

    public int getColumn()
    {
        return col;
    }

    public int getRow()
    {
        return row;
    }

    abstract int readWeights(BufferedReader reader) throws IOException;

    protected static class FeedForwardCommand implements Command<Neuron,Float>
    {
        @Override
        public FloatResult execute(Neuron n)
        {
            n.feedForward();
            return new FloatResult(0.0f);
        }

        @Override
        public boolean canParallelize()
        {
            return true;
        }
    }

    protected static class InputWeightsCommand implements Command<Neuron,Float>
    {
        private final float eta;
        private final float alpha;

        public InputWeightsCommand(float eta, float alpha)
        {
            this.eta = eta;
            this.alpha = alpha;
        }

        @Override
        public FloatResult execute(Neuron n)
        {
            n.updateInputWeights(eta, alpha);
            return new FloatResult(0.0f);
        }

        @Override
        public boolean canParallelize()
        {
            return true;
        }
    }

    protected static class AccumulateSquareErrorCommand implements Command<Neuron,Float>
    {
        private final Sample sample;

        public AccumulateSquareErrorCommand(Sample sample)
        {
            this.sample = sample;
        }

        @Override
        public FloatResult execute(Neuron n)
        {
            float delta = sample.getTargetVal(n.getRow(),n.getColumn()) - n.getOutput();
            return new FloatResult(delta * delta);
        }

        @Override
        public boolean canParallelize()
        {
            return true; // not parellizable
        }
    }

    protected static class AccumulateSquareWeightsCommand implements Command<Neuron,Float>
    {

        @Override
        public FloatResult execute(Neuron n)
        {
            float weights = 0.0f;
            float w;
            for(Connection conn: n.forwardConnections){
                w = conn.getWeight();
                weights += (w*w);
            }
            return new FloatResult(weights);
        }

        @Override
        public boolean canParallelize()
        {
            return true; // not parellizable
        }
    }


    protected static class MaxRowCol
    {
        float max;
        int row, col;
    }

    protected static class MaxRowColResult implements JoinableResult<MaxRowCol>
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

    protected static class MaxNeuronCommand implements Command<Neuron,MaxRowCol>
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
        public FloatResult execute(Neuron n)
        {
            n.setOutput(inputs.get(n.getRow(), n.getColumn()));
            return new FloatResult(0.0f);
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
        public FloatResult execute(Neuron n)
        {
            n.calcOutputGradients(targets.get(n.getRow(), n.getColumn()));
            return new FloatResult(0.0f);
        }

        @Override
        public boolean canParallelize()
        {
            return true;
        }
    }

}
