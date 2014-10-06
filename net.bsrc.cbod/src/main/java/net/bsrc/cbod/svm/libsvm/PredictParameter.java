package net.bsrc.cbod.svm.libsvm;

import net.bsrc.cbod.core.util.CBODUtil;

/**
 * User: bsr Date: 10/18/13 Time: 4:08 PM
 */
public class PredictParameter {

	/**
	 * whether to predict probability estimates, 0 or 1 (default 0); for
	 * one-class SVM only 0 is supported
	 */
	private Integer probabilityEstimates = null;

	public PredictParameter() {
	}

	public Integer getProbabilityEstimates() {
		return probabilityEstimates;
	}

	public void setProbabilityEstimates(Integer probabilityEstimates) {
		this.probabilityEstimates = probabilityEstimates;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();

		CBODUtil.appendParam(sb, "-b", new Object[] { probabilityEstimates });

		return sb.toString();

	}
}
