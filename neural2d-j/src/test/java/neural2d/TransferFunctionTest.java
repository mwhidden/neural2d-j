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
 * @author Michael C. Whidden
 */
public class TransferFunctionTest
{

    public TransferFunctionTest()
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

    @Test
    public void testLogistic()
    {
        assertEquals(0.5,
                TransferFunction.LOGISTIC.transfer(0.0),
                Double.MIN_VALUE);
        assertEquals(1,
                TransferFunction.LOGISTIC.transfer(100),
                0.0000001);
        assertEquals(0,
                TransferFunction.LOGISTIC.transfer(-100),
                0.0000001);
        assertEquals(0.25,
                TransferFunction.LOGISTIC.derivative().transfer(0.0),
                0.0000001);
        assertEquals(0.0,
                TransferFunction.LOGISTIC.derivative().transfer(100.0),
                0.0000001);
        assertEquals(0.0,
                TransferFunction.LOGISTIC.derivative().transfer(-100.0),
                0.0000001);
    }

    @Test
    public void testTanh()
    {
        assertEquals(0.0,
                TransferFunction.TANH.transfer(0.0),
                Double.MIN_VALUE);
        assertEquals(1,
                TransferFunction.TANH.transfer(100),
                0.0000001);
        assertEquals(-1,
                TransferFunction.TANH.transfer(-100),
                0.0000001);
        assertEquals(1.0,
                TransferFunction.TANH.derivative().transfer(0.0),
                0.0000001);
        assertEquals(0.0,
                TransferFunction.TANH.derivative().transfer(100.0),
                0.0000001);
        assertEquals(0.0,
                TransferFunction.TANH.derivative().transfer(-100.0),
                0.0000001);
    }


    @Test
    public void testIdentity()
    {
        assertEquals(0.0,
                TransferFunction.IDENTITY.transfer(0.0),
                Double.MIN_VALUE);
        assertEquals(100,
                TransferFunction.IDENTITY.transfer(100),
                0.0000001);
        assertEquals(-100,
                TransferFunction.IDENTITY.transfer(-100),
                0.0000001);
        assertEquals(1,
                TransferFunction.IDENTITY.derivative().transfer(0.0),
                0.0000001);
        assertEquals(1,
                TransferFunction.IDENTITY.derivative().transfer(100.0),
                0.0000001);
        assertEquals(1,
                TransferFunction.IDENTITY.derivative().transfer(-100.0),
                0.0000001);
    }

    @Test
    public void testRamp()
    {
        assertEquals(0.0,
                TransferFunction.RAMP.transfer(0.0),
                Double.MIN_VALUE);
        assertEquals(.99,
                TransferFunction.RAMP.transfer(.99),
                0.0000001);
        assertEquals(-.99,
                TransferFunction.RAMP.transfer(-.99),
                0.0000001);
        assertEquals(1,
                TransferFunction.RAMP.transfer(1.0),
                0.0000001);
        assertEquals(-1,
                TransferFunction.RAMP.transfer(-1.0),
                0.0000001);
        assertEquals(1,
                TransferFunction.RAMP.transfer(1.5),
                0.0000001);
        assertEquals(-1,
                TransferFunction.RAMP.transfer(-1.5),
                0.0000001);
        assertEquals(1,
                TransferFunction.RAMP.transfer(100),
                0.0000001);
        assertEquals(-1,
                TransferFunction.RAMP.transfer(-100),
                0.0000001);
        assertEquals(1.0,
                TransferFunction.RAMP.derivative().transfer(0.0),
                Double.MIN_VALUE);
        assertEquals(1,
                TransferFunction.RAMP.derivative().transfer(.99),
                0.0000001);
        assertEquals(1,
                TransferFunction.RAMP.derivative().transfer(-.99),
                0.0000001);
        assertEquals(0,
                TransferFunction.RAMP.derivative().transfer(1.5),
                0.0000001);
        assertEquals(0,
                TransferFunction.RAMP.derivative().transfer(-1.5),
                0.0000001);
        assertEquals(0,
                TransferFunction.RAMP.derivative().transfer(100),
                0.0000001);
        assertEquals(0,
                TransferFunction.RAMP.derivative().transfer(-100),
                0.0000001);
    }

    @Test
    public void testGaussian()
    {
        assertEquals(1.0,
                TransferFunction.GAUSSIAN.transfer(0.0),
                Double.MIN_VALUE);
        assertEquals(0,
                TransferFunction.GAUSSIAN.transfer(100),
                0.0000001);
        assertEquals(0,
                TransferFunction.GAUSSIAN.transfer(-100),
                0.0000001);
        assertEquals(0,
                TransferFunction.GAUSSIAN.derivative().transfer(0.0),
                0.0000001);
        assertEquals(-.6065306,
                TransferFunction.GAUSSIAN.derivative().transfer(1.0),
                0.0000001);
        assertEquals(.6065306,
                TransferFunction.GAUSSIAN.derivative().transfer(-1.0),
                0.0000001);
        assertEquals(0,
                TransferFunction.GAUSSIAN.derivative().transfer(100.0),
                0.0000001);
        assertEquals(0,
                TransferFunction.GAUSSIAN.derivative().transfer(-100.0),
                0.0000001);
    }

}
