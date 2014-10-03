package net.bsrc.cbod.core.model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

/**
 * User: bsr Date: 10/10/13 Time: 9:57 PM
 */
@SuppressWarnings("serial")
public class ImageModel implements Serializable {

	/**
	 * Name with suffix (eg: example.jpg)
	 */
	private String imageName;

	/**
	 * Image full path (eg: /usr/local/tmp/example.jpg)
	 */
	private String imagePath;

	private EObjectType objectType;

	/**
	 * image name without suffix (eg: example)
	 */
	private String rawImageName;

	private File file;

	private Mat mat;

	private byte[] data;

	private List<Descriptor> descriptors = new ArrayList<Descriptor>();

	private boolean testImage = false;

	private Rect relativeToOrg;

	public ImageModel() {

	}

	public Mat getMat() {
		return mat;
	}

	public void setMat(Mat mat) {
		this.mat = mat;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
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

	public List<Double> getDescriptorDataList(EDescriptorType type) {

        Validate.notNull(type);

        List<Double> dataList = null;
		Descriptor descriptor = getDescriptor(type);
		if (descriptor != null) {
			dataList = descriptor.getDataList();
		}

		return dataList != null ? dataList : new ArrayList<Double>();
	}

	public static List<List<Double>> getDescriptorDataLists(
            List<ImageModel> modelList, EDescriptorType type) {

        Validate.notEmpty(modelList);

		List<List<Double>> result = new ArrayList<List<Double>>();

        for (ImageModel imageModel : modelList) {
            result.add(imageModel.getDescriptorDataList(type));
        }

        return result;
	}

	public void setRawImageName(String rawImageName) {
		this.rawImageName = rawImageName;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public Rect getRelativeToOrg() {
		return relativeToOrg;
	}

	public void setRelativeToOrg(Rect relativeToOrg) {
		this.relativeToOrg = relativeToOrg;
	}

	public EObjectType getObjectType() {
		return objectType;
	}

	public void setObjectType(EObjectType objectType) {
		this.objectType = objectType;
	}

	public boolean isTestImage() {
		return testImage;
	}

	public void setTestImage(boolean testImage) {
		this.testImage = testImage;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		ImageModel that = (ImageModel) o;

		if (rawImageName != null ? !rawImageName.equals(that.rawImageName)
				: that.rawImageName != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return rawImageName != null ? rawImageName.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "ImageModel [imageName=" + imageName + ", imagePath="
				+ imagePath + ", objectType=" + objectType + ", testImage="
				+ testImage + "]";
	}

}
