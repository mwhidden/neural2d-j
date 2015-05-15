package neural2d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import neural2d.Command.JoinableResult;
import neural2d.config.LayerConfig;

/**
 * <p>
 * Copyright (c) 2015 Michael C. Whidden
 *
 * @author Michael C. Whidden
 */
public abstract class LayerImpl implements Layer
{

    private final LayerConfig params;
    private final String name;
    private final List<Neuron> neurons; // 2d array, flattened index = row * numCols + col
    private final int numNeurons;
    private final ForkJoinPool pool = new ForkJoinPool();
    protected final Net net;

    public LayerImpl(Net net, LayerConfig params)
    {
        this.net = net;
        this.params = params;
        this.name = params.getLayerName();
        this.numNeurons = params.getNumRows() * params.getNumColumns();
        this.neurons = new ArrayList<>(numNeurons);
    }

    @Override
    public void createNeurons()
    {
        // Make sure this.params is set before calling
        // createNeuron
        for (int row = 0; row < params.getNumRows(); ++row) {
            for (int col = 0; col < params.getNumColumns(); ++col) {
                neurons.add(createNeuron(row, col));
            }
        }
    }

    protected double randomDouble()
    {
        return net.randomDouble();
    }

    @Override
    public void accept(NetElementVisitor v)
    {
        if (v.visit(this)) {
            for (Neuron neuron : neurons) {
                neuron.accept(v);
            }
        }
    }

    @Override
    public TransferFunction getTransferFunction()
    {
        return params.getTransferFunction();
    }

    @Override
    public boolean isConvolutionLayer()
    {
        return params.isConvolutionLayer();
    }

