package neural2d.config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * <p>
 * <p>
 * <p>
 * Copyright (c) 2015 Michael C. Whidden
 * @author Michael C. Whidden
 */
public class XMLConfig {

    public static void writeDocument(Document dom, File file)
            throws ConfigurationException
    {
        String fileName = file.getPath();
        try {
            writeDocument(dom, new FileOutputStream(file));
        } catch (FileNotFoundException ex) {
            throw new ConfigurationException("Exception while writing configuration file '" + fileName + "'.", ex);
        }
    }

    public static void writeDocument(Document dom, String fileName)
            throws ConfigurationException
    {
        try {
            writeDocument(dom, new FileOutputStream(fileName));
        } catch (FileNotFoundException ex) {
            throw new ConfigurationException("Exception while writing configuration file '" + fileName + "'.", ex);
        }
    }

    public static void writeDocument(Document dom, OutputStream os)
            throws ConfigurationException
    {
        try {
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();

            DOMImplementationLS impl =
                    (DOMImplementationLS)registry.getDOMImplementation("LS");

            LSSerializer writer = impl.createLSSerializer();
            writer.setNewLine("\n");
            DOMConfiguration domConfiguration = writer.getDomConfig();
            if (domConfiguration.canSetParameter("format-pretty-print", Boolean.TRUE)) {
                writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
            }
            //LSOutput out = impl.createLSOutput();
            //out.setEncoding("UTF-8");
            String docStr = writer.writeToString(dom.getDocumentElement());
            docStr = docStr.replaceFirst("^<[^>]*>", "");
            os.write(docStr.getBytes());
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IOException ex) {
            throw new ConfigurationException("Exception while writing configuration file.", ex);
        }
    }

    public static Document createDocument(String rootNode) throws ConfigurationException
    {
        Document doc = new org.apache.xerces.dom.DocumentImpl();
        Node root = doc.createElement(rootNode);
        doc.appendChild(root);
        return doc;
        /*DOMImplementationRegistry registry;
        try {
            registry = DOMImplementationRegistry.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException ex) {
            throw new ConfigurationException("XML internal error", ex);
        }
        DOMImplementation impl = registry.getDOMImplementation("XML");
        return impl.createDocument(null, rootNode, null);*/
    }

    protected static Document getDocument(String configFilename,
            String dtd) throws ConfigurationException
    {
        try {
            return getDocument(new File(configFilename), dtd);
        } catch (ConfigurationException e){
            throw new ConfigurationException("Error parsing file '" + configFilename + "'", e);
        }
    }

    protected static Document getDocument(File f,
            String dtd) throws ConfigurationException
    {
        String configFilename = f.getPath();
        if(!f.isFile()){
            throw new ConfigurationException("Input file '"
                    + configFilename + "' not found.");
        }

        FileInputStream fis;

        try {
            fis = new FileInputStream(f);
        } catch (IOException e){
            throw new ConfigurationException("Error opening file '" + configFilename + "'", e);
        }
        try {
            return getDocument(fis, dtd);
        } catch (ConfigurationException e){
            throw new ConfigurationException("Error parsing file '" + configFilename + "'", e);
        }
    }

    protected static Document getDocument(InputStream fis, String dtd) throws ConfigurationException
    {
        DOMParser parser = new DOMParser();
        InputStream input = new SequenceInputStream(new ByteArrayInputStream(
                dtd.getBytes()),
                fis);
        try {
            parser.setFeature("http://xml.org/sax/features/validation", true);
            SAXErrorHandler eh = new SAXErrorHandler();
            parser.setErrorHandler(eh);
            parser.parse(new InputSource(input));
            if(eh.exception != null){
                throw new ConfigurationException("Parse error.", eh.exception);
            }
            if(eh.warning != null){
                System.err.println(eh.warning);
            }
        } catch (SAXException | IOException ex) {
            throw new ConfigurationException("Parse error.", ex);
        }
        return parser.getDocument();
    }

    protected static String getNodeContent(Node childNode) throws DOMException
    {
        return childNode.getChildNodes().item(0).getNodeValue();
    }

    protected static Iterable<Node> getChildElements(Node elem)
    {
        NodeList nl = elem.getChildNodes();
        List<Node> l = new ArrayList<>(nl.getLength());
        for (int i = 0; i < nl.getLength(); i++) {
            if(nl.item(i).getNodeType() == Node.ELEMENT_NODE){
                l.add(nl.item(i));
            }
        }
        return l;
    }

    protected static String getAttribute(Node node, String att)
    {
        NamedNodeMap attributes = node.getAttributes();
        if (attributes == null) {
            return null;
        }
        Node nameNode = attributes.getNamedItem(att);
        if (nameNode == null) {
            return null;
        }
        return nameNode.getNodeValue();
    }

    protected static String getNameAttribute(Node node)
    {
        return getAttribute(node, "name");
    }

    protected static int[] extractTwoNums(String tempString)
            throws NumberFormatException
    {
        int ary[] = new int[2];
        int idx = tempString.indexOf("x");
        if(idx < 0){
            ary[0] = Integer.parseInt(tempString);
            ary[1] = 1;
        } else {
            ary[0] = Integer.parseInt(tempString.substring(0,idx));
            ary[1] = Integer.parseInt(tempString.substring(idx+1));
        }
        return ary;
    }


    public XMLConfig()
    {
    }

}
