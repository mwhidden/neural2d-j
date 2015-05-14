package neural2d.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import neural2d.ColorChannel;
import neural2d.Matrix;
import static neural2d.Neural2D.LOG;
import neural2d.TransferFunction;
import static neural2d.config.XMLConfig.getChildElements;
import org.w3c.dom.Node;

/**
 * <p>
 * <p>
 * <p>
 * Copyright (c) 2015 Michael C. Whidden
 *
 * @author Michael C. Whidden
 */
public class TopologyConfig extends XMLConfig
{

    List<LayerConfig> layers;

    public TopologyConfig(Node topoNode) throws ConfigurationException
    {
        layers = new ArrayList<>();
        boolean foundInput = false, foundOutput = false;
        if (!parseNode(topoNode, foundInput, foundOutput)) {
            throw new ConfigurationException("Both input and output layers are required.");
        }

        // Layers come back with input at the front, output at the back,
        // and hidden layers in arbitrary order in between.
        // Validations
        // Input layer cannot be a convolution layer
        if (layers.get(0).isConvolutionLayer()) {
            throw new ConfigurationException("Input layer may not be a convolution layer.");
        }

        // Input layer must not have a 'from' layer
        if (layers.get(0).getNumFromLayers() != 0) {
            throw new ConfigurationException("Input layer may not have a 'from' property.");
        }

        for (int i = 0; i < layers.size(); i++) {
            LayerConfig layer = layers.get(i);

            // No layer except the input layer can have a color channel.
            if (i > 0 && layer.isColorChannelSpecified()) {
                throw new ConfigurationException("Only the input layer may specify a color channel.");
            }

            // All layers must have a layer name
            if (layer.getLayerName() == null) {
                throw new ConfigurationException("All layers must have a name.");
            }

            // All layers except input layer must have a valid 'from' layer
            // that exists.
            if (i > 0) {
                for (String fromLayer : layer.getFromLayerNames()) {
                    int idx = findLayer(layers, fromLayer);
                    if (idx < 0) {
                        throw new ConfigurationException("'" + layer.getLayerName() + "' layer must specify a valid 'from' layer. 'from' layer was '" + fromLayer + "'");
                    } else {
                        layer.addFromLayerConfig(layers.get(idx));
                    }
                }
            }
        }
        sortLayers(layers);
    }

    private static class Edge
    {

        LayerConfig from, to;

        public Edge(LayerConfig from, LayerConfig to)
        {
            this.from = from;
            this.to = to;
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            return hash;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Edge other = (Edge) obj;
            if (!Objects.equals(this.from, other.from)) {
                return false;
            }
            return Objects.equals(this.to, other.to);
        }
    }

