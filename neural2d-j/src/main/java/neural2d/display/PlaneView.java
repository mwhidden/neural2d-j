package neural2d.display;

import java.util.ArrayList;
import java.util.List;
import jgl.GL;
import jgl.GLAUX;
import jgl.GLUT;

/**
 * @author Michael C. Whidden
 */
class PlaneView extends View
{

    final List<LayerView> layers = new ArrayList<>();

    public PlaneView(NetView netView, GL myGL, GLAUX myAUX, GLUT myUT)
    {
        super(netView, myGL, myAUX, myUT);
    }

    public void addLayer(LayerView layer)
    {
        this.layers.add(layer);
    }

    public int size()
    {
        return layers.size();
    }

    @Override
    public void render()
    {

    }

    /**
     * Arrange the layers within the plane, repositioning them appropriately.
     */
    @Override
    void layout()
    {
        switch (size()) {
            case 1:
                // nothing to do. Layer occupies the whole plane
                layers.get(0).resize(size[0], size[1], size[2]);
                layers.get(0).reposition(pos[0], pos[1], pos[2]);
                break;
            case 2:
                // divide the plane in half, one for each layer
                float mid = size[1] / 2.0f;
                layers.get(0).resize(size[0], size[1] / 2.0f, size[2] / 2.0f);
                layers.get(1).resize(size[0], size[1] / 2.0f, size[2] / 2.0f);
                layers.get(0).reposition(pos[0], pos[1], pos[2] + size[2] / 4.0f);
                layers.get(1).reposition(pos[0], pos[1] + mid, pos[2] + size[2] / 4.0f);
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
            case 6:
                break;
            case 7:
            case 8:
            case 9:
                break;
            default:
            // error
        }
        for (LayerView layer : layers) {
            layer.layout();
        }
    }
}
