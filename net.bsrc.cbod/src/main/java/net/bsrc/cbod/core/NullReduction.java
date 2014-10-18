package net.bsrc.cbod.core;

import java.util.List;

/**
 * User: bsr
 * Date: 12/10/14
 * Time: 19:23
 */
public class NullReduction implements IDimensionReduction {
    @Override
    public List<List<Double>> doTransformations(List<List<Double>> list) {
        //Do nothing
        return list;
    }

    @Override
    public List<Double> doTransformation(List<Double> list) {
        //Do nothing
        return list;
    }
}
