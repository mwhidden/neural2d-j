package neural2d;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import neural2d.config.ConfigurationException;
import neural2d.config.NetConfig;
import neural2d.config.SampleConfig;

/**
 *
 ** Copyright Michael C. Whidden 2015
 * @author mwhidden
 */
public class Neural2DJ
{

    private Mode mode = null;
    private File nom = null, outputFile = null;
    private InputStream inputs = null;
    private File outputNOM = null;
    private boolean force = false;
    private Net myNet;
    private NetConfig config;

    // To train the net:
    // Neural2DJ <topology config> <input data> --train <weight file>
    // To use a trained on on some input data:
    // Neural2DJ <topology config> <input data> <weight file>
    // TODO: After training, combine topology and weight file into a
    // single trained-network config file
    public static void main(String[] args) throws Net.LayerException, Net.SampleException, ConfigurationException, IOException, Net.FileFormatException
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

        Neural2DJ n = new Neural2DJ();
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
        return "Usage: Neural2DJ train <NOM> <input file> <trained NOM> [--force|-f]\n"
                + "             Train the network described by the NOM using the .\n"
                + "             given input file. The trained network is written to\n"
                + "             the trained NOM file.\n"
                + "           --force/-f if the trained NOM file exists, overwrite it.\n"
                + "        Neural2DJ validate <NOM> <inputs>\n"
                + "             Validates that a trained network produces the target\n"
                + "             outputs using the given input file.\n"
                + "        Neural2DJ run <NOM> <inputs>\n"
                + "             Run a trained network on some inputs.\n";

    }

    private void parseArgs(String[] args) throws ConfigurationException, Net.SampleException
    {
        // We need two or three filenames -- we can define them here, or get them from
        // the command line. If they are specified on the command line, they must be in
        // the order: topology, input-data, and optionally, weights.
        for (String arg : args) {
            if (mode == null) {
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
            } else if (inputs == null) {
                File inputFile = new File(arg);
                if (arg.equals("-")) {
                    // read inputs from standard in
                    inputs = System.in;
                } else if (!inputFile.exists()
                        || !inputFile.isFile()) {
                    throw new IllegalArgumentException("No such file " + arg);
                } else if (!inputFile.canRead()) {
                    throw new IllegalArgumentException("Input file is not readable: " + arg);
                } else {
                    try {
                        inputs = new FileInputStream(inputFile);
                    } catch (FileNotFoundException ex) {
                        throw new IllegalArgumentException("No such file " + arg);
                    }
                }
            } else if (mode == Mode.TRAIN) {
                switch (arg) {
                    case "--force":
                    case "-f":
                        force = true;
                        break;
                    default:
                        if (force) {
                            throw new IllegalArgumentException("Unexpected argument: " + arg);
                        } else {
                            outputFile = new File(arg);
                            if (outputFile.exists()) {
                                if (!force) {
                                    throw new IllegalArgumentException("Output file for trained NOM exists '"
                                            + arg + "'.");
                                } else {
                                    if (!outputFile.isFile()
                                            || !outputFile.canWrite()) {
                                        throw new IllegalArgumentException("Cannot write to output file for trained NOM '"
                                                + arg + "'.");
                                    }
                                }
                            }
                        }
                }
            } else {
                throw new IllegalArgumentException("Unexpected argument: " + arg);
            }
        }

        config = NetConfig.parseConfig(nom);

        myNet = new Net(config);   // Create net, neurons, and connections
        myNet.sampleSet.loadSamples(SampleConfig.parseConfig(inputs,
                mode != Mode.RUN));

        if (mode == Mode.TRAIN) {
            myNet.reportEveryNth = 10;
        }
    }

    public void run() throws Net.SampleException, ConfigurationException
    {
        if (mode == Mode.TRAIN) {
            myNet.train();
            System.out.println("Solved!   -- Saving weights...");
            config.writeTrainedNOM(myNet, outputFile);
        } else if(mode == Mode.VALIDATE){
            if(myNet.validate()){
                System.out.println("Validition succeeded.");
            } else {
                System.out.println("Validition failed.");
            }
        } else {
            myNet.run();
        }
        System.out.println("Done.");
    }

    private enum Mode
    {

        TRAIN, VALIDATE, RUN
    };
}