    /**
     * @param layerConfigs
     */
    static final void sortLayers(List<LayerConfig> layerConfigs) throws ConfigurationException
    {

        // Sort the layers in the order of forward propagation
        // Since all layers except the input must have a previous
        // layer, and since all previous layers exist, we can order the layers.
        // Since the previous layer may not be unique (one forward layer
        // may have two 'from' layers, this is only a partial ordering.
        /*
         // Since this is only needed during initialization, use a naive
         // sort.
         // Swap layers until sorted. TODO: detect cycles
         showLayerList(layerConfigs);
         boolean swapped;
         do {
         swapped = false;
         for(int i=0; i < layerConfigs.size()-1; i++){
         LayerConfig l0 = layerConfigs.get(i);
         LayerConfig l1 = layerConfigs.get(i+1);
         for(int j=0; j < l0.getNumFromLayers(); j++){
         String fromName = l0.getFromLayerName(j);
         if(fromName.equals(l1.getLayerName())){
         Collections.swap(layerConfigs, i, i+1);
         System.out.print("Swapped " + i + " and "+ (i+1) + " for ");
         showLayerList(layerConfigs);
         swapped = true;
         break;
         }
         }
         }
         }while(swapped);
         for (int i = layerConfigs.size() - 1; i > 0; i--) {

         LayerConfig layer = layerConfigs.get(i);
         for (String fromLayerName : layer.getFromLayers()) {
         // Remove the fromLayer from its current location and
         // insert it just before the current layer. This ensures
         // the layers are in order of forward feeding, but
         // since it is only a partial order, there are many
         // such ordering. This gets us a valid one.
         int j = findLayer(layerConfigs, fromLayerName);
         if(j < 0){
         throw new ConfigurationException("'" + layer.getLayerName()
         + "' layer must specify a valid 'from' layer. 'from' layer was '"
         + fromLayerName + "'");
         }
         // Insert the jth layer at the position i-1
         if (j != i - 1) {
         // check for cycles
         if(j > i){
         // If the referenced layer is earlier in the sorted
         // list, then there is a cycle.
         throw new ConfigurationException("Topology has a cycle involving layer " + fromLayerName);
         }
         LayerConfig l = layerConfigs.get(j);
         layerConfigs.add(i, l);
         layerConfigs.remove(j);
         }
         }
         }*/
        // Use Kahn (1962) algorithm to topologically sort the DAG.
        List<LayerConfig> noIncoming = new ArrayList<>();
        List<LayerConfig> sortedList = new ArrayList<>();
        Map<Edge, Object> removedEdges = new HashMap<>();
        addNoIncoming(noIncoming, layerConfigs);
        if (noIncoming.size() > 1) {
            throw new ConfigurationException("All non-input layers must have a from layer.");
        }
        while (!noIncoming.isEmpty()) {
            LayerConfig n = noIncoming.remove(0);
            sortedList.add(n);
            for (LayerConfig cfg : layerConfigs) {
                Iterator<LayerConfig> iter = cfg.getFromLayerConfigs().iterator();
                boolean hadEdge = false;
                int edgeCount = 0;
                while (iter.hasNext()) {
                    LayerConfig fromConfig = iter.next();
                    Edge e = new Edge(fromConfig, cfg);
                    if (!removedEdges.containsKey(e)) {
                        edgeCount++;
                    }
                    if (fromConfig.equals(n)) {
                        if (!removedEdges.containsKey(e)) {
                            hadEdge = true;
                            removedEdges.put(e, null);
                            edgeCount--;
                        }
                    }

                }

                // If we just made a change that brought this node down
                // to 0 edges, add it to the noIncoming list.
                if (hadEdge && edgeCount == 0) {
                    // Only nodes that previously had an edge from
                    // this node are eligible to be added to the set
                    // of nodes with no incoming edges.
                    noIncoming.add(cfg);
                }
            }
        }
        for (LayerConfig cfg : layerConfigs) {
            // If any edges remain, there is a cycle.
            for (LayerConfig fromCfg : cfg.getFromLayerConfigs()) {
                Edge e = new Edge(fromCfg, cfg);
                if (!removedEdges.containsKey(e)) {
                    throw new ConfigurationException("Network topology has a cycle.");
                }
            }

        }
        // Sort in place
        layerConfigs.clear();
        layerConfigs.addAll(sortedList);
    }

    private static void addNoIncoming(List<LayerConfig> noIncoming, List<LayerConfig> layerConfigs)
    {
        for (LayerConfig cfg : layerConfigs) {
            if (cfg.getNumFromLayers() == 0) {
                noIncoming.add(cfg);
            }
        }
    }

    /**
     * Returns true if both an input and output layer were found.
     *
     * @param topoNode
     * @param foundInput
     * @param foundOutput
     * @return
     * @throws ConfigurationException
     */
    private boolean parseNode(Node topoNode, boolean foundInput, boolean foundOutput) throws ConfigurationException
    {
        for (Node node : getChildElements(topoNode)) {
            LayerConfig layerConfig;
            switch (node.getNodeName()) {
                case "input":
                    if (foundInput) {
                        throw new ConfigurationException("Only one input layer may be specified.");
                    }
                    foundInput = true;
                    break;
                case "layers":
                    parseNode(node, foundInput, foundOutput);
                    break;
                case "layer":
                    if (!foundInput || foundOutput) {
                        throw new ConfigurationException("<layers> must appear between the <input> and <output> nodes.");
                    }
                    break;
                case "output":
                    if (foundOutput) {
                        throw new ConfigurationException("Only one output layer may be specified.");
                    }
                    foundOutput = true;
                    break;
                default:
                    throw new ConfigurationException("Illegal entity in <topology> node.");
            }
            layerConfig = processLayerNode(node);
            switch (node.getNodeName()) {
                case "input":
                    // Make input node the first node.
                    layers.add(0, layerConfig);
                    // Default color channel for input layer is BW
                    // if not specified.
                    if (layerConfig.isColorChannelSpecified()) {
                        layerConfig.setChannel(ColorChannel.BW);
                    }
                    break;
                case "layer":
                    // Make output node the last node. IF already present,
                    // it will stay at the end.
                    if (layers.size() > 0) {
                        // Never add a hidden layer at the front or back
                        // unless there are no layers yet, or only one layer.
                        // this keeps the input layer at the front, and the
                        // output layer at the end.
                        layers.add(1, layerConfig);
                    }
                    break;
                case "output":
                    // Make output node the last node
                    layers.add(layerConfig);
                    break;
                default:
                    // Ignore unknown tags
                    LOG.warning("Unknown tag " + node.getNodeName()
                            + " ignored in config file.");

            }
        }

        return foundInput && foundOutput;
    }

