package neural2d.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import neural2d.ColorChannel;
import neural2d.Matrix;
import neural2d.TransferFunction;
import static neural2d.config.XMLConfig.getChildElements;
import org.w3c.dom.Node;

/**
 * <p>
 * <p>
 * <p>
 * Copyright (c) 2015 Michael C. Whidden
 * @author Michael C. Whidden
 */
public class TopologyConfig extends XMLConfig
{
    List<LayerConfig> layers = new ArrayList<>();

    public TopologyConfig(Node topoNode) throws ConfigurationException
    {
        boolean foundInput = false, foundOutput = false;
        if(!parseNode(topoNode, foundInput, foundOutput)){
            throw new ConfigurationException("Both input and output layers are required.");
        }

        // Validations

        // Input layer cannot be a convolution layer
        if(layers.get(0).isConvolutionLayer()){
            throw new ConfigurationException("Input layer may not be a convolution layer.");
        }

        // Input layer must not have a 'from' layer
        if(layers.get(0).getFromLayerName() != null){
            throw new ConfigurationException("Input layer may not have a 'from' property.");
        }

        for(int i=0; i < layers.size(); i++){
            LayerConfig layer = layers.get(i);

            // No layer except the input layer can have a color channel.
            if(i > 0 && layer.isColorChannelSpecified()){
                throw new ConfigurationException("Only the input layer may specify a color channel.");
            }

            // All layers must have a layer name
            if(layer.getLayerName() == null){
                throw new ConfigurationException("All layers must have a name.");
            }

            // All layers except input layer must have a valid 'from' layer
            // that exists.
            if(i > 0 && findLayer(layer.getFromLayerName()) < 0){
                throw new ConfigurationException("'" + layer.getLayerName() + "' layer must specify a valid 'from' layer. 'from' layer was '"+layer.getFromLayerName());
            }

        }

        // Sort the layers in the order of forward propagation
        // Since all layers except the input must have a previous
        // layer, and since all previous layers exist and are
        // unique, we can order the layers.
        for(int i=layers.size()-1; i > 0; i--){
            int j = findLayer(layers.get(i).getFromLayerName());
            // Move the jth layer to the position i-1
            if(j != i-1){
                Collections.swap(layers, i-1, j);
            }
        }
    }

    /**
     * Returns true if both an input and output layer were found.
     * @param topoNode
     * @param foundInput
     * @param foundOutput
     * @return
     * @throws ConfigurationException
     */
    private boolean parseNode(Node topoNode, boolean foundInput, boolean foundOutput) throws ConfigurationException
    {
        for(Node node: getChildElements(topoNode)){
            LayerConfig layerConfig;
            switch(node.getNodeName()){
                case "input":
                    if(foundInput){
                        throw new ConfigurationException("Only one input layer may be specified.");
                    }
                    foundInput = true;
                    break;
                case "layers":
                    parseNode(node, foundInput, foundOutput);
                    break;
                case "layer":
                    if(!foundInput || foundOutput){
                        throw new ConfigurationException("<layers> must appear between the <input> and <output> nodes.");
                    }
                    break;
                case "output":
                    if(foundOutput){
                        throw new ConfigurationException("Only one output layer may be specified.");
                    }
                    foundOutput = true;
                    break;
                default:
                    throw new ConfigurationException("Illegal entity in <topology> node.");
            }
            layerConfig = processLayerNode(node);
            switch(node.getNodeName()){
                case "input":
                   layers.add(0, layerConfig);
                   // Default color channel for input layer is BW
                   // if not specified.
                   if(layerConfig.isColorChannelSpecified()){
                       layerConfig.setChannel(ColorChannel.BW);
                   }
                   break;
                case "layer":
                   layers.add(layerConfig);
                    break;
                case "output":
                    layers.add(layerConfig);
                    break;
            }
        }

        return foundInput && foundOutput;
    }


    private int findLayer(String name)
    {
        for(int i=0; i < layers.size(); i++){
           if(layers.get(i).getLayerName().equals(name)){
               return i;
           }
        }
        return -1;
    }

    public LayerConfig getInputLayerConfig()
    {
        return layers.get(0);
    }

    public LayerConfig getOutputLayerConfig()
    {
        return layers.get(layers.size()-1);
    }

