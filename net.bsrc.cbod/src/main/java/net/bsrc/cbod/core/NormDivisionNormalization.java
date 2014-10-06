package net.bsrc.cbod.core;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.linear.MatrixUtils;

import java.util.List;

/**
 * User: bsr Date: 07/10/14 Time: 00:13
 */
public class NormDivisionNormalization implements INormalization {

	@Override
	public void applyNormalization(List<Double> datas) {

		double[] dataArr = ArrayUtils.toPrimitive(datas
				.toArray(new Double[datas.size()]));
		double norm = MatrixUtils.createRealVector(dataArr).getNorm();

		for (int i = 0; i < datas.size(); i++) {
			double newValue = (datas.get(i) / norm);
			datas.set(i, newValue);
		}

	}

	@Override
	public void applyNormalizations(List<List<Double>> datas) {

		for (List<Double> dataList : datas) {
			applyNormalization(dataList);
		}
	}
}
