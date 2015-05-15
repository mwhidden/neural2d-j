package neural2d.display;

import java.awt.Frame;
import jgl.GL;
import jgl.GLAUX;
import jgl.GLCanvas;
import neural2d.Net;

public class NetDisplay extends GLCanvas
{
    // TODO: Change color of connections according to their weight
    // Change color of neurons according to their type (input, output,
    // hidden, convolution, pooling...).
    // TODO: Shadows?
    private static final float lineMatDiffuse[] = {0.0f, 0.6f, 0.0f, 1.0f};
    private static final float lineMatSpecular[] = {1.0f, 1.0f, 1.0f, 1.0f};
    private static final float lineMatShininess[] = {10.0f};
    private final GLAUX myAUX = new GLAUX(myGL);
    private int spin = 0;
    private NetView netView;
    private final Net net;
    private static final float[] VOLUME = {10.0f, 10.0f, 10.0f};

    public NetDisplay(Net n)
    {
        this.net = n;
    }

    public void init()
    {
        Frame mainFrame = new Frame();
        mainFrame.setSize(508, 527);

        this.netView = new NetView(net, myGL, myAUX, myUT);
        netView.reposition(0, 0, 0);
        netView.resize(VOLUME[0], VOLUME[1], VOLUME[2]);
        netView.layout();
        myUT.glutInitWindowSize(200, 200);
        myUT.glutInitWindowPosition(0, 0);
        myUT.glutCreateWindow(this);
        myGL.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        myGL.glEnable(GL.GL_DEPTH_TEST);
        myGL.glShadeModel(GL.GL_SMOOTH);
        initlights();		/* for lighted version only */

        myUT.glutMainLoop();
        mainFrame.add(this);

        mainFrame.setVisible(true);
        myUT.glutReshapeFunc("myReshape");
        myUT.glutDisplayFunc("display");
        myUT.glutKeyboardFunc("keyboard");
    }

    public void display()
    {
        myGL.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        myGL.glPushMatrix();
        myGL.glTranslatef(VOLUME[0] / 2, 0, VOLUME[2] / 2);
        myGL.glRotated(spin, 0, 1, 0);
        myGL.glTranslatef(-VOLUME[2] / 2, 0, -VOLUME[2] / 2);
        drawAxes(Math.max(VOLUME[0], Math.max(VOLUME[1], VOLUME[2])));
        netView.render();
        myGL.glPopMatrix();
        myGL.glFlush();
        myGL.glXSwapBuffers(getGraphics(), this);
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
        myGL.glViewport(0, 0, w, h);
        myGL.glMatrixMode(GL.GL_PROJECTION);
        myGL.glLoadIdentity();
        myGLU.gluPerspective(60.0, 1.0 * (double) w / (double) h, 1.0, 60.0);
        myGLU.gluLookAt(VOLUME[0], 1.5 * VOLUME[1], 2 * VOLUME[2], .3 * VOLUME[0], 0, 0, 0, 1, 0);
        myGL.glMatrixMode(GL.GL_MODELVIEW);
        myGL.glLoadIdentity();

    }

    private void initlights()
    {
        float ambient[] = {0.2f, 0.2f, 0.2f, 1.0f};
        float position[] = {50.0f, 50.0f, 50.0f, 1.0f};

        myGL.glEnable(GL.GL_LIGHTING);
        myGL.glEnable(GL.GL_LIGHT0);

        myGL.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, ambient);
        myGL.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, position);

    }

    public void drawAxes(float len)
    {
        myGL.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, lineMatDiffuse);
        myGL.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, lineMatSpecular);
        myGL.glMaterialfv(GL.GL_FRONT, GL.GL_SHININESS, lineMatShininess);
        for (int x = 0; x < len; x++) {
            drawLine(x, 0, -.05f * VOLUME[2], x, 0, .05f * VOLUME[2]);
        }
        for (int y = 0; y < len; y++) {
            drawLine(-.05f * VOLUME[0], y, 0, .05f * VOLUME[0], y, 0);
        }
        for (int z = 0; z < len; z++) {
            drawLine(-.05f * VOLUME[0], 0, z, .05f * VOLUME[0], 0, z);
        }
        drawLine(0, 0, 0, 0, 0, len);
        drawLine(0, 0, 0, 0, len, 0);
        drawLine(0, 0, 0, len, 0, 0);
        drawLine(0, 0, len, 0, len, len);
        drawLine(0, 0, len, len, 0, len);
        drawLine(0, len, 0, 0, len, len);
        drawLine(0, len, 0, len, len, 0);
        drawLine(0, len, len, len, len, len);
        drawLine(len, 0, 0, len, 0, len);
        drawLine(len, 0, 0, len, len, 0);
        drawLine(len, 0, len, len, len, len);
        drawLine(len, len, 0, len, len, len);

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
