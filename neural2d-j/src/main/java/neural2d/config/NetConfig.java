package neural2d.config;

import java.io.File;
import neural2d.Net;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <p>
 *
 * <p>
 *
 * <p>
 ** Copyright Michael C. Whidden 2015
 * @author Michael C. Whidden
 */
public class NetConfig extends XMLConfig
{

    private TopologyConfig topology;
    private WeightsConfig weights;
    private Node topologyElement, trainingElement;
    private TrainingParameters trainingParams;


    // other configuration items here... eta, etc...
    private final static String topologyDTD =
            "<!ELEMENT topology (input,(layers)?,output)>\n"
                + "<!ELEMENT input ((size|radius|from|channel|tf|convolve|name)*)>\n"
                + "<!ELEMENT name (#PCDATA)>\n"
                + "<!ELEMENT size (#PCDATA)>\n"
                + "<!ELEMENT radius (#PCDATA)>\n"
                + "<!ATTLIST radius rectangular (true|false) \"false\">\n"
                + "<!ELEMENT from (#PCDATA)>\n"
                + "<!ELEMENT channel (R|G|B|BW) >\n"
                + "<!ELEMENT tf (#PCDATA)>\n"
                + "<!ELEMENT convolve (#PCDATA)>\n"
                + "<!ATTLIST convolve rows CDATA #REQUIRED>"
                + "<!ATTLIST convolve cols CDATA #REQUIRED>"
                + "<!ELEMENT layers (layer)*>\n"
                + "<!ELEMENT output ((size|radius|from|channel|tf|convolve|name)*)>\n"
                + "<!ATTLIST output classifier (true|false) \"false\">\n"
                + "<!ELEMENT layer ((size|radius|from|channel|tf|convolve|name)*)>\n";
    private final static String weightsDTD = "<!ELEMENT weights ((layerWeights)*)>\n"
                + "<!ELEMENT layerWeights ((neuronWeights)*)>\n"
                + "<!ELEMENT neuronWeights ((connectionWeight)*)>\n"
                + "<!ELEMENT connectionWeight (#PCDATA)>\n"
                + "<!ATTLIST layerWeights name CDATA #REQUIRED>"
                + "<!ATTLIST neuronWeights column CDATA #REQUIRED>"
                + "<!ATTLIST neuronWeights row CDATA #REQUIRED>"
                + "<!ATTLIST connectionWeight toRow CDATA #REQUIRED>"
                + "<!ATTLIST connectionWeight toColumn CDATA #REQUIRED>"
                + "<!ATTLIST connectionWeight toLayer CDATA #REQUIRED>";
    private final static String trainingDTD =
            "<!ELEMENT trainingParams ((eta|alpha|lamba|dynamicEta|errorThreshold|repeatSamples|shuffleSamples|averageErrorSmoothing)*)>\n"
                + "<!ELEMENT eta (#PCDATA)>\n"
                + "<!ELEMENT alpha (#PCDATA)>\n"
                + "<!ELEMENT lamba (#PCDATA)>\n"
                + "<!ELEMENT dynamicEta (#PCDATA)>\n"
                + "<!ELEMENT errorThreshold (#PCDATA)>\n"
                + "<!ELEMENT repeatSamples (#PCDATA)>\n"
                + "<!ELEMENT shuffleSamples (#PCDATA)>\n"
                + "<!ELEMENT averageErrorSmoothing (#PCDATA)>\n";

    public static NetConfig parseConfig(String configFilename) throws ConfigurationException
    {
        return parseConfig(new File(configFilename));
    }

    public static NetConfig parseConfig(File file) throws ConfigurationException
    {
        Document document = getDocument(file,
                "<?xml version=\"1.0\"?>\n"
                + "<!DOCTYPE net [\n"
                + "<!ELEMENT net ((topology|weights|trainingParams)*)>\n"
                + topologyDTD
                + weightsDTD
                + trainingDTD
                + "\n"
                + "]>\n");

        Element netElem = document.getDocumentElement();
        return new NetConfig(netElem);
    }

    public TopologyConfig getTopologyConfig()
    {
        return topology;
    }

    public WeightsConfig getWeightsConfig()
    {
        return weights;
    }

    public TrainingParameters getTrainingParameters()
    {
        return trainingParams;
    }

    public boolean isTrained()
    {
        return weights != null;
    }

    private NetConfig(Element netElem) throws ConfigurationException
    {
        trainingParams = new TrainingParameters();
        for(Node node: getChildElements(netElem)){
            if(node.getNodeType() == Node.ELEMENT_NODE){
                switch(node.getNodeName()){
                    case "topology":
                        this.topology = new TopologyConfig(node);
                        topologyElement = node;
                        break;
                    case "weights":
                        this.weights = new WeightsConfig(node);
                        break;
                    case "trainingParams":
                        this.trainingParams = new TrainingParameters(node);
                        this.trainingElement = node;
                        break;
                }
            }
        }

        if(topology == null){
            throw new ConfigurationException("Could not find <topology> node.");
        }
    }

    public void writeTrainedNOM(Net net, File outputFile) throws ConfigurationException
    {
        Document weightDocument, nomDocument;
        WeightsConfig.SaveWeightConfigVisitor v =
                new WeightsConfig.SaveWeightConfigVisitor();
        net.accept(v);
        weightDocument = v.getDocument();

        nomDocument = createDocument("net");
        Node newTopo = nomDocument.importNode(topologyElement, true);
        nomDocument.getDocumentElement().appendChild(newTopo);

        if(trainingElement != null){
            Node newTrain = nomDocument.importNode(trainingElement, true);
            nomDocument.getDocumentElement().appendChild(newTrain);
        }

        Node newWeight = nomDocument.importNode(weightDocument.getDocumentElement(), true);
        nomDocument.getDocumentElement().appendChild(newWeight);

        XMLConfig.writeDocument(nomDocument, outputFile);
    }
}

