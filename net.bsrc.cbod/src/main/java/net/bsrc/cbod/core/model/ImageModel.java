package net.bsrc.cbod.core.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.opencv.core.Mat;

/**
 * User: bsr Date: 10/10/13 Time: 9:57 PM
 */
public class ImageModel {

	private String imageName;

	private String imagePath;

	/**
	 * image name without suffix
	 */
	private String rawImageName;

	private File file;

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

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public File getFile() {
		if (file == null) {
			file = FileUtils.getFile(imagePath);
		}
		return file;
	}

	public String getRawImageName() {
		if (StringUtils.isEmpty(rawImageName)) {
			rawImageName = FilenameUtils.removeExtension(imageName);
		}
		return rawImageName;
	}

	public Descriptor getDescriptor(EDescriptorType type) {
		if (type == null)
			return null;
		for (Descriptor descriptor : getDescriptors()) {
			if (descriptor.getType() == type) {
				return descriptor;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "ImageModel{" + "imageName='" + imageName + '\''
				+ ", imagePath='" + imagePath + '\'' + ", rawImageName='"
				+ rawImageName + '\'' + '}';
	}
}
