package neural2d;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * <p>
 ** Copyright Michael C. Whidden 2015
 * @author Michael C. Whidden
 */
public class NeuronImpl extends Neuron
{
    public NeuronImpl(TransferFunction tf,
            Layer layer,
            int row,
            int col)
    {
        super(tf, layer, row, col);
        this.backConnections = new ArrayList<>();
        this.forwardConnections = new ArrayList<>();
    }


    // Propagate the net inputs to the outputs
    // To feed forward an individual neuron, we'll sum the weighted inputs, then pass that
    // sum through the transfer function.
    //
    @Override
    public void feedForward(){
        float sum = 0.0f;

        // Sum the neuron's inputs:
        for(Connection conn: backConnections){
            sum += conn.getFromNeuron().output * conn.getWeight();
        }

        // Shape the output by passing it through the transfer function:
        setOutput(transferFunction.transfer(sum));
    }

    // For backprop training
    @Override
    public void updateInputWeights(float eta, float alpha){
        // The weights to be updated are the weights from the neurons in the
        // preceding layer (the source layer) to this neuron:

        //
        for(Connection conn: backConnections){
            Neuron fromNeuron = conn.getFromNeuron();
            float oldDeltaWeight = conn.getDeltaWeight();

            float newDeltaWeight =
                    // Individual input, magnified by the gradient and train rate:
                    eta
                    * fromNeuron.output
                    * gradient
                    // Add momentum = a fraction of the previous delta weight;
                    + alpha
                    * oldDeltaWeight;

            conn.setDeltaWeight(newDeltaWeight);
            conn.setWeight(conn.getWeight() + newDeltaWeight);
        }
    }

    // Used in hidden layer backprop training
    @Override
    protected float sumDOW_nextLayer(){
        float sum = 0.0f;
        // Sum our contributions of the errors at the nodes we feed.

        for (Connection conn : forwardConnections) {
            sum += conn.getWeight() * conn.getToNeuron().gradient;
        }

        return sum;

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

    @Override
    public String debugShowFwdNet()
    {
        StringBuilder buff = new StringBuilder();
        for (Connection pc : forwardConnections) {
            buff.append(pc);
        }
        return buff.toString();
    }

    @Override
    public String debugShowBackNet()
    {
        StringBuilder buff = new StringBuilder();
        for (Connection c : backConnections) {
            buff.append(c);
            if(c.getToNeuron() != this){
                System.out.println("***** This neuron's back-connected neuron is not forward-connected to it ****");
            }
        }
        return buff.toString();
    }

    @Override
    void addBackConnection(Connection c)
    {
        backConnections.add(c);
    }

    @Override
    void setBiasConnection(Connection c)
    {
        addBackConnection(c);
        this.biasConnection = c;
    }

    @Override
    void addForwardConnection(Connection c)
    {
        forwardConnections.add(c);
    }


    @Override
    int readWeights(BufferedReader reader) throws IOException
    {
        int lineNum = 0;
        for (Connection conn : backConnections) {
            String line = reader.readLine();
            if(line == null){
                throw new IOException("Weights file has too few weights. ");
            }
            try {
                lineNum++;
                conn.setWeight(Float.parseFloat(line));
            } catch (NumberFormatException e){
                throw new IOException("Bad weight");
            }
        }
        return lineNum;
    }
}
