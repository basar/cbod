package net.bsrc.cbod.core.model;

/**
 * User: bsr Date: 10/13/13 Time: 1:32 PM
 */
public enum EDescriptorType {

	CSD("CSD"), // Color structure descriptors
	SCD("SCD"), // Scalable color descriptors
	CLD("CLD"), // Color layout descriptors
	DCD("DCD"), // Dominant color descriptors
	HTD("HTD"), // Homogeneus texture descriptors  (Simdilik kullanilmiyor)
	EHD("EHD"), // Edge histogram descriptors
	SIFT("SIFT"), // SIFT descriptors
	HOG("HOG");// Histogram of Gradient Descriptors

	private String name;

	private EDescriptorType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
