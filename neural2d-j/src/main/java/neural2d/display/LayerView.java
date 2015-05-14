package neural2d.display;

import java.util.HashMap;
import java.util.Map;
import jgl.GL;
import jgl.GLAUX;
import jgl.GLUT;
import neural2d.Layer;
import neural2d.NetElementVisitor;
import neural2d.Neuron;

/**
 * @author Michael C. Whidden
 */
class LayerView extends View
{

    Map<Neuron, NeuronView> neurons = new HashMap<>();
    Layer layer;

    public LayerView(NetView netView, Layer l, GL myGL, GLAUX myAUX, GLUT myUT)
    {
        super(netView, myGL, myAUX, myUT);
        this.layer = l;

        layer.accept(new LayerViewBuilder());
    }

    public NeuronView getNeuronView(Neuron n)
    {
        return neurons.get(n);
    }

    class LayerViewBuilder extends NetElementVisitor
    {

        @Override
        public boolean visit(Neuron n)
        {
            NeuronView nv = new NeuronView(netView, LayerView.this, n, myGL, myAUX, myUT);
            neurons.put(n, nv);
            return false;
        }
    }

    @Override
    void layout()
    {
        float maxCount = Math.max(layer.getNumRows(),
                layer.getNumColumns());
        float minSize = Math.min(size[1], size[2]);
        float spacing = minSize / (maxCount + 3);
        float layerDepth = spacing * (layer.getNumColumns() - 1);
        float layerHeight = spacing * (layer.getNumRows() - 1);
        float zOffset = pos[2] + (size[2] - layerDepth) / 2;
        float yOffset = pos[1] + (size[1] - layerHeight) / 2;
        for (NeuronView n : neurons.values()) {
            n.reposition(pos[0] + size[0] / 2,
                    // TODO: Scale neurons and spacing according to
                    // the space assigned to this layer
                    yOffset + spacing * n.neuron.getRow(),
                    zOffset + spacing * n.neuron.getColumn());
            float neuronSize = (float) Math.min(0.5, Math.max(0.02, 0.06 * size[1]));
            n.resize(neuronSize, neuronSize, neuronSize);
            n.layout();
        }
    }

    @Override
    public void render()
    {
        /*myGL.glMaterialfv (GL.GL_FRONT, GL.GL_DIFFUSE, lineMatDiffuse);
         myGL.glMaterialfv (GL.GL_FRONT, GL.GL_SPECULAR, lineMatSpecular);
         myGL.glMaterialfv (GL.GL_FRONT, GL.GL_SHININESS, lineMatShininess);
         myGL.glPushMatrix();
         myGL.glTranslatef(x, 0, (layer.getNumColumns()-1)/2.0f);
         myAUX.auxSolidSphere(0.5f);
         myGL.glPopMatrix();*/

        for (NeuronView nv : neurons.values()) {
            nv.render();
        }
    }
}
