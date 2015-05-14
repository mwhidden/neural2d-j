package neural2d.display;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import jgl.GL;
import jgl.GLCanvas;
import neural2d.Matrix;
import neural2d.Net;
import neural2d.Neural2DException;

/**
 * @author Michael C. Whidden
 */
public class OutputFunctionDisplay extends GLCanvas
{

    private static final float plotMatDiffuse[][] = {{0.0f, 0.7f, 0.0f, 1.0f}, {0.0f, 0.3f, 0.0f, 1.0f}};
    private static final float plotMatSpecular[][] = {{1.0f, 1.0f, 1.0f, 1.0f}, {1.0f, 1.0f, 1.0f, 1.0f}};
    private static final float plotMatShininess[][] = {{10.0f}, {10.0f}};
    private static final float lineMatDiffuse[] = {0.6f, 0.6f, 0.6f, 1.0f};
    private static final float lineMatSpecular[] = {1.0f, 1.0f, 1.0f, 1.0f};
    private static final float lineMatShininess[] = {10.0f};
    private int spin = 0;
    private final Net net;
    private List<double[]> data1D = new ArrayList<>();
    private List<float[][]> data2D = new ArrayList<>();

    public OutputFunctionDisplay(Net n)
    {
        this.net = n;
    }

    public void init()
    {
        Frame mainFrame = new Frame();
        mainFrame.setSize(508, 527);

        myUT.glutInitWindowSize(200, 200);
        myUT.glutInitWindowPosition(0, 0);
        myUT.glutCreateWindow(this);
        myGL.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        myGL.glEnable(GL.GL_DEPTH_TEST);
        myGL.glShadeModel(GL.GL_SMOOTH);
        myUT.glutMainLoop();
        mainFrame.add(this);
        initlights();
        mainFrame.setVisible(true);
        myUT.glutReshapeFunc("myReshape");
        myUT.glutDisplayFunc("display");
        myUT.glutKeyboardFunc("keyboard");
    }

    // Synchronized because when animated, this is called
    // during the idle loop but recalculate can also be called by
    // the Net to refresh the plot.
    public synchronized void display()
    {
        myGL.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        myGL.glPushMatrix();
        myGL.glTranslatef(5, 0, 5);
        int[] size = net.getInputSize();
        if (size[0] + size[1] == 3) {
            myGL.glRotated(spin, 0, 1, 0);
        }
        myGL.glTranslatef(-5, 0, -5);
        drawAxes(10);
        if (size[0] + size[1] == 3) {
            plot2d(data2D);
        } else {
            plot1d(data1D);
        }
        myGL.glPopMatrix();
        myGL.glFlush();
        myGL.glXSwapBuffers(getGraphics(), this);
    }

    // Synchronized because called by
    // the Net to refresh the plot, and when animated, display called
    // during the idle loop.
    public void recalculate() throws Neural2DException
    {
        int[] size = net.getInputSize();
        if (size[0] + size[1] == 2) {
            calc1D(net);
        } else if (size[0] + size[1] == 3) {
            calc2D(net);
        } else {
            throw new Neural2DException("Cannot display output function when more than 3 inputs are required.");
        }
        redisplay();
    }

    private void calc1D(Net net) throws Neural2DException
    {
        Matrix input = new Matrix(1, 1);
        double inVal;
        Matrix val;
        List<double[]> newData = new ArrayList<>();
        for (int i = -100; i <= 100; i++) {
            inVal = (double) i / 100.0;
            input.set(0, 0, (double) i / 100.0);
            val = evaluate(net, input);
            if (val.getNumRows() != 1
                    || val.getNumColumns() != 1) {
                throw new Neural2DException("Cannot yet render non-scalar outputs of 2-input networks.");
            }
            newData.add(new double[]{inVal, val.get(0, 0)});
        }
        data1D = newData;
    }

