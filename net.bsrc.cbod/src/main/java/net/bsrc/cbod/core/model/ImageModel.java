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

	/**
	 * Name with suffix
	 */
	private String imageName;

	private String imagePath;

	private String objectClassType;

	private String objectPart;

	/**
	 * image name without suffix
	 */
	private String rawImageName;

	private File file;

	private Mat mat;

	private List<Descriptor> descriptors = new ArrayList<Descriptor>();

	private boolean negativeImg = false;

	private EDataType dataType;

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

	public boolean isNegativeImg() {
		return negativeImg;
	}

	public void setNegativeImg(boolean negativeImg) {
		this.negativeImg = negativeImg;
	}

	public EDataType getDataType() {
		return dataType;
	}

	public void setDataType(EDataType dataType) {
		this.dataType = dataType;
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

	public String getObjectPart() {
		return objectPart;
	}

	public void setObjectPart(String objectPart) {
		this.objectPart = objectPart;
	}

	public void setRawImageName(String rawImageName) {
		this.rawImageName = rawImageName;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getObjectClassType() {
		return objectClassType;
	}

	public void setObjectClassType(String objectClassType) {
		this.objectClassType = objectClassType;
	}

    @Override
    public String toString() {
        return "ImageModel{" +
                "negativeImg=" + negativeImg +
                ", dataType=" + dataType +
                ", rawImageName='" + rawImageName + '\'' +
                ", objectPart='" + objectPart + '\'' +
                ", objectClassType='" + objectClassType + '\'' +
                ", imageName='" + imageName + '\'' +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }
}
