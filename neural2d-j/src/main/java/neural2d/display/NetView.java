package neural2d.display;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jgl.GL;
import jgl.GLAUX;
import jgl.GLUT;
import neural2d.Connection;
import neural2d.Layer;
import neural2d.LayerType;
import neural2d.Net;
import neural2d.NetElementVisitor;

/**
 * <p>
 * @author Michael C. Whidden
 */
class NetView extends View
{

    private final Map<LayerView, PlaneView> planesMap = new HashMap<>();
    private final Map<Layer, LayerView> layersMap = new HashMap<>();
    private final List<PlaneView> planes = new ArrayList<>();
    private final List<LayerView> layers = new ArrayList<>();

    public NetView(Net net, GL myGL, GLAUX myAUX, GLUT myUT)
    {
        super(null, myGL, myAUX, myUT);
        net.accept(new NetViewBuilder());
    }

    public PlaneView getPlaneView(LayerView l)
    {
        return planesMap.get(l);
    }

    public LayerView getLayerView(Layer l)
    {
        return layersMap.get(l);
    }

    public void addLayer(Layer l)
    {
        LayerView view = new LayerView(this, l, myGL, myAUX, myUT);
        layersMap.put(l, view);
        layers.add(view); // for ordering
    }

    @Override
    void layout()
    {
        // Assign each layer to a plane, so that they remain in order
        // of forward propagation, but so that we can arrange them
        // off axis so they don't overlap if multiple layers feed
        // together into a forward layer.
        /* func add node, plane
         if node has already been assigned a plane
         return
         add node to plane
         if node has children
         foreach child
         add child, plane+1*/
        PlaneView firstPlane = new PlaneView(this, myGL, myAUX, myUT);
        planes.clear();
        planesMap.clear();
        planes.add(firstPlane);
        addToPlane(layers.get(0), firstPlane);
        int idx = 0;
        for (PlaneView plane : planes) {
            System.out.print("plane " + (idx++) + " has layers: ");
            for (LayerView l : plane.layers) {
                System.out.print(l.layer.getName() + " ");
            }
            System.out.println();
        }
        int numPlanes = planes.size();
        float planeThickness = size[0] / (numPlanes + 1);
        float nx = pos[0] + planeThickness / 2.0f;
        for (PlaneView plane : planes) {
            plane.reposition(nx,
                    pos[1],
                    pos[2]);
            nx += planeThickness;
            plane.resize(planeThickness, size[1], size[2]);
            plane.layout();
        }
    }

    private void addToPlane(LayerView layerView,
            PlaneView plane)
    {
        // Count how many planes of layers we need. One
        // display plane may have more than one layer if
        // several layers are 'siblings'.
        /* func add node, plane
         if node has already been assigned a plane
         return
         add node to plane
         if node has children
         foreach child
         add child, plane+1
         final cleanup, if the output layer is an a plane
         with other layers, move it to a layer by itself.
         */
        if (!planesMap.containsKey(layerView)) {
            Layer layer = layerView.layer;

            planesMap.put(layerView, plane);
            plane.addLayer(layerView);
            // Visit each layer that is on a forward
            // connection from neurons in the current layer.
            layer.accept(new AddChildrenToPlaneVisitor());
        }
    }

    private class AddChildrenToPlaneVisitor extends NetElementVisitor
    {

        private PlaneView plane;

        private AddChildrenToPlaneVisitor()
        {
            this.plane = null;
        }

        @Override
        public boolean visit(Connection conn)
        {
            LayerView layerView
                    = getLayerView(conn.getToNeuron().getLayer());
            if (plane == null) {
                if (!planesMap.containsKey(layerView)) {
                    // First time we've encountered a layer, and another
                    // visitor has not already to create a plane for it,
                    // so make the plane.
                    plane = new PlaneView(NetView.this, myGL, myAUX, myUT);
                    planes.add(plane);
                } else {
                    // Plane already made. Use it.
                    plane = planesMap.get(layerView);
                }
            }
            // Add this layer to the current plane layer.
            addToPlane(layerView,
                    plane);
            return false;
        }
    }

    @Override
    public void reposition(float nx, float ny, float nz)
    {
        super.reposition(nx, ny, nz);
    }

    @Override
    public void render()
    {
        for (LayerView view : layers) {
            view.render();
        }
    }

    class NetViewBuilder extends NetElementVisitor
    {

        @Override
        public boolean visit(Layer layer)
        {
            if (layer.getLayerType() != LayerType.BIAS) {
                addLayer(layer);
            }
            return false;
        }

    }

}