    static int findLayer(List<LayerConfig> list, String name)
    {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getLayerName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    static int findLayer(List<LayerConfig> list, LayerConfig layer)
    {
        return list.indexOf(layer);
    }

    public LayerConfig getInputLayerConfig()
    {
        return layers.get(0);
    }

    public LayerConfig getOutputLayerConfig()
    {
        return layers.get(layers.size() - 1);
    }

    /**
     * Return all the intermediate layers in order of forward propagation.
     *
     * @return all the intermediate layers in order of forward propagation.
     */
    public List<LayerConfig> getHiddenLayerConfig()
    {
        return Collections.unmodifiableList(layers.subList(1, layers.size() - 1));
    }

    private LayerConfig processLayerNode(Node node) throws ConfigurationException
    {
        LayerConfig layerConfig = new LayerConfig();
        int twoNums[];
        boolean tfSpecified = false;

        String attVal = getAttribute(node, "classifier");
        if (attVal != null && Boolean.parseBoolean(attVal)) {
            layerConfig.setClassifier(true);
        }

        for (Node childNode : getChildElements(node)) {
            String content;
            switch (childNode.getNodeName()) {
                case "size":
                    content = getNodeContent(childNode);
                    try {
                        twoNums = extractTwoNums(content);
                    } catch (NumberFormatException e) {
                        throw new ConfigurationException("Bad XxY format in config file. " + content);
                    }
                    layerConfig.setSize(twoNums[0], twoNums[1]);
                    break;
                case "from":
                    content = getNodeContent(childNode);
                    layerConfig.addFromLayerName(content);
                    break;
                case "name":
                    content = getNodeContent(childNode);
                    layerConfig.setLayerName(content);
                    break;
                case "radius":
                    content = getNodeContent(childNode);
                    try {
                        twoNums = extractTwoNums(content);
                    } catch (NumberFormatException e) {
                        throw new ConfigurationException("Bad XxY format in config file. " + content);
                    }
                    layerConfig.setRadius(twoNums[0], twoNums[1]);
                    content = getAttribute(childNode, "rectangular");
                    if (Boolean.parseBoolean(content)) {
                        layerConfig.setRectangular(true);
                    }
                    break;
                case "channel":
                    content = getNodeContent(childNode);
                    try {
                        layerConfig.setChannel(ColorChannel.valueOf(childNode.getNodeValue()));
                    } catch (IllegalArgumentException e) {
                        throw new ConfigurationException("Bad color channel '" + content + "' in config file.");
                    }
                    break;
                case "convolve":
                    layerConfig.setConvolveMatrix(parseConvolveMatrix(childNode));
                    if (tfSpecified
                            && layerConfig.getTransferFunction() != TransferFunction.IDENTITY) {
                        // Why?
                        throw new ConfigurationException("Convolution layers must have 'identity' transfer function.");
                    }
                    layerConfig.setTransferFunction(TransferFunction.IDENTITY);
                    break;
                case "tf":
                    content = getNodeContent(childNode);
                    try {
                        layerConfig.setTransferFunction(content);
                    } catch (IllegalArgumentException e) {
                        throw new ConfigurationException("Bad transfer function name '" + content + "' in config file.");
                    }
                    if (layerConfig.isConvolutionLayer()
                            && layerConfig.getTransferFunction() != TransferFunction.IDENTITY) {
                        // Why?
                        throw new ConfigurationException("Convolution layers must have 'identity' transfer function.");
                    }
                    tfSpecified = true;
                    break;
                default:
                    // Ignore unknown tags
                    LOG.warning("Unknown tag " + node.getNodeName()
                            + " ignored in config file.");
            }
        }

        if (layerConfig.getLayerName() != null
                && findLayer(layers, layerConfig.getLayerName()) >= 0) {
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
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Did not find numeric rows and cols attributes of <convolve> tag.");
        }

        if (rows <= 0 || cols <= 0) {
            throw new ConfigurationException("Convolution matrix size must be greater than 0.");
        }

        Matrix matrix = new Matrix(rows, cols);
        String data = childNode.getNodeValue();
        if (data == null) {
            throw new ConfigurationException("Convolution matrix is missing matrix elements.");
        }
        matrix.load(MatrixConfig.parse(data, rows, cols));
        return matrix;
    }
}
