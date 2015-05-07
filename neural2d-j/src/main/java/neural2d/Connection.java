package neural2d;

/**
 * <p>
 * Copyright (c) 2015 Michael C. Whidden
 * @author Michael C. Whidden
 */
public class Connection implements NetElement
{
    private final Neuron fromNeuron;
    private final Neuron toNeuron;
    private float weight;
    private float deltaWeight; // the weight change from the previous training iteration

    public Connection(Neuron from, Neuron to)
    {
        this.fromNeuron = from;
        this.toNeuron = to;
        this.deltaWeight = 0.0f;
    }

    @Override
    public String toString()
    {
        return "Connection(" + fromNeuron + "--(" + weight + "/" + deltaWeight + ")-->" + toNeuron + ")";
    }

    public float getWeight()
    {
        return weight;
    }

    public void setWeight(float weight)
    {
        this.weight = weight;
    }

    public float getDeltaWeight()
    {
        return deltaWeight;
    }

    public void setDeltaWeight(float deltaWeight)
    {
        this.deltaWeight = deltaWeight;
    }

    public Neuron getFromNeuron()
    {
        return fromNeuron;
    }

    public Neuron getToNeuron()
    {
        return toNeuron;
    }

    @Override
    public void accept(NetElementVisitor v)
    {
        if(v.visit(this)){
            // If we had child nodes, we would child.accept(v) here...
        }
    }



}
