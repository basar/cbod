package net.bsrc.cbod.core;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;

import java.util.List;

/**
 * User: bsr Date: 06/10/14 Time: 20:47
 */
public class MinMaxNormalization implements INormalization {

	@Override
	public void applyNormalization(List<Double> datas) {

		double[] dataArr = ArrayUtils.toPrimitive(datas
				.toArray(new Double[datas.size()]));
		final double min = StatUtils.min(dataArr);
		final double max = StatUtils.max(dataArr);

		for (int i = 0; i < datas.size(); i++) {
			double x = datas.get(i);
			datas.set(i, (x - min) / (max - min));
		}

	}

	@Override
	public void applyNormalizations(List<List<Double>> datas) {

		for (List<Double> dataList : datas) {
			applyNormalization(dataList);
		}
	}
}
