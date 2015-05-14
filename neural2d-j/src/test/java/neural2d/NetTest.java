/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neural2d;

import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import neural2d.config.NetConfig;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author mwhidden
 */
public class NetTest
{

    // test net
    String testCfg
            = "<net>"
            + "  <topology>"
            + "    <input>"
            + "      <name>input</name>"
            + "      <size>1</size>"
            + "      <tf>identity</tf>"
            + "    </input>"
            + "    <layers>"
            + "      <layer>"
            + "        <name>layer1</name>"
            + "        <size>2x2</size>"
            + "        <tf>identity</tf>"
            + "        <from>input</from>"
            + "      </layer>"
            + "      <layer>"
            + "        <name>layer2</name>"
            + "        <size>2</size>"
            + "        <tf>identity</tf>"
            + "        <from>layer1</from>"
            + "      </layer>"
            + "    </layers>"
            + "    <output>"
            + "      <name>output</name>"
            + "     <size>1</size>"
            + "      <tf>identity</tf>"
            + "      <from>layer2</from>"
            + "    </output>"
            + "  </topology>"
            + "  <weights>\n"
            + "   <layerWeights name=\"$$bias\">\n"
            + "            <neuronWeights column=\"0\" row=\"0\">\n"
            // Add a .01 (the only fractions added) on each bias connection.
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer1\" toRow=\"0\">.01</connectionWeight>\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer1\" toRow=\"1\">.01</connectionWeight>\n"
            + "                <connectionWeight toColumn=\"1\" toLayer=\"layer1\" toRow=\"0\">.01</connectionWeight>\n"
            + "                <connectionWeight toColumn=\"1\" toLayer=\"layer1\" toRow=\"1\">.01</connectionWeight>\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer2\" toRow=\"0\">.01</connectionWeight>\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer2\" toRow=\"1\">.01</connectionWeight>\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"output\" toRow=\"0\">.01</connectionWeight>\n"
            + "             </neuronWeights>\n"
            + "   </layerWeights>\n"
            + "   <layerWeights name=\"input\">\n"
            + "            <neuronWeights column=\"0\" row=\"0\">\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer1\" toRow=\"0\">2</connectionWeight>\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer1\" toRow=\"1\">3</connectionWeight>\n"
            + "                <connectionWeight toColumn=\"1\" toLayer=\"layer1\" toRow=\"0\">5</connectionWeight>\n"
            + "                <connectionWeight toColumn=\"1\" toLayer=\"layer1\" toRow=\"1\">7</connectionWeight>\n"
            + "             </neuronWeights>\n"
            + "   </layerWeights>\n"
            + "   <layerWeights name=\"layer1\">\n"
            + "            <neuronWeights column=\"0\" row=\"0\">\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer2\" toRow=\"0\">11</connectionWeight>\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer2\" toRow=\"1\">13</connectionWeight>\n"
            + "             </neuronWeights>\n"
            + "            <neuronWeights column=\"0\" row=\"1\">\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer2\" toRow=\"0\">17</connectionWeight>\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer2\" toRow=\"1\">19</connectionWeight>\n"
            + "             </neuronWeights>\n"
            + "            <neuronWeights column=\"1\" row=\"0\">\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer2\" toRow=\"0\">23</connectionWeight>\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer2\" toRow=\"1\">29</connectionWeight>\n"
            + "             </neuronWeights>\n"
            + "            <neuronWeights column=\"1\" row=\"1\">\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer2\" toRow=\"0\">31</connectionWeight>\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer2\" toRow=\"1\">37</connectionWeight>\n"
            + "             </neuronWeights>\n"
            + "   </layerWeights>\n"
            + "   <layerWeights name=\"layer2\">\n"
            + "            <neuronWeights column=\"0\" row=\"0\">\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"output\" toRow=\"0\">41</connectionWeight>\n"
            + "             </neuronWeights>\n"
            + "            <neuronWeights column=\"0\" row=\"1\">\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"output\" toRow=\"0\">43</connectionWeight>\n"
            + "             </neuronWeights>\n"
            + "   </layerWeights>\n"
            + " </weights>"
            + "</net>";

