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

	private List<Integer> scalableColorDescriptors = new ArrayList<Integer>();

	private List<Integer> colorLayoutDescriptors = new ArrayList<Integer>();

	private List<Integer> dominantColorDesciptors = new ArrayList<Integer>();

	private List<Integer> homogeneousTextureDescriptors = new ArrayList<Integer>();

    private List<Integer> tempDescriptors = new ArrayList<Integer>();

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

	public List<Integer> getScalableColorDescriptors() {
		return scalableColorDescriptors;
	}

	public void setScalableColorDescriptors(
			List<Integer> scalableColorDescriptors) {
		this.scalableColorDescriptors = scalableColorDescriptors;
	}

	public List<Integer> getColorLayoutDescriptors() {
		return colorLayoutDescriptors;
	}

	public void setColorLayoutDescriptors(List<Integer> colorLayoutDescriptors) {
		this.colorLayoutDescriptors = colorLayoutDescriptors;
	}

	public List<Integer> getDominantColorDesciptors() {
		return dominantColorDesciptors;
	}

	public void setDominantColorDesciptors(List<Integer> dominantColorDesciptors) {
		this.dominantColorDesciptors = dominantColorDesciptors;
	}

    public List<Integer> getHomogeneousTextureDescriptors() {
        return homogeneousTextureDescriptors;
    }

    public void setHomogeneousTextureDescriptors(List<Integer> homogeneousTextureDescriptors) {
        this.homogeneousTextureDescriptors = homogeneousTextureDescriptors;
    }
}
