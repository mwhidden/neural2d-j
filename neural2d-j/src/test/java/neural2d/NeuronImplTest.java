/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neural2d;

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
public class NeuronImplTest
{
    InputNeuron input;
    HiddenNeuron hidden1, hidden2, hidden3, hidden4, hidden5;
    OutputNeuron output;
    Connection conni1, conni2, conni3, conni4, conni5,
            conn1o, conn2o, conn3o, conn4o, conn5o;

    public NeuronImplTest()
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
        input = new InputNeuron(null, 0, 0);
        input.setOutput(0.5);
        hidden1 = new HiddenNeuron(TransferFunction.TANH, null, 0, 0);
        conni1 = new Connection(input, hidden1);
        hidden1.addBackConnection(conni1);
        input.addForwardConnection(conni1);
        hidden2 = new HiddenNeuron(TransferFunction.GAUSSIAN, null, 0, 1);
        conni2 = new Connection(input, hidden2);
        hidden2.addBackConnection(conni2);
        input.addForwardConnection(conni2);
        hidden3 = new HiddenNeuron(TransferFunction.IDENTITY, null, 0, 2);
        conni3 = new Connection(input, hidden3);
        hidden3.addBackConnection(conni3);
        input.addForwardConnection(conni3);
        hidden4 = new HiddenNeuron(TransferFunction.LOGISTIC, null, 0, 3);
        conni4 = new Connection(input, hidden4);
        hidden4.addBackConnection(conni4);
        input.addForwardConnection(conni4);
        hidden5 = new HiddenNeuron(TransferFunction.RAMP, null, 0, 4);
        conni5 = new Connection(input, hidden5);
        hidden5.addBackConnection(conni5);
        input.addForwardConnection(conni5);

        output = new OutputNeuron(TransferFunction.TANH, null, 0, 0);
        conn1o = new Connection(hidden1, output);
        output.addBackConnection(conn1o);
        hidden1.addForwardConnection(conn1o);
        conn2o = new Connection(hidden2, output);
        output.addBackConnection(conn2o);
        hidden2.addForwardConnection(conn2o);
        conn3o = new Connection(hidden3, output);
        output.addBackConnection(conn3o);
        hidden3.addForwardConnection(conn3o);
        conn4o = new Connection(hidden4, output);
        output.addBackConnection(conn4o);
        hidden4.addForwardConnection(conn4o);
        conn5o = new Connection(hidden5, output);
        output.addBackConnection(conn5o);
        hidden5.addForwardConnection(conn5o);

        // Set all weights to 1.0 for startes
        conni1.setWeight(1.0);
        conni2.setWeight(1.0);
        conni3.setWeight(1.0);
        conni4.setWeight(1.0);
        conni5.setWeight(1.0);

