package net.bsrc.cbod.svm.libsvm;

import net.bsrc.cbod.core.util.CBODUtil;

/**
 * User: bsr Date: 10/16/13 Time: 1:18 PM
 */
public class ScaleParameter {

	/**
	 * x scaling lower limit (default -1)
	 */
	private Integer lower = -1;

	/**
	 * x scaling upper limit (default +1)
	 */
	private Integer upper = 1;

	/**
	 * save scaling parameters to file
	 */
	private String saveFileName;

	/**
	 * restore scaling parameters from restoreFilePath
	 */
	private String restoreFileName;

	/**
	 * default constructor
	 */
	public ScaleParameter() {

	}

	public Integer getLower() {
		return lower;
	}

	public void setLower(Integer lower) {
		this.lower = lower;
	}

	public Integer getUpper() {
		return upper;
	}

	public void setUpper(Integer upper) {
		this.upper = upper;
	}

	public String getSaveFileName() {
		return saveFileName;
	}

	public void setSaveFileName(String saveFileName) {
		this.saveFileName = saveFileName;
	}

	public String getRestoreFileName() {
		return restoreFileName;
	}

	public void setRestoreFileName(String restoreFileName) {
		this.restoreFileName = restoreFileName;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();

		CBODUtil.appendParam(sb, "-l", new Object[] { lower });
		CBODUtil.appendParam(sb, "-u", new Object[] { upper });
		CBODUtil.appendParam(sb, "-s", new Object[] { saveFileName });
		CBODUtil.appendParam(sb, "-r", new Object[] { restoreFileName });

		return sb.toString();

	}
}
