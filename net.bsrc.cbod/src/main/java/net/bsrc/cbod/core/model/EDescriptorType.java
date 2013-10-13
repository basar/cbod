package net.bsrc.cbod.core.model;

/**
 * User: bsr Date: 10/13/13 Time: 1:32 PM
 */
public enum EDescriptorType {

	CSD("CSD"),
    SCD("SCD"),
    CLD("CLD"),
    DCD("DCD"),
    HTD("HTD"),
    EHD("EHD");

	private String name;

	private EDescriptorType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
