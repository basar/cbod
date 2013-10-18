package net.bsrc.cbod.svm.libsvm;

import net.bsrc.cbod.core.util.CBODUtil;

/**
 * User: bsr Date: 10/17/13 Time: 1:40 PM
 */
public class TrainParameter {

	/**
	 * set type of SVM <br/>
	 * (default 0) 0 -- C-SVC (multi-class classification) <br/>
	 * 1 -- nu-SVC (multi-class classification) <br/>
	 * 2 -- one-class SVM <br/>
	 * 3 -- epsilon-SVR (regression) <br/>
	 * 4 -- nu-SVR (regression) <br/>
	 */
	private Integer svmType = null;
	/**
	 * set type of kernel function (default 2) 0 -- linear: u'*v <br/>
	 * 1 -- polynomial: (gamma*u'*v + coef0)^degree <br/>
	 * 2 -- radial basis function: exp(-gamma*|u-v|^2) <br/>
	 * 3 -- sigmoid: tanh(gamma*u'*v + coef0) <br/>
	 * 4 -- precomputed kernel (kernel values in training_set_file) <br/>
	 */
	private Integer kernelType = null;
	/**
	 * set degree in kernel function (default 3)
	 */
	private Integer degree = null;

	/**
	 * set gamma in kernel function (default 1/num_features)
	 */
	private Float gamma = null;

	/**
     *
     */
	private Float cost = null;

	public TrainParameter() {

	}

	public Integer getSvmType() {
		return svmType;
	}

	public void setSvmType(Integer svmType) {
		this.svmType = svmType;
	}

	public Integer getKernelType() {
		return kernelType;
	}

	public void setKernelType(Integer kernelType) {
		this.kernelType = kernelType;
	}

	public Integer getDegree() {
		return degree;
	}

	public void setDegree(Integer degree) {
		this.degree = degree;
	}

	public Float getGamma() {
		return gamma;
	}

	public void setGamma(Float gamma) {
		this.gamma = gamma;
	}

	public Float getCost() {
		return cost;
	}

	public void setCost(Float cost) {
		this.cost = cost;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();

		CBODUtil.appendParam(sb, "-s", svmType);
		CBODUtil.appendParam(sb, "-t", kernelType);
		CBODUtil.appendParam(sb, "-d", degree);
		CBODUtil.appendParam(sb, "-g", gamma);
		CBODUtil.appendParam(sb, "-c", cost);

		return sb.toString();

	}

}