    /**
     * Return all the intermediate layers in order of
     * forward propagation.
     * @return all the intermediate layers in order of
     * forward propagation.
     */
    public List<LayerConfig> getHiddenLayerConfig()
    {
        return Collections.unmodifiableList(layers.subList(1, layers.size()-1));
    }


    private LayerConfig processLayerNode(Node node) throws ConfigurationException
    {
        LayerConfig layerConfig = new LayerConfig();
        int twoNums[] = new int[2];
        boolean tfSpecified = false;

        String attVal = getAttribute(node, "classifier");
        if(attVal != null && Boolean.parseBoolean(attVal)){
            layerConfig.setClassifier(true);
        }

        for(Node childNode: getChildElements(node)){
            String content;
            switch(childNode.getNodeName()){
                case "size":
                    content = getNodeContent(childNode);
                    try {
                        twoNums = extractTwoNums(content);
                    } catch (NumberFormatException e){
                        throw new ConfigurationException("Bad XxY format in config file. " + content);
                    }
                    layerConfig.setSize(twoNums[0],twoNums[1]);
                    break;
                case "from":
                    // TODO: allow a layer to receive input
                    // from multiple previous layers.
                    content = getNodeContent(childNode);
                    layerConfig.setFromLayerName(content);
                    break;
                case "name":
                    content = getNodeContent(childNode);
                    layerConfig.setLayerName(content);
                    break;
                case "radius":
                    content = getNodeContent(childNode);
                    try {
                        twoNums = extractTwoNums(content);
                    } catch (NumberFormatException e){
                        throw new ConfigurationException("Bad XxY format in config file. " + content);
                    }
                    layerConfig.setRadius(twoNums[0],twoNums[1]);
                    content = getAttribute(childNode, "rectangular");
                    if(Boolean.parseBoolean(content)){
                        layerConfig.setRectangular(true);
                    }
                    break;
                case "channel":
                    content = getNodeContent(childNode);
                    try {
                        layerConfig.setChannel(ColorChannel.valueOf(childNode.getNodeValue()));
                    } catch (IllegalArgumentException e){
                        throw new ConfigurationException("Bad color channel '" + content + "' in config file.");
                    }
                    break;
                case "convolve":
                    layerConfig.setConvolveMatrix(parseConvolveMatrix(childNode));
                    if(tfSpecified
                            &&
                        layerConfig.getTransferFunction() != TransferFunction.IDENTITY){
                        // Why?
                        throw new ConfigurationException("Convolution layers must have 'identity' transfer function.");
                    }
                    layerConfig.setTransferFunction(TransferFunction.IDENTITY);
                    break;
                case "tf":
                    content = getNodeContent(childNode);
                    try {
                        layerConfig.setTransferFunction(content);
                    } catch (IllegalArgumentException e){
                        throw new ConfigurationException("Bad transfer function name '" + content + "' in config file.");
                    }
                    if(layerConfig.isConvolutionLayer()
                            &&
                        layerConfig.getTransferFunction() != TransferFunction.IDENTITY){
                        // Why?
                        throw new ConfigurationException("Convolution layers must have 'identity' transfer function.");
                    }
                    tfSpecified = true;
                    break;
            }
        }

        if(layerConfig.getLayerName() != null
                &&
            findLayer(layerConfig.getLayerName()) >= 0){
            throw new ConfigurationException("Each layer must have a unique name. Name '" + layerConfig.getLayerName() + "' appears more than once.");
        }

        return layerConfig;
    }

    private static Matrix parseConvolveMatrix(Node childNode) throws ConfigurationException
    {
        int rows, cols;
        try {
            rows = Integer.parseInt(getAttribute(childNode, "rows"));
            cols = Integer.parseInt(getAttribute(childNode, "cols"));
        } catch (NumberFormatException e){
            throw new ConfigurationException("Did not find numeric rows and cols attributes of <convolve> tag.");
        }

        if(rows <=0 || cols <= 0){
            throw new ConfigurationException("Convolution matrix size must be greater than 0.");
        }

        Matrix matrix = new Matrix(rows, cols);
        String data = childNode.getNodeValue();
        if(data == null){
            throw new ConfigurationException("Convolution matrix is missing matrix elements.");
        }
        matrix.load(MatrixConfig.parse(data, rows, cols));
        return matrix;
    }
}