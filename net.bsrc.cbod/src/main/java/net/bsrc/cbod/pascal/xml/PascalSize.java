package net.bsrc.cbod.pascal.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("size")
public class PascalSize {

	private int width;

	private int height;

	private int depth;

	public PascalSize() {

	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	@Override
	public String toString() {
		return "PascalSize [width=" + width + ", height=" + height + ", depth="
				+ depth + "]";
	}

}
