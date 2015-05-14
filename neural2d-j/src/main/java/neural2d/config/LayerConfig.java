package neural2d.config;

import java.util.ArrayList;
import java.util.List;
import neural2d.ColorChannel;
import neural2d.Matrix;
import neural2d.TransferFunction;
import static neural2d.TransferFunction.GAUSSIAN;
import static neural2d.TransferFunction.IDENTITY;
import static neural2d.TransferFunction.LOGISTIC;
import static neural2d.TransferFunction.RAMP;
import static neural2d.TransferFunction.TANH;

/**
 * Copyright (c) 2015 Michael C. Whidden
 *
 * @author Michael C. Whidden
 */
public class LayerConfig
{

    private String layerName;
    private final List<String> fromLayerNames = new ArrayList<>();
    private int numColumns, numRows;
    private ColorChannel channel; // applies only to input layer
    private int radiusX, radiusY;
    private boolean rectangular; // if the radius defines a rectangle, not an ellipse.
    private TransferFunction tf;
    private Matrix convolveMatrix;
    private boolean isClassifier;
    private final List<LayerConfig> fromLayerConfigs = new ArrayList<>();

    public void setTransferFunction(TransferFunction func)
    {
        tf = func;
    }

    public void setTransferFunction(String transferFunctionName)
    {
        if (transferFunctionName == null) {
            tf = TANH;
        } else {
            String tfName = transferFunctionName.toLowerCase();
            if (tfName.isEmpty() || tfName.equals("tanh")) {
                tf = TANH;
            } else if (tfName.equals("logistic")) {
                tf = LOGISTIC;
            } else if (tfName.equals("linear")) {
                // Allow 'linear' as an alias for 'identity'
                tf = IDENTITY;
            } else if (tfName.equals("ramp")) {
                tf = RAMP;
            } else if (tfName.equals("gaussian")) {
                tf = GAUSSIAN;
            } else if (tfName.equals("identity")) {
                tf = IDENTITY;
            } else {
                throw new IllegalArgumentException("No such transfer function: " + tfName);
            }
        }
    }

    public LayerConfig()
    {
        _clear();
    }

    public void clear()
    {
        _clear();
    }

    private void _clear()
    {
        layerName = null;
        fromLayerNames.clear();
        numColumns = numRows = 0;
        channel = null;
        radiusX = 1000000000;
        radiusY = 1000000000;
        tf = TANH;
        convolveMatrix = null;
    }

    public boolean isColorChannelSpecified()
    {
        return channel != null;
    }

    public boolean isConvolutionLayer()
    {
        return convolveMatrix != null;
    }

    public String getLayerName()
    {
        return layerName;
    }

    public void setLayerName(String layerName)
    {
        this.layerName = layerName;
    }

    /*public String getFromLayerName(int i)
     {
     return fromLayerNames.get(i);
     }*/
    public void addFromLayerName(String fromLayerName)
    {
        fromLayerNames.add(fromLayerName);
    }

    List<String> getFromLayerNames()
    {
        return fromLayerNames;
    }

    public List<LayerConfig> getFromLayerConfigs()
    {
        return fromLayerConfigs;
    }

    public void addFromLayerConfig(LayerConfig l)
    {
        fromLayerConfigs.add(l);
    }

    public int getNumFromLayers()
    {
        return fromLayerNames.size();
    }

    public int getNumRows()
    {
        return numRows;
    }

    public int getNumColumns()
    {
        return numColumns;
    }

    public void setSize(int rows, int cols)
    {
        this.numColumns = cols;
        this.numRows = rows;
    }

    public ColorChannel getChannel()
    {
        return channel;
    }

    public void setChannel(ColorChannel channel)
    {
        this.channel = channel;
    }

    public int getRadiusX()
    {
        return radiusX;
    }

    public int getRadiusY()
    {
        return radiusY;
    }

    public boolean isRectangular()
    {
        return rectangular;
    }

    public void setRadius(int radiusX, int radiusY)
    {
        this.radiusX = radiusX;
        this.radiusY = radiusY;
    }

    public TransferFunction getTransferFunction()
    {
        return tf;
    }

    public Matrix getConvolveMatrix()
    {
        return convolveMatrix;
    }

    public void setConvolveMatrix(Matrix convolveMatrix)
    {
        this.convolveMatrix = convolveMatrix;
    }

    void setRectangular(boolean b)
    {
        this.rectangular = b;
    }

    public boolean isClassifier()
    {
        return isClassifier;
    }

    public void setClassifier(boolean b)
    {
        this.isClassifier = b;
    }
}