    String siblingCfg
            = "<net>"
            + "  <topology>"
            + "    <input>"
            + "      <name>input</name>"
            + "      <size>1</size>"
            + "      <tf>identity</tf>"
            + "    </input>"
            + "    <layers>"
            + "      <layer>"
            + "        <name>layer1</name>"
            + "        <size>2x2</size>"
            + "        <tf>identity</tf>"
            + "        <from>input</from>"
            + "      </layer>"
            + "      <layer>"
            + "        <name>layer2</name>"
            + "        <size>1</size>"
            + "        <tf>identity</tf>"
            + "        <from>layer1</from>"
            + "      </layer>"
            + "      <layer>"
            + "        <name>layer3</name>"
            + "        <size>1</size>"
            + "        <tf>identity</tf>"
            + "        <from>layer1</from>"
            + "      </layer>"
            + "    </layers>"
            + "    <output>"
            + "      <name>output</name>"
            + "     <size>1</size>"
            + "      <tf>identity</tf>"
            + "      <from>layer2</from>"
            + "      <from>layer3</from>"
            + "    </output>"
            + "  </topology>"
            + "  <weights>\n"
            + "   <layerWeights name=\"$$bias\">\n"
            + "            <neuronWeights column=\"0\" row=\"0\">\n"
            // Add a .01 (the only fractions added) on each bias connection.
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer1\" toRow=\"0\">.01</connectionWeight>\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer1\" toRow=\"1\">.01</connectionWeight>\n"
            + "                <connectionWeight toColumn=\"1\" toLayer=\"layer1\" toRow=\"0\">.01</connectionWeight>\n"
            + "                <connectionWeight toColumn=\"1\" toLayer=\"layer1\" toRow=\"1\">.01</connectionWeight>\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer2\" toRow=\"0\">.01</connectionWeight>\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer3\" toRow=\"0\">.01</connectionWeight>\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"output\" toRow=\"0\">.01</connectionWeight>\n"
            + "             </neuronWeights>\n"
            + "   </layerWeights>\n"
            + "   <layerWeights name=\"input\">\n"
            + "            <neuronWeights column=\"0\" row=\"0\">\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer1\" toRow=\"0\">2</connectionWeight>\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer1\" toRow=\"1\">3</connectionWeight>\n"
            + "                <connectionWeight toColumn=\"1\" toLayer=\"layer1\" toRow=\"0\">5</connectionWeight>\n"
            + "                <connectionWeight toColumn=\"1\" toLayer=\"layer1\" toRow=\"1\">7</connectionWeight>\n"
            + "             </neuronWeights>\n"
            + "   </layerWeights>\n"
            + "   <layerWeights name=\"layer1\">\n"
            + "            <neuronWeights column=\"0\" row=\"0\">\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer2\" toRow=\"0\">11</connectionWeight>\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer3\" toRow=\"0\">13</connectionWeight>\n"
            + "             </neuronWeights>\n"
            + "            <neuronWeights column=\"0\" row=\"1\">\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer2\" toRow=\"0\">17</connectionWeight>\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer3\" toRow=\"0\">19</connectionWeight>\n"
            + "             </neuronWeights>\n"
            + "            <neuronWeights column=\"1\" row=\"0\">\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer2\" toRow=\"0\">23</connectionWeight>\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer3\" toRow=\"0\">29</connectionWeight>\n"
            + "             </neuronWeights>\n"
            + "            <neuronWeights column=\"1\" row=\"1\">\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer2\" toRow=\"0\">31</connectionWeight>\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"layer3\" toRow=\"0\">37</connectionWeight>\n"
            + "             </neuronWeights>\n"
            + "   </layerWeights>\n"
            + "   <layerWeights name=\"layer2\">\n"
            + "            <neuronWeights column=\"0\" row=\"0\">\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"output\" toRow=\"0\">41</connectionWeight>\n"
            + "             </neuronWeights>\n"
            + "   </layerWeights>\n"
            + "   <layerWeights name=\"layer3\">\n"
            + "            <neuronWeights column=\"0\" row=\"0\">\n"
            + "                <connectionWeight toColumn=\"0\" toLayer=\"output\" toRow=\"0\">43</connectionWeight>\n"
            + "             </neuronWeights>\n"
            + "   </layerWeights>\n"
            + " </weights>"
            + "</net>";

    public NetTest()
    {
    }

    @BeforeClass
    public static void setUpClass()
    {
    }

    @AfterClass
    public static void tearDownClass()
    {
    }

    @Before
    public void setUp()
    {
    }

    @After
    public void tearDown()
    {
    }

    /**
     * Test of compute method, of class Net.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testCompute() throws Exception
    {
        // Create a simple net and make sure it computes correctly
        NetConfig cfg = NetConfig.parseConfig(new ByteArrayInputStream(testCfg.getBytes()));
        Net net = new Net(cfg, 0);
        Matrix input = new Matrix(1, 1);
        input.set(0, 0, 1.0);
        Neural2D.LOG.setLevel(Level.ALL);
        Neural2D.LOG.getParent().getHandlers()[0].setLevel(Level.ALL);
        Matrix output = net.compute(input);
        assertEquals(41 * (11 * (2.01) + 17 * (3.01) + 23 * (5.01) + 31 * (7.01) + 0.01) + 43 * (13 * (2.01) + 19 * (3.01) + 29 * (5.01) + 37 * (7.01) + 0.01) + 0.01,
                output.get(0, 0), 0.001);

        NetConfig siblingNetCfg = NetConfig.parseConfig(new ByteArrayInputStream(this.siblingCfg.getBytes()));
        Net siblingNet = new Net(siblingNetCfg, 0);

        // Same result as before, but this network has two 1x1 layers
        // where the previous net had a single 2x1 layer.
        output = siblingNet.compute(input);
        assertEquals(41 * (11 * (2.01) + 17 * (3.01) + 23 * (5.01) + 31 * (7.01) + 0.01) + 43 * (13 * (2.01) + 19 * (3.01) + 29 * (5.01) + 37 * (7.01) + 0.01) + 0.01,
                output.get(0, 0), 0.001);
    }

    // TODO: Test multiple layers feeding to output
}
