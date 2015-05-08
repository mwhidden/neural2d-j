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
 * @author Michael C. Whidden
 */
public abstract class Layer implements NetElement
{
    private final LayerConfig params;
    private final List<Neuron> neurons; // 2d array, flattened index = y * sizeX + x
    private final int numNeurons;
    private final ForkJoinPool pool = new ForkJoinPool();

    public Layer(LayerConfig params)
    {
        this.params = params;
        this.numNeurons = params.getNumRows() * params.getNumColumns();
        this.neurons = new ArrayList<>(numNeurons);
        for(int i=0; i < numNeurons; i++){
            neurons.add(null);
        }
    }

    public abstract LayerType getLayerType();

    @Override
    public void accept(NetElementVisitor v)
    {
        if(v.visit(this)){
            for(Neuron neuron: neurons){
                neuron.accept(v);
            }
        }
    }

    public TransferFunction getTransferFunction()
    {
        return params.getTransferFunction();
    }

    public boolean isConvolutionLayer()
    {
        return params.isConvolutionLayer();
    }

    /**
     * Each neuron in this layer will be connected to one or more neurons
     * in the <i>previous</i> layer which lie in an ellipse or rectangle
     * centered on a neuron in the previous layer whose x,y location
     * matches the neuron in this layer, with a width of twice
     * <code>getRadiusX</code>
     */
    public int getRadiusX()
    {
        return params.getRadiusX();
    }

    /**
     * Each neuron in this layer will be connected to one or more neurons
     * in the <i>previous</i> layer which lie in an ellipse or rectangle
     * centered on a neuron in the previous layer whose x,y location
     * matches the neuron in this layer, with a height of twice
     * <code>getRadiusY</code>
     */
    public int getRadiusY()
    {
        return params.getRadiusY();
    }

    /**
     * If true, then <code>getRadiusX</code> and <code>getRadiusY</code>
     * specify the width and height of a rectangular region. If false,
     * they specify the width and height of an ellipse.
     * @return
     */
    public boolean isRectangular()
    {
        return params.isRectangular();
    }

    /**
     * When true on an output layer, the output neuron with the
     * greatest value will be forced to 1.0, and all other neurons
     * will be forced to -1.0.
     *
     * @return
     */
    public boolean isClassifier()
    {
        return params.isClassifier();
    }

    public ColorChannel getChannel()
    {
        return params.getChannel();
    }

    /**
     * Creates a neuron in this layer at the given x,y location and returns it.
     * @param row
     * @param col
     * @param tf
     * @return
     */
    public Neuron createNeuron(int row, int col, TransferFunction tf)
    {
        Neuron n = new NeuronImpl(tf, this, row, col );
        addNeuron(n, row, col);
        return n;
    }

    public int getNumColumns()
    {
        return params.getNumColumns();
    }

    public int getNumRows()
    {
        return params.getNumRows();
    }

    public String getName()
    {
        return params.getLayerName();
    }

    public Matrix getConvolveMatrix()
    {
        return params.getConvolveMatrix();
    }


    public void addNeuron(Neuron neuron, int row, int col)
    {
        if(row >= getNumRows() || col >= getNumColumns() || row < 0 || col < 0){
            throw new ArrayIndexOutOfBoundsException("Invalid location for adding neuron. "
                    + "Row was " + row + ", column was " + col + " and size is " + getNumRows() + ", " + getNumColumns());
        }
        neurons.set(row*getNumColumns() + col, neuron);
    }

    public Neuron getNeuron(int row, int col)
    {
        return neurons.get(row*getNumColumns() + col);
    }

