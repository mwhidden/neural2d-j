package neural2d;

/**
 *
 * Copyright (c) 2015 Michael C. Whidden
 *
 * @author Michael C. Whidden
 */
public abstract class NetElementVisitor
{

    /**
     * For NetElements that can be traversed either forward or backward, this
     * indicates which direction to go.
     *
     * @return the direction. Currently only applies to layer traversal.
     */
    public Direction getDirection()
    {
        return Direction.FORWARD;
    }

    public enum Direction
    {

        FORWARD, BACKWARD
    };

    /**
     * @param net
     * @return true if deeper nodes should be visited, or false to prevent
     * deepening.
     */
    public boolean visit(Net net)
    {
        return true;
    }

    /**
     * @param layer
     * @return true if deeper nodes should be visited, or false to prevent
     * deepening.
     */
    public boolean visit(Layer layer)
    {
        return true;
    }

    /**
     * @param neuron
     * @return true if deeper nodes should be visited, or false to prevent
     * deepening.
     */
    public boolean visit(Neuron neuron)
    {
        return true;
    }

    /**
     * @param conn
     * @return true if deeper nodes should be visited, or false to prevent
     * deepening.
     */
    public boolean visit(Connection conn)
    {
        return true;
    }
}
