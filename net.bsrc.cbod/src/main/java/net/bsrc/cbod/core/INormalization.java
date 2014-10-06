package net.bsrc.cbod.core;

import java.util.Collection;
import java.util.List;

/**
 * User: bsr Date: 06/10/14 Time: 20:36
 */
public interface INormalization {

	void applyNormalization(List<Double> datas);

	void applyNormalizations(List<List<Double>> datas);

}
