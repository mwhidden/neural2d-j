package neural2d.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import neural2d.Matrix;
import neural2d.Sample;
import neural2d.Sample.ImageData;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * <p>
 *
 * <p>
 *
 * <p>
 * Copyright (c) 2015 Michael C. Whidden
 * @author Michael C. Whidden
 */
public class SampleConfig extends XMLConfig
{
    int inSizeX, inSizeY, outSizeX, outSizeY;
    List<Sample> samples = new ArrayList<>();
    private final static String dtd = "<?xml version=\"1.0\"?>\n"
                + "<!DOCTYPE sampleSet [\n"
                + "<!ELEMENT sampleSet ((inputSize|targetSize?|samples)*)>\n"
                + "<!ELEMENT samples ((sample)*)>\n"
                + "<!ELEMENT sample ((input,target?)|(target?,input))>\n"
                + "<!ELEMENT input (#PCDATA)>\n"
                + "<!ELEMENT target (#PCDATA)>\n"
                + "<!ELEMENT inputSize (#PCDATA)>\n"
                + "<!ELEMENT targetSize (#PCDATA)>\n"
                + "]>\n";

    public static SampleConfig parseConfig(String configFilename, boolean needsTargets) throws ConfigurationException
    {
        return parseConfig(new File(configFilename), needsTargets);
    }

    public static SampleConfig parseConfig(File f, boolean needsTargets) throws ConfigurationException
    {
        Document dom = getDocument(f, dtd);

        return new SampleConfig(dom.getDocumentElement(), needsTargets);
    }

    public static SampleConfig parseConfig(InputStream is, boolean needsTargets) throws ConfigurationException
    {
        Document dom = getDocument(is, dtd);

        return new SampleConfig(dom.getDocumentElement(), needsTargets);
    }


    private SampleConfig(Node parentNode, boolean needsTargets) throws ConfigurationException
    {
        List<Node> samplesNodes = new ArrayList<>();
        for(Node node: getChildElements(parentNode)){
            String content;
            int twoNums[];
            switch(node.getNodeName()){
                case "inputSize":
                case "targetSize":
                    boolean input = node.getNodeName().charAt(0) == 'i';
                    content = getNodeContent(node);
                    try {
                        twoNums = extractTwoNums(content);
                    } catch (NumberFormatException e){
                        throw new ConfigurationException("Bad XxY format in config file. " + content);
                    }
                    if(input){
                        inSizeX = twoNums[0];
                        inSizeY = twoNums[1];
                    } else {
                        outSizeX = twoNums[0];
                        outSizeY = twoNums[1];
                    }
                    break;
                case "samples":
                    samplesNodes.add(node);
                    break;
            }
        }
        if(samplesNodes.isEmpty()){
            throw new ConfigurationException("Input file has no sample data.");
        }
        // in and out sizes will be valid at this point
        for(Node node: samplesNodes){
            parseSamples(node);
        }

        for(Sample sample: samples){
            if(sample.getTargetVals() == null && needsTargets){
                throw new ConfigurationException("All samples must have target values in training and validation modes).");
            }
        }
    }

    private void parseSamples(Node parentNode) throws ConfigurationException
    {
        for(Node node: getChildElements(parentNode)){
            switch(node.getNodeName()){
                case "sample":
                    parseSample(node);
                    break;
            }
        }
    }

    private void parseSample(Node parentNode) throws ConfigurationException
    {
        Matrix in = null, out = null;
        ImageData img = null;
        for(Node node: getChildElements(parentNode)){
            switch(node.getNodeName()){
                case "input":
                    img = parseImage(node);

                    if(img == null){
                        in = new Matrix(inSizeX, inSizeY);
                        in.load(MatrixConfig.parse(getNodeContent(node), inSizeX, inSizeY));
                    }
                    break;
                case "target":
                    out = new Matrix(outSizeX, outSizeY);
                    out.load(MatrixConfig.parse(getNodeContent(node), outSizeX, outSizeY));
                    break;
            }
        }
        if((in == null && img == null)){
            throw new ConfigurationException("input values must be supplied for all samples.");
        } else if(img == null) {
            samples.add(Sample.createSample(in, out));
        } else {
            samples.add(Sample.createSample(img, out));
        }
    }

    private ImageData parseImage(Node parentNode) throws ConfigurationException
    {
        // See if the input node has an image in it.
        for(Node node: getChildElements(parentNode)){
            switch(node.getNodeName()){
                case "img":
                    String file = getAttribute(node, "src");
                    try {
                        InputStream is = new URL(file).openConnection().getInputStream();
                        ImageData img = readBMP(is, file);
                        if(img.getNumRows() != inSizeY
                                || img.getNumColumns() != inSizeX){
                            throw new ConfigurationException("Image file '" + file + "' dimensions do not match size of input layer.");
                        }
                        return img;
                    } catch (MalformedURLException ex) {
                        throw new ConfigurationException("Image file '" + file + "' could not be loaded.", ex);
                    } catch (IOException ex) {
                        throw new ConfigurationException("Image file '" + file + "' could not be loaded.", ex);

                    }
            }
        }
        return null;
    }

    private ImageData readBMP(InputStream is, String filename) throws IOException
    {
        byte info[] = new byte[54];
        if(is.read(info) != 54) {
            throw new IOException("Error reading the image header from '"
                    + filename + "'");
        }

        if (info[0] != 'B' || info[1] != 'M') {
            throw new IOException("Invalid BMP file '"
                    + filename + "'");
        }

        // Verify the offset to the pixel data. It should be the same size as the info[] data read above.

        int dataOffset = (info[13] << 24)
                          + (info[12] << 16)
                          + (info[11] << 8)
                          +  info[10];

        // Verify that the file contains 24 bits (3 bytes) per pixel (red, green blue at 8 bits each):

        int pixelDepth = (info[29] << 8) + info[28];
        if (pixelDepth != 24) {
            throw new IOException("Error: BMP file is not 24 bits per pixel");
        }

        // This method of converting 4 bytes to a uint32_t is portable for little- or
        // big-endian environments:

        int width = (info[21] << 24)
                       + (info[20] << 16)
                       + (info[19] << 8)
                       +  info[18];

        int height = (info[25] << 24)
                        + (info[24] << 16)
                        + (info[23] << 8)
                        +  info[22];

        // Position the read pointer to the first byte of pixel data:
        dataOffset -= 54; // already read 54
        while(dataOffset-- > 0){
            is.read();
        }
        int rowLen_padded = (width*3 + 3) & (~3);
        byte[] rowData = new byte[rowLen_padded];
        byte[] imageData = new byte[width * height * 3];
        int idx = 0;
        // Fill the data container with 8-bit data taken from the image data:
        int read = 0, len = 0;
        for (int y = 0; y < height; ++y) {
            while(read < rowLen_padded && len >= 0){
                len = is.read(rowData, read, rowLen_padded - read);
                if(len >= 0){
                    read += len;
                } else {
                    throw new IOException("Error reading '" + filename + "' row " + y);
                }
            }
            for(int x=0; x < width; x++){
                imageData[idx++] = rowData[x * 3 + 2]; // Red
                imageData[idx++] = rowData[x * 3 + 1]; // Green
                imageData[idx++] = rowData[x * 3 + 0]; // Blue
            }
        }
        return new ImageData(imageData, height, width);
    }

    public List<Sample> getSamples()
    {
        return samples;
    }
}
