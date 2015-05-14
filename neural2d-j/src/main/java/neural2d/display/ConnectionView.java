package neural2d.display;

import jgl.GL;
import jgl.GLAUX;
import jgl.GLUT;
import neural2d.Connection;
import neural2d.Neuron;

/**
 * @author Michael C. Whidden
 */
class ConnectionView extends View
{

    float connMatDiffuse[] = {0.6f, 0.6f, 0.0f, 1.0f};
    float connMatSpecular[] = {0.6f, 0.6f, 0.6f, 1.0f};
    float connMatShininess[] = {50.0f};
    private final Connection conn;

    ConnectionView(NetView netView, Connection c,
            GL myGL, GLAUX myAUX, GLUT myUT)
    {
        super(netView, myGL, myAUX, myUT);
        this.conn = c;
    }

    @Override
    public void render()
    {
        Neuron toNeuron = conn.getToNeuron(), fromNeuron = conn.getFromNeuron();
        NeuronView toNeuronView, fromNeuronView;
        LayerView toLayerView = netView.getLayerView(toNeuron.getLayer());
        LayerView fromLayerView = netView.getLayerView(fromNeuron.getLayer());
        toNeuronView = toLayerView.getNeuronView(toNeuron);
        fromNeuronView = fromLayerView.getNeuronView(fromNeuron);
        myGL.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, connMatDiffuse);
        myGL.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, connMatSpecular);
        myGL.glMaterialfv(GL.GL_FRONT, GL.GL_SHININESS, connMatShininess);
        myGL.glPushMatrix();
        // starting point
        // Rotate so cylinder axis (on z) points to the target neuron
        // Construct a vector pointing from here to the other neuron
        //float toX = toNeuronView.x - fromNeuronView.x;
        //float toY = toNeuronView.y - fromNeuronView.z;
        //float toZ = toNeuronView.y - fromNeuronView.z;

        // figure out the rotation
        //if(toZ == 0){
        //    toZ = 0.0000001f; // handle degenerate case.
        //}
        //float length = (float)Math.sqrt(toX*toX+ toY*toY + toZ*toZ);
        //float angle = (float)(57.2957795f*Math.acos(toZ/length));
        //if(toZ < 0.0){ // backward cylinder
        //    angle = -angle;
        //}
        //float rx = -toY*toZ;
        //float ry = toX*toZ;
        //myGL.glTranslatef(length/2,0,0);
        //myGL.glRotatef(-90, 0, 0, 1);
        //myGL.glRotatef(angle, rx, ry, 0.0f);
        //myAUX.auxSolidCylinder(0.1, length);
        myGL.glBegin(GL.GL_LINES);
        myGL.glVertex3fv(fromNeuronView.pos);
        myGL.glVertex3fv(toNeuronView.pos);
        myGL.glEnd();
        myGL.glPopMatrix();
    }

    @Override
    void layout()
    {
    }

}
