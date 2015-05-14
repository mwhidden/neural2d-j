package neural2d.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import static neural2d.config.TopologyConfig.findLayer;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Michael C. Whidden
 */
public class TopologyConfigTest
{

    List<LayerConfig> goodList, missingFromList, cycleList;

    public TopologyConfigTest()
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
        // Create a config for a net like this:

        //               L3
        //             /    \
        //           L1--L4--L6
        //          /          \
        //        L0---L2--L5---L7
        List list = new ArrayList<>();

        LayerConfig cfg;
        cfg = new LayerConfig();
        cfg.setLayerName("L0");
        list.add(cfg);

        cfg = new LayerConfig();
        cfg.setLayerName("L1");
        cfg.addFromLayerName("L0");
        list.add(cfg);

        cfg = new LayerConfig();
        cfg.setLayerName("L2");
        cfg.addFromLayerName("L0");
        list.add(cfg);

        cfg = new LayerConfig();
        cfg.setLayerName("L3");
        cfg.addFromLayerName("L1");
        list.add(cfg);

        cfg = new LayerConfig();
        cfg.setLayerName("L4");
        cfg.addFromLayerName("L1");
        list.add(cfg);

        cfg = new LayerConfig();
        cfg.setLayerName("L5");
        cfg.addFromLayerName("L2");
        list.add(cfg);

        cfg = new LayerConfig();
        cfg.setLayerName("L6");
        cfg.addFromLayerName("L3");
        cfg.addFromLayerName("L4");
        list.add(cfg);

        cfg = new LayerConfig();
        cfg.setLayerName("L7");
        cfg.addFromLayerName("L5");
        cfg.addFromLayerName("L6");
        list.add(cfg);
        resolveConfigs(list);

        goodList = list;

        list = new ArrayList<>();

        cfg = new LayerConfig();
        cfg.setLayerName("L0");
        list.add(cfg);

        cfg = new LayerConfig();
        cfg.setLayerName("L1");
        cfg.addFromLayerName("L0");
        list.add(cfg);

        cfg = new LayerConfig();
        cfg.setLayerName("L2");
        // cfg.addFromLayerName("L0"); // L2 has no from layer
        list.add(cfg);

        cfg = new LayerConfig();
        cfg.setLayerName("L3");
        cfg.addFromLayerName("L1");
        list.add(cfg);

        cfg = new LayerConfig();
        cfg.setLayerName("L4");
        cfg.addFromLayerName("L1");
        list.add(cfg);

        cfg = new LayerConfig();
        cfg.setLayerName("L5");
        cfg.addFromLayerName("L2");
        list.add(cfg);

        cfg = new LayerConfig();
        cfg.setLayerName("L6");
        cfg.addFromLayerName("L3");
        cfg.addFromLayerName("L4");
        list.add(cfg);

        cfg = new LayerConfig();
        cfg.setLayerName("L7");
        cfg.addFromLayerName("L5");
        cfg.addFromLayerName("L6");
        list.add(cfg);
        resolveConfigs(list);

        missingFromList = list;

        list = new ArrayList<>();

        cfg = new LayerConfig();
        cfg.setLayerName("L0");
        list.add(cfg);

        cfg = new LayerConfig();
        cfg.setLayerName("L1");
        cfg.addFromLayerName("L0");
        cfg.addFromLayerName("L6"); // Cycle L1->L3/L2->L6->L1
        list.add(cfg);

        cfg = new LayerConfig();
        cfg.setLayerName("L2");
        cfg.addFromLayerName("L0");
        list.add(cfg);

        cfg = new LayerConfig();
        cfg.setLayerName("L3");
        cfg.addFromLayerName("L1");
        list.add(cfg);

        cfg = new LayerConfig();
        cfg.setLayerName("L4");
        cfg.addFromLayerName("L1");
        list.add(cfg);

        cfg = new LayerConfig();
        cfg.setLayerName("L5");
        cfg.addFromLayerName("L2");
        list.add(cfg);

        cfg = new LayerConfig();
        cfg.setLayerName("L6");
        cfg.addFromLayerName("L3");
        cfg.addFromLayerName("L4");
        list.add(cfg);

        cfg = new LayerConfig();
        cfg.setLayerName("L7");
        cfg.addFromLayerName("L5");
        cfg.addFromLayerName("L6");
        list.add(cfg);
        resolveConfigs(list);

        cycleList = list;

    }

    private void resolveConfigs(List<LayerConfig> l)
    {
        for (LayerConfig layer : l) {
            for (String fromLayer : layer.getFromLayerNames()) {
                int idx = findLayer(l, fromLayer);
                layer.addFromLayerConfig(l.get(idx));
            }
        }
    }

    @After
    public void tearDown()
    {
    }

    /**
     * Test of sortLayers method, of class TopologyConfig.
     *
     * @throws neural2d.config.ConfigurationException
     */
    @Test
    public void testSortLayers() throws ConfigurationException
    {
        Random rand = new Random(1);
        // Try different arrangements
        for (int j = 0; j < 10; j++) {
            Collections.shuffle(goodList, rand);
            TopologyConfig.sortLayers(goodList);
            for (int i = 0; i < 8; i++) {
                // There are many valid orders, but the important thing is
                // that every layer's 'from' layers appear before it in the lst.
                for (LayerConfig layer : goodList.get(i).getFromLayerConfigs()) {
                    int idx = TopologyConfig.findLayer(goodList, layer);
                    assert (idx >= 0);
                    assert (idx < i);
                }
            }
            System.out.println();
        }

        try {
            TopologyConfig.sortLayers(missingFromList);
            fail("Hidden layer with no 'from' should have caused exception.");
        } catch (ConfigurationException e) {
            // pass
        }

        try {
            TopologyConfig.sortLayers(cycleList);
            for (int i = 0; i < 8; i++) {
                System.out.print(cycleList.get(i).getLayerName() + ", ");
            }
            fail("Layer configuration with cycle should have caused exception.");
        } catch (ConfigurationException e) {
            // pass
        }

    }

    @Test
    public void testFindLayer()
    {
        assertEquals(0, TopologyConfig.findLayer(goodList, "L0"));
        assertEquals(3, TopologyConfig.findLayer(goodList, "L3"));
        assertEquals(4, TopologyConfig.findLayer(goodList, "L4"));
        assertEquals(7, TopologyConfig.findLayer(goodList, "L7"));
    }

}
