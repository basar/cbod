package net.bsrc.cbod.pascal.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("object")
public class PascalObject {

	private String name;

	private String pose;

	private String truncated;

	private String occluded;

	private String difficult;

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

	public String getTruncated() {
		return truncated;
	}

	public void setTruncated(String truncated) {
		this.truncated = truncated;
	}

	public String getOccluded() {
		return occluded;
	}

	public void setOccluded(String occluded) {
		this.occluded = occluded;
	}

	public PascalBndBox getBndbox() {
		return bndbox;
	}

	public void setBndbox(PascalBndBox bndbox) {
		this.bndbox = bndbox;
	}

	public String getDifficult() {
		return difficult;
	}

	public void setDifficult(String difficult) {
		this.difficult = difficult;
	}

	@Override
	public String toString() {
		return "PascalObject [name=" + name + ", pose=" + pose + ", truncated="
				+ truncated + ", occluded=" + occluded + ", difficult="
				+ difficult + ", bndbox=" + bndbox + "]";
	}
	
	

}
