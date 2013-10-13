package net.bsrc.cbod.core.model;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;

/**
 * User: bsr Date: 10/10/13 Time: 9:57 PM
 */
public class ImageModel {

	private String imageName;

	private String imageFullPath;

	private Mat mat;

	private List<Descriptor> descriptors = new ArrayList<Descriptor>();

	public ImageModel() {

	}

	public Mat getMat() {
		return mat;
	}

	public void setMat(Mat mat) {
		this.mat = mat;
	}

	public String getImageFullPath() {
		return imageFullPath;
	}

	public void setImageFullPath(String imageFullPath) {
		this.imageFullPath = imageFullPath;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public List<Descriptor> getDescriptors() {
		return descriptors;
	}

	public void setDescriptors(List<Descriptor> descriptors) {
		this.descriptors = descriptors;
	}
}
