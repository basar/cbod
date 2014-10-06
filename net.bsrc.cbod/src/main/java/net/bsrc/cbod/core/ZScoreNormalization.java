package net.bsrc.cbod.core;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;

import java.util.List;

/**
 * User: bsr Date: 07/10/14 Time: 00:02
 */
public class ZScoreNormalization implements INormalization {

	@Override
	public void applyNormalization(List<Double> datas) {

		double[] dataArr = ArrayUtils.toPrimitive(datas
				.toArray(new Double[datas.size()]));
		final double mean = StatUtils.mean(dataArr);
		final double standartDeviation = Math.sqrt(StatUtils.variance(dataArr));

		for (int i = 0; i < datas.size(); i++) {
			double x = datas.get(i);
			double z = (x - mean) / standartDeviation;
			datas.set(i, z);
		}
	}

	@Override
	public void applyNormalizations(List<List<Double>> datas) {
        for (List<Double> dataList : datas) {
            applyNormalization(dataList);
        }
	}
}
