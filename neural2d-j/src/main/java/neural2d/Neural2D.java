package neural2d;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import neural2d.config.ConfigurationException;
import neural2d.config.NetConfig;
import neural2d.config.SampleConfig;
import neural2d.display.NetDisplay;
import neural2d.display.OutputFunctionDisplay;

/**
 *
 * Copyright (c) 2015 Michael C. Whidden
 *
 * @author mwhidden
 */
public class Neural2D
{

    public static final Logger LOG = Logger.getLogger(Neural2D.class.getName());

    private Mode mode = null;
    private File nom = null, outputFile = null, inputFile = null;
    private boolean force = false, plot = false, model = false;
    private Net myNet;
    private NetConfig config;
    private long randSeed = System.currentTimeMillis();

    // To train the net:
    // Neural2DJ train <NOM> <inputs> <trained NOM>
    // To use a trained on on some input data:
    // Neural2DJ run <NOM> <inputs>
    public static void main(String[] args) throws Exception
    {
        /*
         Perhaps someday generate images of the network...?
         BufferedImage b = new BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB);
         Graphics2D g = b.createGraphics();
         Ellipse2D ell = new Ellipse2D.Double(320, 240, 240, 240);
         g.setColor(Color.red);
         g.fill(ell);
         boolean res = ImageIO.write(b, "bmp", new File("test.bmp"));
         */
        Neural2D n = new Neural2D();
        try {
            n.parseArgs(args);
        } catch (IllegalArgumentException e) {
            System.err.println(e);
            System.err.println(usage());
            System.exit(1);
        }
        n.run();
        System.exit(0);
    }

    public static String usage()
    {
        return "Usage: \n"
                + "     neural2d  [--force|-f] [--seed=seed|-s seed] [--verbose|-v] [--help|-h] train <NOM> <inputs> <trained NOM>\n"
                + "          Train the network described by the NOM using the .\n"
                + "          given input file. The trained network is written to\n"
                + "          the trained NOM file.\n"
                + "        --force/-f   if the trained NOM file exists, overwrite it.\n"
                + "        --seed/-s    specify an integer seed to the random number generator.\n"
                + "                     This allows a training run to be reliably reproduced.\n"
                + "                     If not provided, the current time is used as a seed.\n"
                + "        --model/-m   Display the net as a balls-and-sticks model.\n"
                + "        --plot/-p    Plot the function of the net. Valid only for nets with\n"
                + "                     1x1,1x2 or 2x1 inputs and 1x1 output.\n"
                + "        --verbose/-v verbose output\n"
                + "        --help/-h    display this message\n"
                + "     neural2d validate <NOM> <inputs>\n"
                + "          Validates that a trained network produces the target outputs using\n"
                + "          the given input file.\n"
                + "     neural2d run <NOM> <inputs>\n"
                + "          Run a trained network on some inputs.\n";
    }

    private void parseArgs(String[] args) throws ConfigurationException, Net.SampleException
    {
        int idx;
        for (idx = 0; idx < args.length; idx++) {
            String arg = args[idx];
            if (arg.startsWith("-")
                    && mode == null) {
                try {
                    if (arg.startsWith("--seed=")) {
                        if (arg.length() > 7) {
                            randSeed = Long.parseLong(arg.substring(7));
                        }
                    } else if (arg.equals("-s")) {
                        if (args.length > idx + 1) {
                            randSeed = Long.parseLong(args[++idx]);
                        }
                    } else if ("--plot".equals(arg)
                            || "-p".equals(arg)) {
                        plot = true;
                    } else if ("--model".equals(arg)
                            || "-m".equals(arg)) {
                        model = true;
                    } else if ("--force".equals(arg)
                            || "-f".equals(arg)) {
                        force = true;
                    } else if ("--verbose".equals(arg)
                            || "-v".equals(arg)) {
                        LOG.setLevel(Level.ALL);
                        LOG.getParent().getHandlers()[0].setLevel(Level.ALL);
                    } else if ("--help".equals(arg)
                            || "-h".equals(arg)) {
                        System.out.println(usage());
                        System.exit(0);
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Random seed must be an integer.");
                }
            } else if (mode == null) {
                try {
                    mode = Mode.valueOf(arg.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("No such mode: " + arg);
                }
            } else if (nom == null) {
                nom = new File(arg);
                if (!nom.exists()
                        || !nom.isFile()) {
                    throw new IllegalArgumentException("No such file " + arg);
                }
                if (!nom.canRead()) {
                    throw new IllegalArgumentException("NOM file is not readable: " + arg);
                }
            } else if (inputFile == null) {
                inputFile = new File(arg);
                if (!inputFile.exists()
                        || !inputFile.isFile()) {
                    throw new IllegalArgumentException("No such file " + arg);
                } else if (!inputFile.canRead()) {
                    throw new IllegalArgumentException("Input file is not readable: " + arg);
                }
            } else if (mode == Mode.TRAIN) {
                if (outputFile != null) {
                    throw new IllegalArgumentException("Unexpected argument: " + arg);
                } else {
                    outputFile = new File(arg);
                }
            } else {
                throw new IllegalArgumentException("Unexpected argument: " + arg);
            }
        }

        if (nom == null) {
            throw new IllegalArgumentException("nom file is required.");
        }
        if (inputFile == null) {
            throw new IllegalArgumentException("input file is required.");
        }
        if (mode == Mode.TRAIN) {
            if (outputFile == null) {
                throw new IllegalArgumentException("output file is required in TRAIN mode.");
            }
            if (outputFile.exists()) {
                if (!force) {
                    throw new IllegalArgumentException("Output file for trained NOM exists '"
                            + outputFile.getPath() + "'.");
                } else {
                    if (!outputFile.isFile()
                            || !outputFile.canWrite()) {
                        throw new IllegalArgumentException("Cannot write to output file for trained NOM '"
                                + outputFile.getPath() + "'.");
                    }
                }
            }
        }

        config = NetConfig.parseConfig(nom);
        LOG.info("Random seed is " + randSeed);
        myNet = new Net(config, randSeed);   // Create net, neurons, and connections

        // If this device has a display, show a nice 3d view of the
        // net topology
        if (!GraphicsEnvironment.isHeadless() && model) {
            NetDisplay nv = new NetDisplay(myNet);
            nv.init();
            nv.startAnimate();
        }

    }

    public void run() throws Neural2DException
    {
        SampleSet samples = new SampleSet();
        samples.loadSamples(SampleConfig.parseConfig(inputFile,
                mode != Mode.RUN));
        if (mode == Mode.TRAIN) {
            // TODO: make this optional
            OutputFunctionDisplay disp = null;
            if (!GraphicsEnvironment.isHeadless() && plot) {
                disp = new OutputFunctionDisplay(myNet);
                disp.init();
                disp.startAnimate();
            }
            myNet.train(samples, disp);
            LOG.info("Solved!   -- Saving weights...");
            config.writeTrainedNOM(myNet, outputFile);
            if(plot || model){
                System.out.println("Press any key to exit.");
                try {
                    System.in.read();
                } catch (IOException ex) {
                }
            }
        } else if (mode == Mode.VALIDATE) {
            if (myNet.validate(samples)) {
                LOG.info("Validition succeeded.");
            } else {
                LOG.info("Validition failed.");
            }
        } else {
            myNet.run(samples);
        }
        LOG.info("Done.");
    }

    private enum Mode
    {

        TRAIN, VALIDATE, RUN
    };
}