    private void calc2D(Net net) throws Neural2DException
    {
        List<float[][]> newData = new ArrayList<>();
        int size[] = net.getInputSize();
        Matrix input = new Matrix(size[0], size[1]);
        Matrix val;
        double inVal1, inVal2;
        for (int i = -25; i <= 25; i++) {
            inVal1 = (double) i / 25.0;
            float[][] points = new float[51][];
            newData.add(points);
            for (int j = -25; j <= 25; j++) {
                inVal2 = (double) j / 25.0;
                input.set(0, 0, inVal1);
                if (size[0] == 2) {
                    input.set(1, 0, inVal2);
                } else {
                    input.set(0, 1, inVal2);
                }
                val = evaluate(net, input);
                if (val.getNumRows() != 1
                        || val.getNumColumns() != 1) {
                    throw new Neural2DException("Cannot yet render non-scalar outputs of 2-input networks.");
                }
                points[j + 25] = new float[]{(float) inVal1,
                    (float) val.get(0, 0),
                    (float) inVal2};
            }
        }
        data2D = newData;
    }

    private void plot1d(List<double[]> data)
    {
        double[] prev = null;
        myGL.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, plotMatDiffuse[0]);
        myGL.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, plotMatSpecular[0]);
        myGL.glMaterialfv(GL.GL_FRONT, GL.GL_SHININESS, plotMatShininess[0]);
        myGL.glBegin(GL.GL_LINES);
        for (double[] d : data) {
            if (prev != null) {
                myGL.glVertex2f((float) prev[0] * 10, (float) prev[1] * 10);
                myGL.glVertex2f((float) d[0] * 10, (float) d[1] * 10);
            }
            prev = d;
        }
        myGL.glEnd();

    }

    private void plot2d(List<float[][]> data)
    {
        myGL.glPushMatrix();
        myGL.glTranslated(5, 5, 5);
        myGL.glScaled(5, 5, 5);
        myGL.glBegin(GL.GL_TRIANGLES);
        int matIdx;
        int nextMatIdx = 1;
        for (int y = 0; y < data.size() - 1; y++) {
            matIdx = nextMatIdx;
            nextMatIdx = (nextMatIdx + 1) % 2;
            for (int x = 0; x < data.get(y).length - 1; x++) {
                myGL.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, plotMatDiffuse[matIdx]);
                myGL.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, plotMatSpecular[matIdx]);
                myGL.glMaterialfv(GL.GL_FRONT, GL.GL_SHININESS, plotMatShininess[matIdx]);
                float[][] tri = new float[3][];
                tri[0] = data.get(y)[x];
                tri[1] = data.get(y)[x + 1];
                tri[2] = data.get(y + 1)[x];
                float[] normal = calcNormal(tri);
                myGL.glNormal3fv(normal);
                myGL.glVertex3fv(tri[0]);
                myGL.glVertex3fv(tri[1]);
                myGL.glVertex3fv(tri[2]);
                tri[0] = data.get(y)[x + 1];
                tri[1] = data.get(y + 1)[x + 1];
                tri[2] = data.get(y + 1)[x];

                normal = calcNormal(tri);
                myGL.glNormal3fv(normal);
                myGL.glVertex3fv(tri[0]);
                myGL.glVertex3fv(tri[1]);
                myGL.glVertex3fv(tri[2]);
                matIdx = (matIdx + 1) % 2;
            }
        }
        myGL.glEnd();
        myGL.glPopMatrix();
    }

    private float[] calcNormal(float[][] tri)
    {
        float[] A, B;
        float Nx, Ny, Nz, len;
        A = new float[]{tri[1][0] - tri[0][0],
            tri[1][1] - tri[0][1],
            tri[1][2] - tri[0][2]};
        B = new float[]{tri[2][0] - tri[1][0],
            tri[2][1] - tri[1][1],
            tri[2][2] - tri[1][2]};
        Nx = A[1] * B[2] - A[2] * B[1];
        Ny = A[2] * B[0] - A[0] * B[2];
        Nz = A[0] * B[1] - A[1] * B[0];
        len = (float) Math.sqrt(Nx * Nx + Ny * Ny + Nz * Nz);
        Nx /= len;
        Ny /= len;
        Nz /= len;
        return new float[]{Nx, Ny, Nz};

    }

    private Matrix evaluate(Net net, Matrix input) throws Neural2DException
    {
        return net.compute(input);
    }

    public void redisplay()
    {
        myUT.glutPostRedisplay();
    }

    public void keyboard(char key, int x, int y)
    {
    }

    public void animate()
    {
        spin += 1;
        if (spin >= 360) {
            spin = 0;
        }
        redisplay();
    }

    public void startAnimate()
    {
        myUT.glutIdleFunc("animate");
    }

    public void stopAnimate()
    {
        myUT.glutIdleFunc(null);
    }

    public void myReshape(int w, int h)
    {
        int[] inputSize = net.getInputSize();
        myGL.glViewport(0, 0, w, h);
        myGL.glMatrixMode(GL.GL_PROJECTION);
        myGL.glLoadIdentity();
        if (inputSize[0] + inputSize[1] == 2) {
            myGL.glOrtho(-10, 10, -10, 10, -10, 10);
        } else {
            myGLU.gluPerspective(60.0, 1.0 * (double) w / (double) h, 1.0, 60.0);
            myGLU.gluLookAt(12, 13, 12, 0, 0, 0, 0, 1, 0);
        }
        myGL.glMatrixMode(GL.GL_MODELVIEW);
        myGL.glLoadIdentity();
        myGL.glColor3f(0.0f, 0.0f, 0.0f);

    }

    private void initlights()
    {

        myGL.glEnable(GL.GL_LIGHTING);
        myGL.glEnable(GL.GL_LIGHT0);
        setlights();
    }

    private void setlights()
    {
        float ambient[] = {0.5f, 0.5f, 0.5f, 1.0f};
        float position[] = {50.0f, 150.0f, 50.0f, 1.0f};
        myGL.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, ambient);
        myGL.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, position);
        myGL.glLightfv(GL.GL_LIGHT0, GL.GL_SPECULAR, ambient);

    }

    public void drawAxes(float len)
    {
        myGL.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, lineMatDiffuse);
        myGL.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, lineMatSpecular);
        myGL.glMaterialfv(GL.GL_FRONT, GL.GL_SHININESS, lineMatShininess);
        for (int x = 0; x <= len; x++) {
            drawLine(x, 4.5f, 5, x, 5.5f, 5);
        }
        for (int y = 0; y <= len; y++) {
            drawLine(4.5f, y, 5, 5.5f, y, 5);
        }
        for (int z = 0; z <= len; z++) {
            drawLine(5, 4.5f, z, 5f, 5.5f, z);
        }
        drawLine(5, 5, 0, 5, 5, len);
        drawLine(5, 0, 5, 5, len, 5);
        drawLine(0, 5, 5, len, 5, 5);
        /*drawLine(0,0,len,0,len,len);
         drawLine(0,0,len,len,0,len);
         drawLine(0,len,0,0,len,len);
         drawLine(0,len,0,len,len,0);
         drawLine(0,len,len,len,len,len);
         drawLine(len,0,0,len,0,len);
         drawLine(len,0,0,len,len,0);
         drawLine(len,0,len,len,len,len);
         drawLine(len,len,0,len,len,len);
         */
        for (int x = 0; x <= len; x++) {
            drawLine(x, 5, 0, x, 5, 10);
            drawLine(0, 5, x, 10, 5, x);
        }

    }

    public void drawLine(float x0, float y0, float z0,
            float x1, float y1, float z1)
    {
        myGL.glBegin(GL.GL_LINES);
        myGL.glVertex3f(x0, y0, z0);
        myGL.glVertex3f(x1, y1, z1);
        myGL.glEnd();

    }
}
