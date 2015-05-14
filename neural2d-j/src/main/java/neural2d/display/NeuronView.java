package neural2d.display;

import java.util.ArrayList;
import java.util.List;
import jgl.GL;
import jgl.GLAUX;
import jgl.GLUT;
import neural2d.Connection;
import neural2d.NetElementVisitor;
import neural2d.Neuron;

/**
 * @author Michael C. Whidden
 */
class NeuronView extends View
{

    final Neuron neuron;
    private final List<ConnectionView> conns = new ArrayList<>();
    private static final float neuronMatDiffuse[] = {0.6f, 0.0f, 0.3f, 1.0f};
    private static final float neuronMatSpecular[] = {0.6f, 0.6f, 0.6f, 1.0f};
    private static final float neuronMatShininess[] = {50.0f};
    private final LayerView layerView;

    public NeuronView(NetView netView, LayerView layerView, Neuron n, GL myGL, GLAUX myAUX, GLUT myUT)
    {
        super(netView, myGL, myAUX, myUT);
        this.neuron = n;
        this.layerView = layerView;
        neuron.accept(new NeuronViewBuilder());
    }

    public LayerView getLayerView()
    {
        return layerView;
    }

    @Override
    void layout()
    {
        for (ConnectionView v : conns) {
            v.layout();
        }

    }

    class NeuronViewBuilder extends NetElementVisitor
    {

        @Override
        public boolean visit(Connection c)
        {

            ConnectionView cv = new ConnectionView(netView, c,
                    myGL, myAUX, myUT);
            conns.add(cv);
            cv.reposition(0, 0, 0);
            return false;
        }
    }

    @Override
    public void render()
    {
        /*if(neuron.getName().equals("2")
         || neuron.getName().equals("3")){
         System.out.println("Neuron "+ neuron.getName() + " is at " + x + ", "+ y + ", " + z
         + " from " + layerView.x + "," + layerView.y + "," + layerView.z);
         }  else {
         return;
         }*/
        myGL.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, neuronMatDiffuse);
        myGL.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, neuronMatSpecular);
        myGL.glMaterialfv(GL.GL_FRONT, GL.GL_SHININESS, neuronMatShininess);
        myGL.glPushMatrix();
        myGL.glTranslatef(pos[0], pos[1], pos[2]);
        myAUX.auxSolidSphere(size[1]);
        myGL.glPopMatrix();
        for (ConnectionView v : conns) {
            v.render();
        }
    }

}
