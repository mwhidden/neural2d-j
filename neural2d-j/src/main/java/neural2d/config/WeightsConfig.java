package neural2d.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import neural2d.Connection;
import neural2d.Layer;
import neural2d.LayerType;
import neural2d.Net;
import neural2d.NetElementVisitor;
import neural2d.Neuron;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <p>
 *
 * <p>
 *
 * <p>
 * Copyright (c) 2015 Michael C. Whidden
 *
 * @author Michael C. Whidden
 */
public class WeightsConfig extends XMLConfig
{

    private final Map<String, LayerWeights> layerWeights
            = new HashMap<>();

    public void set(String layer, int row, int col,
            String toLayer, int toRow, int toCol, double val)
    {
        LayerWeights lW = layerWeights.get(layer);
        if (lW == null) {
            lW = new LayerWeights();
            layerWeights.put(layer, lW);
        }
        NeuronWeights nW = lW.get(row, col);
        if (nW == null) {
            nW = new NeuronWeights();
            lW.set(row, col, nW);
        }
        nW.set(toLayer, toRow, toCol, val);
    }

    private static class LayerWeights extends ListMatrix<NeuronWeights>
    {
    }

    private static class NeuronWeights
    {

        private final Map<String, ListMatrix<Double>> weights
                = new HashMap<>();

        public double get(String toLayer,
                int toRow,
                int toCol)
        {
            ListMatrix<Double> m = weights.get(toLayer);
            if (m == null) {
                throw new IllegalArgumentException("Weight configuration has no such layer named: "
                        + toLayer);
            }
            return m.get(toRow, toCol);
        }

        boolean exists(String toLayer,
                int toRow,
                int toCol)
        {
            ListMatrix<Double> m = weights.get(toLayer);
            if (m == null) {
                return false;
            }
            try {
                return m.get(toRow, toCol) != null;
            } catch (IndexOutOfBoundsException e) {
                // Means it doesn't exist
                return false;
            }
        }

        public void set(String toLayer,
                int toRow,
                int toCol,
                double val)
        {
            ListMatrix<Double> m = weights.get(toLayer);
            if (m == null) {
                m = new ListMatrix<>();
                weights.put(toLayer, m);
            }
            m.set(toRow, toCol, val);
        }
    }

    private static class ListMatrix<T>
    {

        private final List<List<T>> m = new ArrayList<>();

        public T get(int row, int col)
        {
            return m.get(row).get(col);
        }

        public void set(int row, int col, T val)
        {
            List<T> rowList;
            while (row >= m.size()) {
                m.add(new ArrayList<T>());
            }
            rowList = m.get(row);
            while (col >= rowList.size()) {
                rowList.add(null);
            }
            rowList.set(col, val);
        }
    }

    public double getWeight(String layer, int row, int col,
            String toLayer, int toRow, int toCol)
    {
        return layerWeights.get(layer).get(row, col).get(toLayer, toRow, toCol);
    }

    public static WeightsConfig parseConfig(String configFilename) throws ConfigurationException
    {
        Document dom = getDocument(configFilename,
                "<?xml version=\"1.0\"?>\n"
                + "<!DOCTYPE weights [\n"
                + "]>\n");

        Element netElem = dom.getDocumentElement();

        return new WeightsConfig(netElem);
    }

    public WeightsConfig(Node parentNode) throws ConfigurationException
    {
        for (Node node : getChildElements(parentNode)) {
            if ("layerWeights".equals(node.getNodeName())) {
                String name = getAttribute(node, "name");
                LayerWeights w = parseLayerWeights(node, name);
                if (layerWeights.get(name) != null) {
                    throw new ConfigurationException("layerWeights element for layer '" + name + "' occurs more than once.");
                }
                layerWeights.put(name, w);
            }
        }
    }

