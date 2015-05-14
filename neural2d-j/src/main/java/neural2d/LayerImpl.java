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
    private final List<Neuron> neurons; // 2d array, flattened index = row * numCols + col
    private final int numNeurons;
    private final ForkJoinPool pool = new ForkJoinPool();

    public LayerImpl(LayerConfig params)
    {
        this.params = params;
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
        return params.getLayerName();
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
