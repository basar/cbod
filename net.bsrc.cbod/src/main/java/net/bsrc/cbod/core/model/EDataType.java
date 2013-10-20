package net.bsrc.cbod.core.model;

/**
 * User: bsr Date: 10/20/13 Time: 4:43 PM
 */
public enum EDataType {

	TRAIN("train"), TEST("test");

	private String name;

	private EDataType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