    @Override
    public Matrix getOutput()
    {
        int numRows = getNumRows();
        int numCols = getNumColumns();
        Matrix output = new Matrix(numRows, numCols);
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                output.set(r, c, getNeuron(r, c).getOutput());
            }
        }
        return output;
    }

    /**
     * Each neuron in this layer will be connected to one or more neurons in the
     * <i>previous</i> layer which lie in an ellipse or rectangle centered on a
     * neuron in the previous layer whose x,y location matches the neuron in
     * this layer, with a width of twice <code>getRadiusX</code>
     */
    @Override
    public int getRadiusX()
    {
        return params.getRadiusX();
    }

    /**
     * Each neuron in this layer will be connected to one or more neurons in the
     * <i>previous</i> layer which lie in an ellipse or rectangle centered on a
     * neuron in the previous layer whose x,y location matches the neuron in
     * this layer, with a height of twice <code>getRadiusY</code>
     */
    @Override
    public int getRadiusY()
    {
        return params.getRadiusY();
    }

    /**
     * If true, then <code>getRadiusX</code> and <code>getRadiusY</code> specify
     * the width and height of a rectangular region. If false, they specify the
     * width and height of an ellipse.
     *
     * @return
     */
    @Override
    public boolean isRectangular()
    {
        return params.isRectangular();
    }

    /**
     * When true on an output layer, the output neuron with the greatest value
     * will be forced to 1.0, and all other neurons will be forced to -1.0.
     *
     * @return
     */
    @Override
    public boolean isClassifier()
    {
        return params.isClassifier();
    }

    @Override
    public ColorChannel getChannel()
    {
        return params.getChannel();
    }

    @Override
    public int connectTo(Layer toLayer)
    {
        // If layerFrom is layerTo, it means we're making input neurons
        // that have no input connections to the neurons. Else, we must make connections
        // to the source neurons and, for classic neurons, to a bias input:
        int numConnections = 0;
        for (int row = 0; row < toLayer.getNumRows(); ++row) {
            for (int col = 0; col < toLayer.getNumColumns(); ++col) {
                numConnections += connectToNeuron(toLayer, row, col);
            }
        }
        return numConnections;
    }

    // Assuming an ellipse centered at 0,0 and aligned with the global axes, returns
    // a positive value if x,y is outside the ellipse; 0.0 if on the ellipse;
    // negative if inside the ellipse.
    //
    private double elliptDist(double x, double y, double radiusX, double radiusY)
    {
        assert (radiusX >= 0.0 && radiusY >= 0.0);
        return radiusY * radiusY * x * x + radiusX * radiusX * y * y - radiusX * radiusX * radiusY * radiusY;
    }

    private int clip(int val, int min, int max)
    {
        val = Math.max(min, val);
        val = Math.min(max, val);
        return val;
    }

    // This creates the initial set of connections for a layer of neurons. (If the same layer
    // appears again in the topology config file, those additional connections must be added
    // to existing connections by calling addToLayer() instead of this function.
    //
    // Neurons can be "regular" neurons, or convolution nodes. If a convolution matrix is
    // defined for the layer, the neurons in that layer will be connected to source neurons
    // in a rectangular pattern defined by the matrix dimensions. No bias connections are
    // created for convolution nodes. Convolution nodes ignore any radius parameter.
    // For convolution nodes, the transfer function is set to be the identity function.
    //
    // For regular neurons,
    // the location of the destination neuron is projected onto the neurons of the source
    // layer. A shape is defined by the radius parameters, and is considered to be either
    // rectangular or elliptical (depending on the value of projectRectangular below).
    // A radius of 0,0 connects a single source neuron in the source layer to this
    // destination neuron. E.g., a radius of 1,1, if projectRectangular is true, connects
    // nine neurons from the source layer in a 3x3 block to this destination neuron.
    //
    // Each Neuron object holds a container of Connection objects for all the source
    // inputs to the neuron. Each neuron also holds a container of pointers to Connection
    // objects in the forward direction. Those points point to Container objects in
    // and owned by neurons in other layers. I.e., the master copies of all connection are
    // in the containers of back connections; forward connection pointers refer to
    // back connection records in other neurons.
    //
    private int connectToNeuron(Layer layerTo, int toRow, int toCol)
    {
        // TODO: have the 'to layer' let us know which
        // neurons on our side it wants to hear from.
        Neuron toNeuron = layerTo.getNeuron(toRow, toCol);
        int numCols = layerTo.getNumColumns();
        int numRows = layerTo.getNumRows();
        int numConns = 0;
        assert (numCols > 0 && numRows > 0);

        // Calculate the normalized [0..1] coordinates of our neuron:
        double normalizeCol = ((float) toCol / numCols) + (1.0 / (2 * numCols));
        double normalizedRow = ((float) toRow / numRows) + (1.0 / (2 * numRows));

        // Calculate the coords of the nearest neuron in the "from" layer.
        // The calculated coords are relative to the "from" layer:
        int lFromCol = (int) (normalizeCol * getNumColumns()); // should we round off instead of round down?
        int lFromRow = (int) (normalizedRow * getNumRows());

        // Calculate the rectangular window into the "from" layer:
        int minCol, maxCol;
        int minRow, maxRow;

        if (layerTo.isConvolutionLayer()) {
            //ymin = lfromY - params.convolveMatrix.get(0).size() / 2;
            //ymax = ymin + params.convolveMatrix.get(0).size() - 1;
            //xmin = lfromX - params.convolveMatrix.size() / 2;
            //xmax = xmin + params.convolveMatrix.size() - 1;
            throw new UnsupportedOperationException("TODO");
        } else {
            minCol = lFromCol - layerTo.getRadiusX();
            maxCol = lFromCol + layerTo.getRadiusX();
            minRow = lFromRow - layerTo.getRadiusY();
            maxRow = lFromRow + layerTo.getRadiusY();
        }

        // Clip to the layer boundaries:
        minCol = clip(minCol, 0, getNumColumns() - 1);
        minRow = clip(minRow, 0, getNumRows() - 1);
        maxCol = clip(maxCol, 0, getNumColumns() - 1);
        maxRow = clip(maxRow, 0, getNumRows() - 1);

        // Now (xmin,xmax,ymin,ymax) defines a rectangular subset of neurons in a previous layer.
        // We'll make a connection from each of those neurons in the previous layer to our
        // neuron in the current layer.
        // We will also check for and avoid duplicate connections. Duplicates are mostly harmless,
        // but unnecessary. Duplicate connections can be formed when the same layer name appears
        // more than once in the topology config file with the same "from" layer if the projected
        // rectangular or elliptical areas on the source layer overlap.
        double centerCol = (minCol + maxCol) / 2.0;
        double centerRow = (minRow + maxRow) / 2.0;
        int maxNumSourceNeurons = ((maxCol - minCol) + 1) * ((maxRow - minRow) + 1);

        for (int y = minRow; y <= maxRow; ++y) {
            for (int x = minCol; x <= maxCol; ++x) {
                if (!layerTo.isConvolutionLayer() && !layerTo.isRectangular() && elliptDist(centerCol - x, centerRow - y,
                        layerTo.getRadiusX(), layerTo.getRadiusY()) >= 1.0) {
                    continue; // Skip this location, it's outside the ellipse
                }

                if (layerTo.isConvolutionLayer() && layerTo.getConvolveMatrix().get(x - minCol, y - minRow) == 0.0) {
                    // Skip this connection because the convolve matrix weight is zero:
                    continue;
                }
                Neuron fromNeuron = getNeuron(x, y);

                if (fromNeuron.isForwardConnectedTo(toNeuron)) {
                    Neural2D.LOG.finer("Skipping a dupe connection from " + fromNeuron + " to " + toNeuron);
                    break; // Skip this connection, proceed to the next
                } else {
                    // Add a new Connection record to the main container of connections:
                    Connection conn = new Connection(fromNeuron, toNeuron);
                    fromNeuron.addForwardConnection(conn);
                    toNeuron.addBackConnection(conn);
                    //connections.add(conn);
                    numConns++;

                    // Initialize the weight of the connection:
                    if (layerTo.isConvolutionLayer()) {
                        conn.setWeight(layerTo.getConvolveMatrix().get(x - minCol, y - minRow));
                    } else {
                        //connections.back().weight = (randomDouble() - 0.5) / maxNumSourceNeurons;
                        conn.setWeight(((randomDouble() * 2.0) - 1.0) / Math.sqrt(maxNumSourceNeurons));
                    }
                }
            }
        }
        return numConns;
    }


    /**
     * Creates a neuron in this layer at the given x,y location and returns it.
     *
     * @param row
     * @param col
     * @return
     */
    protected Neuron createNeuron(int row, int col)
    {
        Neuron n;
        switch (getLayerType()) {
            case BIAS:
                n = new BiasNeuron(this);
                break;
            case INPUT:
                n = new InputNeuron(this, row, col);
                break;
            case OUTPUT:
                n = new OutputNeuron(getTransferFunction(), this, row, col);
                break;
            default: // Hidden
                n = new HiddenNeuron(getTransferFunction(), this, row, col);
                break;
        }
        return n;
    }

    @Override
    public int getNumColumns()
    {
        return params.getNumColumns();
    }

    @Override
    public int getNumRows()
    {
        return params.getNumRows();
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Matrix getConvolveMatrix()
    {
        return params.getConvolveMatrix();
    }

    @Override
    public Neuron getNeuron(int row, int col)
    {
        return neurons.get(row * getNumColumns() + col);
    }

    @Override
    public List<Neuron> getNeurons()
    {
        return Collections.unmodifiableList(neurons);
    }

    @Override
    public int size()
    {
        return numNeurons;
    }

    private static class ConnectionCountVisitor extends NetElementVisitor
    {

        private static final int FORWARD = 0;
        private static final int BACKWARD = 0;
        private final int dir;
        int count = 0;

        public ConnectionCountVisitor(int dir)
        {
            this.dir = dir;
        }

        @Override
        public boolean visit(Neuron neuron)
        {
            if (dir == FORWARD) {
                count += neuron.getNumForwardConnections();
            } else {
                count += neuron.getNumBackConnections();
            }
            return false;
        }

    }

    @Override
    public int getNumFwdConnections()
    {
        ConnectionCountVisitor v = new ConnectionCountVisitor(ConnectionCountVisitor.FORWARD);
        accept(v);
        return v.count;
    }

    @Override
    public int getNumBackConnections()
    {
        ConnectionCountVisitor v = new ConnectionCountVisitor(ConnectionCountVisitor.BACKWARD);
        accept(v);
        return v.count;
    }

    /**
     * If the NeuronCommand is parallelizable, the NeuronCommands may be
     * executed, once per Neuron, in any order, in parallel. Otherwise, the
     * NeuronActions will be executed, once per Neuron, in any order, serially.
     *
     * @param <T>
     * @param action
     * @return
     */
    @Override
    public <T> T executeCommand(Command<Neuron, T> action)
    {
        LayerTask<T> lAction = new LayerTask<>(action);
        JoinableResult<T> result = pool.invoke(lAction);
        if(result != null){
            return result.getResult();
        } else {
            return null;
        }
    }

    private class LayerTask<T> extends RecursiveTask<JoinableResult<T>>
    {

        private static final long serialVersionUID = 0L;
        private final int startCol, startRow;
        private final int numCols, numRows;
        private final int numSplits;
        private final Command<Neuron, T> action;

        public LayerTask(Command<Neuron, T> action)
        {
            this(action, 0, 0, getNumColumns(), getNumRows());
        }

        protected LayerTask(Command<Neuron, T> action, int startX, int startY, int lenX, int lenY)
        {
            this(action, startX, startY, lenX, lenY, 0);
        }

        protected LayerTask(Command<Neuron, T> action, int startX, int startY, int lenX, int lenY, int numSplits)
        {
            this.startCol = startX;
            this.startRow = startY;
            this.numCols = lenX;
            this.numRows = lenY;
            this.numSplits = numSplits;
            this.action = action;
        }

        private class LayerTaskVisitor extends NetElementVisitor
        {

            JoinableResult<T> result;

            @Override
            public boolean visit(Neuron n)
            {
                JoinableResult<T> res = action.execute(n);
                if (result == null) {
                    result = res;
                } else {
                    result.join(res);
                }
                return false;
            }
        }

        @Override
        protected JoinableResult<T> compute()
        {
            if (!action.canParallelize() || numCols * numRows < 128 /* && numSplits < numProcessors */) {
                LayerTaskVisitor v = new LayerTaskVisitor();
                accept(v);
                return v.result;
            } else {
                LayerTask<T> left, right;
                // First split horizontally, preserving rows, since
                // we secretly know that the Neurons are arranged
                // in the Layer by rows. Perhaps we should instead
                // ask the Layer to do the split?
                if (numRows > 1) {
                    int rowSplit = numRows / 2;
                    left = new LayerTask(action, startCol, startRow, numCols, rowSplit, numSplits + 1);
                    right = new LayerTask(action, startCol, startRow + rowSplit, numCols, numRows - rowSplit, numSplits + 1);
                } else {
                    // Split columns in this row, then
                    int colSplit = numCols / 2;
                    left = new LayerTask(action, startCol, startRow, colSplit, numRows, numSplits + 1);
                    right = new LayerTask(action, startCol + colSplit, startRow, numCols - colSplit, numRows, numSplits + 1);
                }
                left.fork();
                JoinableResult<T> result = right.compute();
                if (result == null) {
                    result = left.join();
                } else {
                    result.join(left.join());
                }
                return result;
            }
        }
    }
}