    public List<Neuron> getNeurons()
    {
        return Collections.unmodifiableList(neurons);
    }

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
            if(dir == FORWARD){
                count += neuron.getNumForwardConnections();
            } else {
                count += neuron.getNumBackConnections();
            }
            return false;
        }

    }

    public int getNumFwdConnections()
    {
        ConnectionCountVisitor v = new ConnectionCountVisitor(ConnectionCountVisitor.FORWARD);
        accept(v);
        return v.count;
    }


    public int getNumBackConnections()
    {
        ConnectionCountVisitor v = new ConnectionCountVisitor(ConnectionCountVisitor.BACKWARD);
        accept(v);
        return v.count;
    }

    /**
     * If the NeuronCommand is parallelizable, the NeuronCommands may be
     * executed, once per Neuron, in any order, in parallel. Otherwise,
     * the NeuronActions will be executed, once per Neuron, in any order,
     * serially.
     * @param <T>
     * @param action
     * @return
     */
    public <T> T executeCommand(Command<Neuron,T> action)
    {
        LayerTask<T> lAction = new LayerTask<>(action);
        return pool.invoke(lAction).getResult();
    }

    static class AccumulateForwardWeights implements Command<Layer,Double>
    {
        @Override
        public DoubleResult execute(Layer l)
        {
            // Skip the bias layer
            if(l.getLayerType() != LayerType.BIAS){
                Neuron.AccumulateSquareWeightsCommand cmd =
                        new Neuron.AccumulateSquareWeightsCommand();
                return new DoubleResult(l.executeCommand(cmd));
            } else {
                return new DoubleResult(0.0);
            }
        }

        @Override
        public boolean canParallelize()
        {
            return true;
        }
    }

    private class LayerTask<T> extends RecursiveTask<JoinableResult<T>>
    {
        private final int startCol, startRow;
        private final int numCols, numRows;
        private final int numSplits;
        private final Command<Neuron,T> action;

        public LayerTask(Command<Neuron,T> action)
        {
            this(action, 0, 0, getNumColumns(), getNumRows());
        }

        protected LayerTask(Command<Neuron,T> action, int startX, int startY, int lenX, int lenY)
        {
            this(action, startX, startY, lenX, lenY, 0);
        }

        protected LayerTask(Command<Neuron,T> action, int startX, int startY, int lenX, int lenY, int numSplits)
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
            public boolean visit(Neuron n){
                JoinableResult<T> res = action.execute(n);
                if(result == null){
                    result = res;
                } else {
                    result.join(res.getResult());
                }
                return false;
            }
        }

        @Override
        protected JoinableResult<T> compute()
        {
            if(!action.canParallelize() || numCols*numRows < 128 /* && numSplits < numProcessors */){
                LayerTaskVisitor v = new LayerTaskVisitor();
                accept(v);
                return v.result;
            } else {
                LayerTask<T> left, right;
                // First split horizontally, preserving rows, since
                // we secretly know that the Neurons are arranged
                // in the Layer by rows. Perhaps we should instead
                // ask the Layer to do the split?
                if(numRows > 1){
                    int rowSplit = numRows/2;
                    left = new LayerTask(action, startCol, startRow, numCols, rowSplit, numSplits+1);
                    right = new LayerTask(action, startCol, startRow+rowSplit, numCols, numRows-rowSplit, numSplits+1);
                } else {
                    // Split columns in this row, then
                    int colSplit = numCols/2;
                    left = new LayerTask(action, startCol, startRow, colSplit, numRows, numSplits+1);
                    right = new LayerTask(action, startCol+colSplit, startRow, numCols-colSplit, numRows, numSplits+1);
                }
                left.fork();
                JoinableResult<T> result = right.compute();
                result.join(left.join().getResult());
                return result;
            }
        }
    }

    public static class HiddenLayer extends Layer
    {
        public HiddenLayer(LayerConfig params)
        {
            super(params);
        }

        @Override
        public LayerType getLayerType()
        {
            return LayerType.HIDDEN;
        }

    }

    public static class InputLayer extends Layer
    {
        public InputLayer(LayerConfig params)
        {
            super(params);
        }

        @Override
        public LayerType getLayerType()
        {
            return LayerType.INPUT;
        }

    }

    public static class OutputLayer extends Layer
    {
        public OutputLayer(LayerConfig params)
        {
            super(params);
        }

        @Override
        public LayerType getLayerType()
        {
            return LayerType.OUTPUT;
        }

    }

    public static class BiasLayer extends Layer
    {
        public BiasLayer(LayerConfig params)
        {
            super(params);
        }

        @Override
        public LayerType getLayerType()
        {
            return LayerType.BIAS;
        }

    }}
