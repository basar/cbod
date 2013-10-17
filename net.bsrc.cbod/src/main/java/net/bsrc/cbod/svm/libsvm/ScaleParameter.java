package net.bsrc.cbod.svm.libsvm;

import org.apache.commons.lang.StringUtils;

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

		sb.append("-l").append(" ");
		sb.append(lower).append(" ");

		sb.append("-u").append(" ");
		sb.append(upper).append(" ");

		if (!StringUtils.isEmpty(saveFileName)) {
			sb.append("-s").append(" ");
			sb.append(saveFileName).append(" ");
		}

		if (!StringUtils.isEmpty(restoreFileName)) {
			sb.append("-r").append(" ");
			sb.append(restoreFileName).append(" ");
		}

		return sb.toString();

	}
}
