package neural2d;

import java.util.List;

/**
 * <p>
 * Copyright (c) 2015 Michael C. Whidden
 *
 * @author Michael C. Whidden
 */
public interface Layer extends NetElement
{

    /**
     * Populates this layer with the proper type and number of neurons.
     */
    void createNeurons();

    ColorChannel getChannel();

    Matrix getConvolveMatrix();

    Command<Neuron, Double> getFeedForwardCommand(Matrix inputData);

    LayerType getLayerType();

    String getName();

    Neuron getNeuron(int row, int col);

    List<Neuron> getNeurons();

    int getNumBackConnections();

    int getNumColumns();

    int getNumFwdConnections();

    int getNumRows();

    /**
     * Each neuron in this layer will be connected to one or more neurons in the
     * <i>previous</i> layer which lie in an ellipse or rectangle centered on a
     * neuron in the previous layer whose x,y location matches the neuron in
     * this layer, with a width of twice <code>getRadiusX</code>
     *
     * @return
     */
    int getRadiusX();

    /**
     * Each neuron in this layer will be connected to one or more neurons in the
     * <i>previous</i> layer which lie in an ellipse or rectangle centered on a
     * neuron in the previous layer whose x,y location matches the neuron in
     * this layer, with a height of twice <code>getRadiusY</code>
     *
     * @return
     */
    int getRadiusY();

    TransferFunction getTransferFunction();

    /**
     * When true on an output layer, the output neuron with the greatest value
     * will be forced to 1.0, and all other neurons will be forced to -1.0.
     *
     * @return
     */
    boolean isClassifier();

    boolean isConvolutionLayer();

    /**
     * If true, then <code>getRadiusX</code> and <code>getRadiusY</code> specify
     * the width and height of a rectangular region. If false, they specify the
     * width and height of an ellipse.
     *
     * @return
     */
    boolean isRectangular();

    int size();

    /**
     * If the NeuronCommand is parallelizable, the NeuronCommands may be
     * executed, once per Neuron, in any order, in parallel. Otherwise, the
     * NeuronActions will be executed, once per Neuron, in any order, serially.
     *
     * @param <T>
     * @param action
     * @return
     */
    <T> T executeCommand(Command<Neuron, T> action);

    Command<Neuron, Double> getCalculateGradientsCommand(Matrix targeVals);

    public Matrix getOutput();

    static class AccumulateForwardWeights implements Command<Layer, Double>
    {

        @Override
        public Command.DoubleResult execute(Layer l)
        {
            // Skip the bias layer
            if (l.getLayerType() != LayerType.BIAS) {
                Neuron.AccumulateSquareWeightsCommand cmd
                        = new Neuron.AccumulateSquareWeightsCommand();
                l.executeCommand(cmd);
                return new Command.DoubleResult(l.executeCommand(cmd));
            } else {
                return new Command.DoubleResult(0.0);
            }
        }

        @Override
        public boolean canParallelize()
        {
            return true;
        }
    }
}
