package net.bsrc.cbod.pascal.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("source")
public class PascalSource {

	private String database;

	private String annotation;

	private String image;

	public PascalSource() {

	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	@Override
	public String toString() {
		return "PascalSource [database=" + database + ", annotation="
				+ annotation + ", image=" + image + "]";
	}

}
