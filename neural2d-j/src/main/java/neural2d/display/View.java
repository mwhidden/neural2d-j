package neural2d.display;

import jgl.GL;
import jgl.GLAUX;
import jgl.GLUT;

/**
 * @author Michael C. Whidden
 */
abstract class View
{

    protected float pos[] = {0, 0, 0};
    protected float size[] = {0, 0, 0};
    protected final GLUT myUT;
    protected final GL myGL;
    protected final GLAUX myAUX;
    protected final NetView netView;

    public View(NetView netView, GL myGL, GLAUX myAUX, GLUT myUT)
    {
        this.netView = netView;
        this.myGL = myGL;
        this.myAUX = myAUX;
        this.myUT = myUT;
    }

    /**
     * Specify the corner of the view item's corner that has the smallest x,y
     * and z coordinate.
     */
    public void reposition(float x, float y, float z)
    {
        pos = new float[]{x, y, z};
    }

    /**
     * Specify the size of the view volume.
     */
    public void resize(float xSize, float ySize, float zSize)
    {
        size = new float[]{xSize, ySize, zSize};
    }

    abstract void layout();

    public abstract void render();

    public final void redisplay()
    {
        myUT.glutPostRedisplay();
    }
}
