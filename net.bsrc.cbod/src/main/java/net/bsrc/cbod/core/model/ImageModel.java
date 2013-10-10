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

	private List<Integer> colorStructureDescriptors = new ArrayList<Integer>();

	public ImageModel() {

	}

	public List<Integer> getColorStructureDescriptors() {
		return colorStructureDescriptors;
	}

	public void setColorStructureDescriptors(
			List<Integer> colorStructureDescriptors) {
		this.colorStructureDescriptors = colorStructureDescriptors;
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

	@Override
	public String toString() {
		return "ImageModel{" + "imageName='" + imageName + '\''
				+ ", imageFullPath='" + imageFullPath + '\'' + ", mat=" + mat
				+ ", colorStructureDescriptors=" + colorStructureDescriptors
				+ '}';
	}
}
