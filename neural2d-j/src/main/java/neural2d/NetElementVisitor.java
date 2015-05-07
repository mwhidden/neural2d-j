package neural2d;

/**
 * <p>
 * <p>
 * <p>
 * Copyright (c) 2015 Michael C. Whidden
 * @author Michael C. Whidden
 */
public abstract class NetElementVisitor {
    /**
     * @param net
     * @return true if deeper nodes should be visited, or false
     * to prevent deepening.
    */
    public boolean visit(Net net){return true;}
    /**
     * @param layer
     * @return true if deeper nodes should be visited, or false
     * to prevent deepening.
    */
    public boolean visit(Layer layer){return true;}
    /**
     * @param neuron
     * @return true if deeper nodes should be visited, or false
     * to prevent deepening.
    */
    public boolean visit(Neuron neuron){return true;}
    /**
     * @param conn
     * @return true if deeper nodes should be visited, or false
     * to prevent deepening.
    */
    public boolean visit(Connection conn){return true;}
}
