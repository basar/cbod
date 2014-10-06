package net.bsrc.cbod.core;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.rank.Median;

import java.util.List;

/**
 * User: bsr Date: 07/10/14 Time: 00:09
 */
public class MedianNormalization implements INormalization {
	@Override
	public void applyNormalization(List<Double> datas) {

		double[] dataArr = ArrayUtils.toPrimitive(datas
				.toArray(new Double[datas.size()]));

		Median medianOrginalArr = new Median();
		medianOrginalArr.setData(dataArr);
		final double dataArrMedian = medianOrginalArr.evaluate();

		/**
		 * double[] tempArr = new double[dataList.size()];
		 * 
		 * for(int i=0;i<tempArr.length;i++){ tempArr[i]=Math.abs(dataArr[i] -
		 * dataArrMedian); }
		 * 
		 * Median medianTempArr = new Median(); medianTempArr.setData(tempArr);
		 * //median absolute deviation final double mad =
		 * medianTempArr.evaluate();
		 **/

		for (int i = 0; i < datas.size(); i++) {
			double newValue = (datas.get(i) - dataArrMedian);
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