        conn1o.setWeight(1.0);
        conn2o.setWeight(1.0);
        conn3o.setWeight(1.0);
        conn4o.setWeight(1.0);
        conn5o.setWeight(1.0);
    }

    @After
    public void tearDown()
    {
    }

    /**
     * Test of getGradient method, of class NeuronImpl.
     */
    @Test
    public void testGetGradient()
    {
        System.out.println("getGradient");
        NeuronImpl instance = null;
        double expResult = 0.0;
        //double result = instance.getGradient();
        //assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to

    }

    /**
     * Test of getLayer method, of class NeuronImpl.
     */
    @Test
    public void testGetLayer()
    {
        System.out.println("getLayer");
        NeuronImpl instance = null;
        Layer expResult = null;
        //Layer result = instance.getLayer();
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to

    }

    /**
     * Test of calcHiddenGradients method, of class NeuronImpl.
     */
    @Test
    public void testCalcHiddenGradients()
    {
        testCalcOutputGradients();
        // Now that the gradient of the output neuron is known,
        // calculate the hidden layer gradients

        // sum the weighted gradients from each hidden
        // neuron to the output neuron
        double sumWeightedGradients;
        // All weights are 0.5, sum weight*output.gradient of all connections
        // from any hidden neuron to the output layer is
        // just output.gradient*0.5, since there is only one connection
        // per hidden neuron to the output layer.
        sumWeightedGradients = output.gradient*.5;

        // We want to drive the outputs of the hidden layer
        // up/down by sumWeightedGradients in order to get to the
        // target value. To do this, each hidden neuron must
        // move its inputs up/down it's own gradient according
        // to it's contribution to the total input to the next layer.

        double gradient;
        // hidden 1's input sum is 0.5
        // derivative of tf at 0.5 is 1-tanh(0.5)*tanh(0.5)
        gradient = sumWeightedGradients * 0.78644773;
        hidden1.calcGradient(0.0); // target is ignored in hidden layers
        assertEquals(gradient, hidden1.gradient, 0.000001);

        // hidden 2's input sum is 0.5
        // derivative of guassian at 0.5 is -0.5*e^(-(0.5^2)/2)
        gradient = sumWeightedGradients * -0.44124845;
        hidden2.calcGradient(0.0); // target is ignored in hidden layers
        assertEquals(gradient, hidden2.gradient, 0.000001);

        // hidden 3's input sum is 0.5
        // derivative of identity at 0.5 is 1
        gradient = sumWeightedGradients;
        hidden3.calcGradient(0.0); // target is ignored in hidden layers
        assertEquals(gradient, hidden3.gradient, 0.000001);

        // hidden 4's input sum is 0.5
        // derivative of logistic at 0.5 is e^-0.5/(1+e^-0.5)(1+e^-0.5)
        gradient = sumWeightedGradients*0.23500371;
        hidden4.calcGradient(0.0); // target is ignored in hidden layers
        assertEquals(gradient, hidden4.gradient, 0.000001);

        // hidden 4's input sum is 0.5
        // derivative of ramp at 0.5 is 1
        gradient = sumWeightedGradients;
        hidden5.calcGradient(0.0); // target is ignored in hidden layers
        assertEquals(gradient, hidden5.gradient, 0.000001);

    }

    /**
     * Test of calcOutputGradients method, of class NeuronImpl.
     */
    @Test
    public void testCalcOutputGradients()
    {
        testFeedForward();

        // Target is 0.5 less than the actual output (tanh(sumOfHiddenOutputs)-0.5)
        output.calcGradient(0.40212849);
        // double sumOfHiddenOutputs  = 1.48353669;
        double delta = -0.5;
        double slopeOfOutputFunction =.18616417; // derivative of tanh function at sumOfHiddenOutputs
        double gradient = delta * slopeOfOutputFunction;
        assertEquals(gradient, output.gradient, 0.000001);
    }

    /**
     * Test of accept method, of class NeuronImpl.
     */
    @Test
    public void testAccept()
    {
        System.out.println("accept");
        NetElementVisitor v = null;
        NeuronImpl instance = null;
        // instance.accept(v);
        // TODO review the generated test code and remove the default call to

    }

    /**
     * Test of acceptBackward method, of class NeuronImpl.
     */
    @Test
    public void testAcceptBackward()
    {
        System.out.println("acceptBackward");
        NetElementVisitor v = null;
        NeuronImpl instance = null;
        // instance.acceptBackward(v);
        // TODO review the generated test code and remove the default call to

    }

    /**
     * Test of acceptForward method, of class NeuronImpl.
     */
    @Test
    public void testAcceptForward()
    {
        System.out.println("acceptForward");
        NetElementVisitor v = null;
        NeuronImpl instance = null;
        // instance.acceptForward(v);
        // TODO review the generated test code and remove the default call to

    }

    /**
     * Test of sumDOW_nextLayer method, of class NeuronImpl.
     */
    @Test
    public void testSumDOW_nextLayer()
    {
        System.out.println("sumDOW_nextLayer");
        NeuronImpl instance = null;
        double expResult = 0.0;
        //double result = instance.sumDOW_nextLayer();
        //assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to

    }

    /**
     * Test of hasForwardConnections method, of class NeuronImpl.
     */
    @Test
    public void testHasForwardConnections()
    {
        assert(input.hasForwardConnections());

        assert(hidden1.hasForwardConnections());
        assert(hidden2.hasForwardConnections());
        assert(hidden3.hasForwardConnections());
        assert(hidden4.hasForwardConnections());
        assert(hidden5.hasForwardConnections());
        assert(!output.hasForwardConnections());
    }

    /**
     * Test of hasBackConnections method, of class NeuronImpl.
     */
    @Test
    public void testHasBackConnections()
    {
        assert(!input.hasBackConnections());
        assert(hidden1.hasBackConnections());
        assert(hidden2.hasBackConnections());
        assert(hidden3.hasBackConnections());
        assert(hidden4.hasBackConnections());
        assert(hidden5.hasBackConnections());
        assert(output.hasBackConnections());
    }

    /**
     * Test of getNumForwardConnections method, of class NeuronImpl.
     */
    @Test
    public void testGetNumForwardConnections()
    {
        assertEquals(5,input.getNumForwardConnections());
        assertEquals(1,hidden1.getNumForwardConnections());
        assertEquals(1,hidden2.getNumForwardConnections());
        assertEquals(1,hidden3.getNumForwardConnections());
        assertEquals(1,hidden4.getNumForwardConnections());
        assertEquals(1,hidden5.getNumForwardConnections());
        assertEquals(0,output.getNumForwardConnections());
    }

    /**
     * Test of getNumBackConnections method, of class NeuronImpl.
     */
    @Test
    public void testGetNumBackConnections()
    {
        assertEquals(0,input.getNumBackConnections());
        assertEquals(1,hidden1.getNumBackConnections());
        assertEquals(1,hidden2.getNumBackConnections());
        assertEquals(1,hidden3.getNumBackConnections());
        assertEquals(1,hidden4.getNumBackConnections());
        assertEquals(1,hidden5.getNumBackConnections());
        assertEquals(5,output.getNumBackConnections());
    }

    /**
     * Test of feedForward method, of class NeuronImpl.
     */
    @Test
    public void testFeedForward()
    {
        hidden1.feedForward();
        assertEquals(.462117157, hidden1.getOutput(), 0.000001);
        hidden2.feedForward();
        assertEquals(.882496902, hidden2.getOutput(), 0.000001);
        hidden3.feedForward();
        assertEquals(0.5, hidden3.getOutput(), 0.000001);
        hidden4.feedForward();
        assertEquals(.622459331, hidden4.getOutput(), 0.000001);
        hidden5.feedForward();
        assertEquals(0.5, hidden5.getOutput(), 0.000001);
        output.feedForward();
        assertEquals(0.99471901, output.getOutput(), 0.000001);

        conn1o.setWeight(0.5);
        conn2o.setWeight(0.5);
        conn3o.setWeight(0.5);
        conn4o.setWeight(0.5);
        conn5o.setWeight(0.5);

        output.feedForward();
        assertEquals(0.90212849, output.getOutput(), 0.000001);


    }

    /**
     * Test of updateInputWeights method, of class NeuronImpl.
     */
    @Test
    public void testUpdateInputWeights()
    {
        System.out.println("updateInputWeights");
        double eta = 0.0;
        double alpha = 0.0;
        NeuronImpl instance = null;
        // instance.updateInputWeights(eta, alpha);
        // TODO review the generated test code and remove the default call to

    }

    /**
     * Test of getOutput method, of class NeuronImpl.
     */
    @Test
    public void testGetOutput()
    {
        System.out.println("getOutput");
        NeuronImpl instance = null;
        double expResult = 0.0;
        //double result = instance.getOutput();
        //assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to

    }

    /**
     * Test of setOutput method, of class NeuronImpl.
     */
    @Test
    public void testSetOutput()
    {
    }

    /**
     * Test of setGradient method, of class NeuronImpl.
     */
    @Test
    public void testSetGradient()
    {
        System.out.println("setGradient");
        double f = 0.0;
        NeuronImpl instance = null;
        // instance.setGradient(f);
        // TODO review the generated test code and remove the default call to

    }

    /**
     * Test of addBackConnection method, of class NeuronImpl.
     */
    @Test
    public void testAddBackConnection()
    {
        System.out.println("addBackConnection");
        Connection c = null;
        NeuronImpl instance = null;
        // instance.addBackConnection(c);
        // TODO review the generated test code and remove the default call to

    }

    /**
     * Test of addForwardConnection method, of class NeuronImpl.
     */
    @Test
    public void testAddForwardConnection()
    {
        System.out.println("addForwardConnection");
        Connection c = null;
        NeuronImpl instance = null;
        // instance.addForwardConnection(c);
        // TODO review the generated test code and remove the default call to

    }

    /**
     * Test of getColumn method, of class NeuronImpl.
     */
    @Test
    public void testGetColumn()
    {
        System.out.println("getColumn");
        NeuronImpl instance = null;
        //int expResult = 0;
        //int result = instance.getColumn();
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to

    }

    /**
     * Test of getRow method, of class NeuronImpl.
     */
    @Test
    public void testGetRow()
    {
        System.out.println("getRow");
        NeuronImpl instance = null;
        //int expResult = 0;
        //int result = instance.getRow();
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to

    }

}