    private LayerWeights parseLayerWeights(Node parentNode, String layerName) throws ConfigurationException
    {
        LayerWeights lW = new LayerWeights();
        for (Node node : getChildElements(parentNode)) {
            if ("neuronWeights".equals(node.getNodeName())) {
                try {
                    int col = Integer.parseInt(getAttribute(node, "column"));
                    int row = Integer.parseInt(getAttribute(node, "row"));
                    try {
                        if (lW.get(row, col) != null) {
                            throw new ConfigurationException("neuronWeights element for layer '"
                                    + layerName + "' at row " + row + " and column " + col
                                    + " appears more than once in weight configuration.");
                        }
                    } catch (IndexOutOfBoundsException e) {
                        // Ignore. Means we havent't seen this neuron yet.
                    }
                    NeuronWeights w = parseNeuronWeights(node);
                    lW.set(row, col, w);
                } catch (NumberFormatException e) {
                    throw new ConfigurationException("row or column attributes of neuronWeights element must be numeric.");
                }
            }
        }
        return lW;
    }

    private NeuronWeights parseNeuronWeights(Node parentNode) throws ConfigurationException
    {
        NeuronWeights nW = new NeuronWeights();
        for (Node node : getChildElements(parentNode)) {
            if ("connectionWeight".equals(node.getNodeName())) {
                try {
                    int col = Integer.parseInt(getAttribute(node, "toColumn"));
                    int row = Integer.parseInt(getAttribute(node, "toRow"));
                    String layer = getAttribute(node, "toLayer");
                    double val;
                    try {
                        val = Double.parseDouble(getNodeContent(node));
                    } catch (NumberFormatException e) {
                        throw new ConfigurationException("Content of connectionWeight element must be numeric.");
                    }
                    if (nW.exists(layer, row, col)) {
                        throw new ConfigurationException("connectionWeights element for toLayer '"
                                + layer + "' at toRow " + row + " and toColumn " + col
                                + " appears more than once in weight configuration.");
                    }
                    nW.set(layer, row, col, val);
                } catch (NumberFormatException e) {
                    throw new ConfigurationException("row or column attributes of neuronWeights element must be numeric.");
                }
            }
        }
        return nW;
    }

    /**
     * Builds a Document objects that when serialized can be read by
     * WeightsConfig.
     */
    public static class SaveWeightConfigVisitor extends NetElementVisitor
    {

        private final Document document;
        private Element layerNode;
        private Element neuronNode;
        private Element connectionNode;

        public SaveWeightConfigVisitor() throws ConfigurationException
        {
            document = XMLConfig.createDocument("weights");
        }

        @Override
        public boolean visit(Connection conn)
        {
            connectionNode = document.createElement("connectionWeight");
            Neuron toNeuron = conn.getToNeuron();
            connectionNode.setAttribute("toLayer", toNeuron.getLayer().getName());
            connectionNode.setAttribute("toRow", "" + toNeuron.getRow());
            connectionNode.setAttribute("toColumn", "" + toNeuron.getColumn());
            Node textNode = document.createTextNode("" + conn.getWeight());
            connectionNode.appendChild(textNode);
            neuronNode.appendChild(connectionNode);
            return false;
        }

        @Override
        public boolean visit(Neuron neuron)
        {
            neuronNode = document.createElement("neuronWeights");
            neuronNode.setAttribute("row", "" + neuron.getRow());
            neuronNode.setAttribute("column", "" + neuron.getColumn());
            layerNode.appendChild(neuronNode);
            return true;
        }

        @Override
        public boolean visit(Layer layer)
        {
            // Skip the output layer
            if (layer.getLayerType() == LayerType.OUTPUT) {
                return false;
            }
            layerNode = document.createElement("layerWeights");
            layerNode.setAttribute("name", layer.getName());
            document.getDocumentElement().appendChild(layerNode);
            return true;
        }

        @Override
        public boolean visit(Net net)
        {
            return true;
        }

        public Document getDocument()
        {
            return document;
        }

    }
}
