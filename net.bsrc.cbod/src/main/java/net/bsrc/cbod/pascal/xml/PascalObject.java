package net.bsrc.cbod.pascal.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("object")
public class PascalObject {

	private String name;

	private String pose;

	private int truncated;

	private int occluded;

	private int difficult;

	private PascalBndBox bndbox;

	public PascalObject() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPose() {
		return pose;
	}

	public void setPose(String pose) {
		this.pose = pose;
	}

	public boolean isTruncated() {
		return truncated == 1;
	}

	public void setTruncated(int truncated) {
		this.truncated = truncated;
	}

	public boolean isOccluded() {
		return occluded == 1;
	}

	public void setOccluded(int occluded) {
		this.occluded = occluded;
	}

	public PascalBndBox getBndbox() {
		return bndbox;
	}

	public void setBndbox(PascalBndBox bndbox) {
		this.bndbox = bndbox;
	}

	public boolean isDifficult() {
		return difficult == 1;
	}

	public void setDifficult(int difficult) {
		this.difficult = difficult;
	}

	@Override
	public String toString() {
		return "PascalObject [name=" + name + ", pose=" + pose + ", truncated="
				+ truncated + ", occluded=" + occluded + ", difficult="
				+ difficult + ", bndbox=" + bndbox + "]";
	}

}
