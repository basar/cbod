package net.bsrc.cbod.main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.bsrc.cbod.core.CBODConstants;
import net.bsrc.cbod.core.model.EDescriptorType;
import net.bsrc.cbod.core.model.ImageModel;
import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.jseg.JSEG;
import net.bsrc.cbod.jseg.JSEGParameter;
import net.bsrc.cbod.mpeg.bil.BilMpeg7Fex;
import net.bsrc.cbod.opencv.OpenCV;
import net.bsrc.cbod.pascal.EPascalType;
import net.bsrc.cbod.pascal.PascalVOC;
import net.bsrc.cbod.pascal.xml.PascalAnnotation;
import net.bsrc.cbod.pascal.xml.PascalBndBox;
import net.bsrc.cbod.pascal.xml.PascalObject;
import net.bsrc.cbod.pascal.xml.PascalXMLHelper;
import net.bsrc.cbod.svm.libsvm.LibSvm;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private final static Logger logger = LoggerFactory.getLogger(Main.class);

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args) {

		extractDescriptors();

	}

	private static void extractDescriptors() {

		BilMpeg7Fex mpegFex = BilMpeg7Fex.getInstance();

		String cbodDirPath = CBODUtil.getDefaultOutputDirectoryPath();

		List<ImageModel> positiveImageModelList = new ArrayList<ImageModel>();
		List<ImageModel> negativeImageModelList = new ArrayList<ImageModel>();

		String positiveImageDirPath = cbodDirPath.concat("/train_data/tire");
		String negativeImageDirPath = cbodDirPath
				.concat("/train_data/negative");

		for (String imgPath : CBODUtil.getFileList(positiveImageDirPath,
				CBODConstants.JPEG_SUFFIX)) {

			ImageModel imgModel = new ImageModel();
			imgModel.setImagePath(imgPath);
			imgModel.setImageName(FilenameUtils.getName(imgPath));

			positiveImageModelList.add(imgModel);

		}

		for (String imgPath : CBODUtil.getFileList(negativeImageDirPath,
				CBODConstants.JPEG_SUFFIX)) {

			ImageModel imgModel = new ImageModel();
			imgModel.setImagePath(imgPath);
			imgModel.setImageName(FilenameUtils.getName(imgPath));

			negativeImageModelList.add(imgModel);

		}

		mpegFex.extractEdgeHistogramDescriptors(positiveImageModelList);
		mpegFex.extractEdgeHistogramDescriptors(negativeImageModelList);

		LibSvm libSvm = LibSvm.getInstance();

		libSvm.createTrainDataFile(cbodDirPath.concat("/svm/ehd_model.txt"), 0, negativeImageModelList, 1,
				positiveImageModelList, EDescriptorType.EHD);

	}

	private static void segmentNegativeImages() {

		PascalVOC pascal = PascalVOC.getInstance();

		List<ImageModel> imageModelList = pascal.getImageModels(
				EPascalType.CAR, 2, -1);

		String directoryName = CBODUtil.getDefaultOutputDirectoryPath().concat(
				"/train_data/negative");

		for (int i = 0; i < 10; i++) {

			ImageModel imgModel = imageModelList.get(i);
			String imageRawName = imgModel.getRawImageName();

			JSEGParameter jsegParam = new JSEGParameter(imgModel.getImagePath());
			jsegParam.setRegionMapFileName(directoryName.concat("/").concat(
					imageRawName + CBODConstants.MAP_SUFFIX));
			jsegParam.setOutputFileImage(directoryName
					.concat("/")
					.concat(imageRawName)
					.concat(CBODConstants.SEG_SUFFIX
							+ CBODConstants.JPEG_SUFFIX));
			String mapName = jsegParam.getRegionMapFileName();

			JSEG.getInstance().execute(jsegParam);

			List<Mat> regions = OpenCV.getSegmentedRegions(
					imgModel.getImagePath(), mapName, true);

			for (int r = 0; r < regions.size(); r++) {

				Mat region = regions.get(r);
				String regionName = directoryName
						.concat("/")
						.concat(imageRawName)
						.concat(CBODConstants.SEG_SUFFIX + r
								+ CBODConstants.JPEG_SUFFIX);
				// Save region file to disk
				OpenCV.writeImage(region, regionName);
			}
		}

	}

	/**
     *
     */
	public static void cropVehiclesFromImageDataSetAndSegmentThem() {

		String outputDir = CBODUtil.getDefaultOutputDirectory()
				.getAbsolutePath();
		String vehicleOutputDir = outputDir.concat("/Vehicle");
		PascalVOC pascal = PascalVOC.getInstance();

		List<ImageModel> imageModelList = pascal.getImageModels(
				EPascalType.CAR, 2, 1);
		List<String> segmentedImageNames = new ArrayList<String>();

		final String jpg = CBODConstants.JPEG_SUFFIX;
		final String seg = CBODConstants.SEG_SUFFIX;
		final String map = CBODConstants.MAP_SUFFIX;

		for (int i = 0; i < imageModelList.size(); i++) {

			ImageModel imgModel = imageModelList.get(i);

			String imgName = imgModel.getRawImageName();
			String imgPath = imgModel.getImagePath();

			PascalAnnotation ann = PascalXMLHelper.fromXML(pascal
					.getAnnotationXML(imgName));

			List<PascalObject> objectList = getTrainedPascalObjectList(ann);

			for (int j = 0; j < objectList.size(); j++) {

				PascalObject po = objectList.get(j);

				Mat crop = OpenCV.getImageMat(imgPath, po.getBndbox());

				String outputImgPath = vehicleOutputDir
						.concat("/" + po.getPose() + "/")
						.concat(imgName + "_" + j).concat(jpg);
				OpenCV.writeImage(crop, outputImgPath);
				segmentedImageNames.add(outputImgPath);
			}
		}

		for (int i = 0; i < segmentedImageNames.size(); i++) {

			String imageName = segmentedImageNames.get(i);
			File imageFile = FileUtils.getFile(imageName);
			File parentDir = imageFile.getParentFile();

			String imageRawName = imageFile.getName().replaceAll(jpg, "");
			String directoryName = parentDir.getAbsolutePath().concat("/")
					.concat(imageRawName + seg);

			CBODUtil.createDirectory(directoryName);

			// JSEG Parameter
			JSEGParameter jsegParam = new JSEGParameter(imageName);
			jsegParam.setRegionMapFileName(directoryName.concat("/").concat(
					imageRawName + map));
			jsegParam.setOutputFileImage(directoryName.concat("/")
					.concat(imageRawName).concat(seg + jpg));

			String mapName = jsegParam.getRegionMapFileName();
			JSEG.getInstance().execute(jsegParam);

			List<Mat> regions = OpenCV.getSegmentedRegions(imageName, mapName,
					true);

			for (int r = 0; r < regions.size(); r++) {

				Mat region = regions.get(r);
				String regionName = directoryName.concat("/")
						.concat(imageRawName).concat(seg + r + jpg);
				// Save region file to disk
				OpenCV.writeImage(region, regionName);
			}

		}
	}

	public static List<PascalObject> getTrainedPascalObjectList(
			PascalAnnotation ann) {

		List<PascalObject> result = new ArrayList<PascalObject>();

		List<PascalObject> list = ann.getObjectList(EPascalType.CAR);
		for (PascalObject po : list) {
			if (!po.isDifficult() && !po.isTruncated() && !po.isOccluded()) {
				// Size hesaplamasi
				PascalBndBox box = po.getBndbox();
				int width = box.getXmax() - box.getXmin();
				int height = box.getYmax() - box.getYmin();
				if (width >= JSEG.MIN_IMG_WIDTH
						&& height >= JSEG.MIN_IMG_HEIGHT) {
					result.add(po);
				}
			}
		}

		return result;
	}

}
