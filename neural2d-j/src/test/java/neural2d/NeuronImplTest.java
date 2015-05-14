/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neural2d;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author mwhidden
 */
public class NeuronImplTest
{

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
        System.out.println("calcHiddenGradients");
        NeuronImpl instance = null;
        // instance.calcHiddenGradients();
        // TODO review the generated test code and remove the default call to

    }

    /**
     * Test of calcOutputGradients method, of class NeuronImpl.
     */
    @Test
    public void testCalcOutputGradients()
    {
        System.out.println("calcOutputGradients");
        double targetVal = 0.0;
        NeuronImpl instance = null;
        // instance.calcOutputGradients(targetVal);
        // TODO review the generated test code and remove the default call to

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
        System.out.println("hasForwardConnections");
        NeuronImpl instance = null;
        boolean expResult = false;
        //boolean result = instance.hasForwardConnections();
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to

    }

    /**
     * Test of hasBackConnections method, of class NeuronImpl.
     */
    @Test
    public void testHasBackConnections()
    {
        System.out.println("hasBackConnections");
        NeuronImpl instance = null;
        boolean expResult = false;
        //boolean result = instance.hasBackConnections();
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to

    }

    /**
     * Test of getNumForwardConnections method, of class NeuronImpl.
     */
    @Test
    public void testGetNumForwardConnections()
    {
        System.out.println("getNumForwardConnections");
        NeuronImpl instance = null;
        //int expResult = 0;
        ////int result = instance.getNumForwardConnections();
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to

    }

    /**
     * Test of getNumBackConnections method, of class NeuronImpl.
     */
    @Test
    public void testGetNumBackConnections()
    {
        System.out.println("getNumBackConnections");
        NeuronImpl instance = null;
        //int expResult = 0;
        //int result = instance.getNumBackConnections();
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to

    }

    /**
     * Test of toString method, of class NeuronImpl.
     */
    @Test
    public void testToString()
    {
        System.out.println("toString");
        NeuronImpl instance = null;
        //String expResult = "";
        //String result = instance.toString();
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to

    }

    /**
     * Test of debugShowFwdNet method, of class NeuronImpl.
     */
    @Test
    public void testDebugShowFwdNet()
    {
        System.out.println("debugShowFwdNet");
        NeuronImpl instance = null;
        //String expResult = "";
        //String result = instance.debugShowFwdNet();
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to

    }

    /**
     * Test of debugShowBackNet method, of class NeuronImpl.
     */
    @Test
    public void testDebugShowBackNet()
    {
        System.out.println("debugShowBackNet");
        NeuronImpl instance = null;
        //String expResult = "";
        //String result = instance.debugShowBackNet();
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to

    }

    /**
     * Test of feedForward method, of class NeuronImpl.
     */
    @Test
    public void testFeedForward()
    {
        System.out.println("feedForward");
        NeuronImpl instance = null;
        // instance.feedForward();
        // TODO review the generated test code and remove the default call to

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
