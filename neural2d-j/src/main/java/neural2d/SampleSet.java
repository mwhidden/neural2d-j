package neural2d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import neural2d.config.SampleConfig;

/**
 * <p>
 * Copyright (c) 2015 Michael C. Whidden
 * @author Michael C. Whidden
 */
public class SampleSet {

    private List<Sample> samples = new ArrayList<>();

    /**
     * Get the value of samples
     *
     * @return the value of samples
     */
    public List<Sample> getSamples()
    {
        return samples;
    }

    public void loadSamples(SampleConfig config)
    {
        samples.addAll(config.getSamples());

        System.out.println(samples.size() + " training samples initialized");
    }

    // Randomize the order of the samples container.
    //
    public void shuffle()
    {
        Collections.shuffle(samples);
    }

    // By clearing the cache, future image access will cause the pixel data to
    // be re-read and converted by whatever color conversion is in effect then.
    public void clearCache()
    {
        for(Sample s: samples){
            s.clearCache();
        }
    }

    public SampleSet()
    {
        samples = new ArrayList<>();
    }

}
