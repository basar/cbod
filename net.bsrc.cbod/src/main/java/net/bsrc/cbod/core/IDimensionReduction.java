package net.bsrc.cbod.core;

import java.util.List;

/**
 * User: bsr
 * Date: 08/10/14
 * Time: 22:32
 */
public interface IDimensionReduction {

    List<List<Double>> doTransformations(List<List<Double>> list);

    List<Double> doTransformation(List<Double> list);

}
