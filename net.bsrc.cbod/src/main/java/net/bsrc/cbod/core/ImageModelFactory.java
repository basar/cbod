package net.bsrc.cbod.core;

import net.bsrc.cbod.core.model.ImageModel;
import net.bsrc.cbod.opencv.OpenCV;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * User: bsr Date: 30/11/13 Time: 12:37
 */
public final class ImageModelFactory {

	private static final Logger logger = LoggerFactory
			.getLogger(ImageModelFactory.class);

	private static ImageModel _createImageModel(final String imagePath,
			final boolean isCreateMat, final boolean isCreateSegments) {

		Validate.notEmpty(imagePath);

		File file = FileUtils.getFile(imagePath);

		if (!file.exists()) {
			logger.error("Image cannot found:{}", imagePath);
			return null;
		}

		final String imageName = FilenameUtils.getName(imagePath);

		ImageModel model = new ImageModel();
		model.setImagePath(imagePath);
		model.setImageName(imageName);
		model.setFile(file);
		model.setRawImageName(FilenameUtils.removeExtension(imageName));

		if (isCreateMat) {
			model.setMat(OpenCV.getImageMat(imagePath));
		}

		if (isCreateSegments) {
			// TODO segmentleri olusturup image modele setle. Bunun icin
			// imagemodel sinifi icerisinde segmentlerin listesi tutulmali
		}

		return model;
	}

	public static final ImageModel createImageModel(final String imagePath) {
		return _createImageModel(imagePath, false, false);
	}

	public static final ImageModel createImageModel(final String imagePath,
			boolean isCreateMat) {
		return _createImageModel(imagePath, true, false);
	}

	public static final ImageModel createImageMode(final String imagePath,
			boolean isCreateMat, boolean isCreateSegments) {
		return _createImageModel(imagePath, isCreateMat, isCreateSegments);
	}

}
