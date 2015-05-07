package neural2d.config;

import static neural2d.config.XMLConfig.getChildElements;
import org.w3c.dom.Node;

/**
 * <p>
 * <p>
 * <p>
 * Copyright (c) 2015 Michael C. Whidden
 * @author Michael C. Whidden
 */
public class TrainingParameters extends XMLConfig
{

    // Training will pause when the recent average overall error falls below this threshold:

    private float errorThreshold;
        // eta is the network learning rate. It can be set to a constant value, somewhere
    // in the range 0.0001 to 0.1. Optionally, set dynamicEtaAdjust to true to allow
    // the program to automatically adjust eta during learning for optimal learning.
    private float eta;                // Initial overall net learning rate, [0.0..1.0]
    private boolean dynamicEta;    // true enables automatic eta adjustment during training
    // alpha is the momentum factor. Set it to zero to disable momentum. If alpha > 0, then
    // changes in any connection weight in the same direction that the weight was changed
    // last time is amplified. This helps converge on a solution a little faster during
    // the early stages of training, but if set too high will interfere with the network
    // converging on the most accurate solution.
    private float alpha; // Initial momentum, multiplier of last deltaWeight, [0.0..1.0]

    // Regularization parameter. If zero, regularization is disabled:
    private float lambda;
        // When a net topology specifies sparse connections (i.e., when there is a radius
    // parameter specified in the topology config file), then the shape of the area
    // that is back-projected onto the source layer of neurons can be elliptical or
    // rectangular. The default is elliptical (false).
    private boolean projectRectangular;
        // For some calculations, we use a running average of net error, averaged over
    // this many input samples:
    private int recentAverageSmoothingFactor;

        // If repeatInputSamples is false, the program will pause after running all the
    // input samples once. If set to true, the input samples will automatically repeat.
    // If shuffleInputSamplies is true, then the input samples will be randomly
    // shuffled after each use:
    private boolean repeatInputSamples;
    private boolean shuffleInputSamples;
    private int reportEveryNth;

    public TrainingParameters() throws ConfigurationException
    {
        errorThreshold = 0.01f;
        eta = 0.01f;                    // Initial overall net learning rate, [0.0..1.0]
        dynamicEta = true;       // true enables automatic eta adjustment during training
        alpha = 0.0f;                   // Momentum factor, multiplier of last deltaWeight, [0.0..1.0]
        lambda = 0.0f;                  // Regularization parameter; disabled if 0.0
        projectRectangular = false;    // Use elliptical areas for sparse connections
        recentAverageSmoothingFactor = 125; // Average net errors over this many input samples
        repeatInputSamples = true;
        shuffleInputSamples = true;
        reportEveryNth = 100;
    }

    public TrainingParameters(Node parent) throws ConfigurationException
    {
        this();
        for (Node node : getChildElements(parent)) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String name = node.getNodeName();
                switch (name) {
                    case "eta":
                        try {
                            eta = Float.parseFloat(getNodeContent(node));
                        } catch (NumberFormatException e) {
                            throw new ConfigurationException("Training parameter " + name + " should be numeric", e);
                        }
                        break;
                    case "alpha":
                        try {
                            alpha = Float.parseFloat(getNodeContent(node));
                        } catch (NumberFormatException e) {
                            throw new ConfigurationException("Training parameter " + name + " should be numeric", e);
                        }
                        break;
                    case "lamba":
                        try {
                            lambda = Float.parseFloat(getNodeContent(node));
                        } catch (NumberFormatException e) {
                            throw new ConfigurationException("Training parameter " + name + " should be numeric", e);
                        }
                        break;
                    case "dynamicEta":
                        try {
                            dynamicEta = Boolean.parseBoolean(getNodeContent(node));
                        } catch (NumberFormatException e) {
                            throw new ConfigurationException("Training parameter " + name + " should be 'on' or 'off'", e);
                        }
                        break;
                    case "errorThreshold":
                        try {
                            errorThreshold = Float.parseFloat(getNodeContent(node));
                        } catch (NumberFormatException e) {
                            throw new ConfigurationException("Training parameter " + name + " should be numeric", e);
                        }
                        break;
                    case "repeatSamples":
                        try {
                            repeatInputSamples = Boolean.parseBoolean(getNodeContent(node));
                        } catch (NumberFormatException e) {
                            throw new ConfigurationException("Training parameter " + name + " should be 'on' or 'off'", e);
                        }
                        break;
                    case "shuffleSamples":
                        try {
                            shuffleInputSamples = Boolean.parseBoolean(getNodeContent(node));
                        } catch (NumberFormatException e) {
                            throw new ConfigurationException("Training parameter " + name + " should be 'on' or 'off'", e);
                        }
                        break;
                    case "reportEveryNth":
                        try {
                            reportEveryNth = Integer.parseInt(getNodeContent(node));
                        } catch (NumberFormatException e) {
                            throw new ConfigurationException("Training parameter " + name + " should be an integer.", e);
                        }
                        break;
                    case "averageErrorSmoothing":
                        try {
                            recentAverageSmoothingFactor = Integer.parseInt(getNodeContent(node));
                        } catch (NumberFormatException e) {
                            throw new ConfigurationException("Training parameter " + name + " should be an integer.", e);
                        }
                        break;
                }
            }
        }
    }

    public int getReportEveryNth()
    {
        return reportEveryNth;
    }

    public float getErrorThreshold()
    {
        return errorThreshold;
    }

    public float getEta()
    {
        return eta;
    }

    public boolean isDynamicEta()
    {
        return dynamicEta;
    }

    public float getAlpha()
    {
        return alpha;
    }

    public float getLambda()
    {
        return lambda;
    }

    public boolean isRectangular()
    {
        return projectRectangular;
    }

    public int getRecentAverageSmoothingFactor()
    {
        return recentAverageSmoothingFactor;
    }

    public boolean repeatInputSamples()
    {
        return repeatInputSamples;
    }

    public boolean shuffleInputSamples()
    {
        return shuffleInputSamples;
    }
}
