package net.bsrc.cbod.pascal.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("bndbox")
public class PascalBndBox {

	private int xmin;

	private int ymin;

	private int xmax;

	private int ymax;

	public PascalBndBox() {

	}

	public int getXmin() {
		return xmin;
	}

	public void setXmin(int xmin) {
		this.xmin = xmin;
	}

	public int getYmin() {
		return ymin;
	}

	public void setYmin(int ymin) {
		this.ymin = ymin;
	}

	public int getXmax() {
		return xmax;
	}

	public void setXmax(int xmax) {
		this.xmax = xmax;
	}

	public int getYmax() {
		return ymax;
	}

	public void setYmax(int ymax) {
		this.ymax = ymax;
	}

	@Override
	public String toString() {
		return "PascalBndBox [xmin=" + xmin + ", ymin=" + ymin + ", xmax="
				+ xmax + ", ymax=" + ymax + "]";
	}

}
