package net.bsrc.cbod.core.model;

import java.util.List;

/**
 * User: bsr Date: 10/13/13 Time: 1:38 PM
 */
public class Descriptor {

	private EDescriptorType type;

	private List<Integer> dataList;

	public Descriptor() {

	}

	public Descriptor(EDescriptorType type) {
		this.type = type;
	}

	public Descriptor(EDescriptorType type, List<Integer> dataList) {
		this.type = type;
		this.dataList = dataList;
	}

	public EDescriptorType getType() {
		return type;
	}

	public void setType(EDescriptorType type) {
		this.type = type;
	}

	public List<Integer> getDataList() {
		return dataList;
	}

	public void setDataList(List<Integer> dataList) {
		this.dataList = dataList;
	}
}
