package neural2d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copyright Michael C. Whidden 2015
 * @author Michael C. Whidden
 */
public class NeuronImpl implements Neuron
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

    protected NeuronImpl(TransferFunction tf,
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
        this.backConnections = new ArrayList<>();
        this.forwardConnections = new ArrayList<>();
    }

    @Override
    public float getGradient()
    {
        return gradient;
    }

    /**
     *
     * @return the layer in which this Neuron resides.
     */
    @Override
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
    @Override
    public void calcHiddenGradients(){
        float dow = sumDOW_nextLayer();
        gradient = dow * transferFunction.derivative().transfer(output);
    }

    // For backprop training
    // The error gradient of an output-layer neuron is equal to the target (desired)
    // value minus the computed output value, times the derivative of
    // the output-layer activation function evaluated at the computed output value.
    //
    @Override
    public void calcOutputGradients(float targetVal){
        float delta = targetVal - output;
        gradient = delta * transferFunction.derivative().transfer(output);
    }

    @Override
    public void accept(NetElementVisitor v)
    {
        acceptForward(v);
    }

    /**
     * Like acceptForward(), but traverses the backConnections instead of the
     * forward ones.
     * @param v
     */
    protected void acceptBackward(NetElementVisitor v)
    {
        if(v.visit(this)){
            for(Connection conn: backConnections){
                conn.accept(v);
            }
        }
    }

    /**
     * Like acceptBackward(), but traverses the fwdConnections instead of the
     * backward ones.
     * @param v
     */
    protected void acceptForward(NetElementVisitor v)
    {
        if(v.visit(this)){
            for(Connection conn: forwardConnections){
                conn.accept(v);
            }
        }
    }

    private static class SumDOWVisitor extends NetElementVisitor
    {
        float sum =0.0f;

        @Override
        public boolean visit(Connection conn)
        {
            sum += conn.getWeight() * conn.getToNeuron().getGradient();
            return false;
        }
    }

    // Used in hidden layer backprop training
    protected float sumDOW_nextLayer()
    {
        SumDOWVisitor v = new SumDOWVisitor();
        accept(v);
        return v.sum;
    }

    @Override
    public boolean hasForwardConnections()
    {
        return !forwardConnections.isEmpty();
    }

    @Override
    public boolean hasBackConnections()
    {
        return !backConnections.isEmpty();
    }


    @Override
    public int getNumForwardConnections()
    {
        return forwardConnections.size();
    }

    @Override
    public int getNumBackConnections()
    {
        return backConnections.size();
    }

    @Override
    public String toString()
    {
        return "Neuron(" + id + ")";
    }

    private static class DebugVisitor extends NetElementVisitor
    {
        StringBuilder buff = new StringBuilder();

        @Override
        public boolean visit(Connection conn)
        {
            buff.append(conn);
            return false;
        }
    }

    public String debugShowFwdNet()
    {
        DebugVisitor v = new DebugVisitor();
        accept(v);
        return v.buff.toString();
    }

    public String debugShowBackNet()
    {
        DebugVisitor v = new DebugVisitor();
        acceptBackward(v);
        return v.buff.toString();
    }

    private static class FeedForwardVisitor extends NetElementVisitor
    {
        float sum =0.0f;

        @Override
        public boolean visit(Connection conn)
        {
            sum += conn.getWeight() * conn.getFromNeuron().getOutput();
            return false;
        }
    }

    // Propagate the net inputs to the outputs
    // To feed forward an individual neuron, we'll sum the weighted inputs, then pass that
    // sum through the transfer function.
    @Override
    public void feedForward(){
        FeedForwardVisitor v = new FeedForwardVisitor();
        acceptBackward(v);

        // Shape the output by passing it through the transfer function:
        setOutput(transferFunction.transfer(v.sum));
    }

    private static class UpdateWeightsVisitor extends NetElementVisitor
    {
        float eta, alpha, gradient;

        public UpdateWeightsVisitor(float eta, float alpha, float gradient)
        {
            this.eta = eta;
            this.alpha = alpha;
            this.gradient = gradient;
        }


        @Override
        public boolean visit(Connection conn)
        {
            Neuron fromNeuron = conn.getFromNeuron();
            float oldDeltaWeight = conn.getDeltaWeight();

            float newDeltaWeight =
                    // Individual input, magnified by the gradient and train rate:
                    eta
                    * fromNeuron.getOutput()
                    * gradient
                    // Add momentum = a fraction of the previous delta weight;
                    + alpha
                    * oldDeltaWeight;

            conn.setDeltaWeight(newDeltaWeight);
            conn.setWeight(conn.getWeight() + newDeltaWeight);
            return false;
        }
    }


    // For backprop training
    @Override
    public void updateInputWeights(float eta, float alpha){
        // The weights to be updated are the weights from the neurons in the
        // preceding layer (the source layer) to this neuron:
        UpdateWeightsVisitor v = new UpdateWeightsVisitor(eta, alpha, gradient);
        acceptBackward(v);
    }

    @Override
    public float getOutput()
    {
        return output;
    }

    @Override
    public void setOutput(float f)
    {
        this.output = f;
    }

    @Override
    public void setGradient(float f)
    {
        this.gradient = f;
    }

    @Override
    public void addBackConnection(Connection c)
    {
        backConnections.add(c);
    }

    @Override
    public void setBiasConnection(Connection c)
    {
        addBackConnection(c);
        this.biasConnection = c;
    }

    @Override
    public void addForwardConnection(Connection c)
    {
        forwardConnections.add(c);
    }


    @Override
    public int getColumn()
    {
        return col;
    }

    @Override
    public int getRow()
    {
        return row;
    }

}
