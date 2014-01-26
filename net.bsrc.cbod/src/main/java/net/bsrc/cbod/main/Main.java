package net.bsrc.cbod.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.bsrc.cbod.core.CBODConstants;
import net.bsrc.cbod.core.ImageModelFactory;
import net.bsrc.cbod.core.model.EDataType;
import net.bsrc.cbod.core.model.EDescriptorType;
import net.bsrc.cbod.core.model.ImageModel;
import net.bsrc.cbod.core.persistence.DB4O;
import net.bsrc.cbod.core.persistence.ImageModelService;
import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.experiment.CbodExperiment;
import net.bsrc.cbod.jseg.JSEG;
import net.bsrc.cbod.jseg.JSEGParameter;
import net.bsrc.cbod.jseg.JSEGParameterFactory;
import net.bsrc.cbod.mpeg.bil.BilMpeg7Fex;
import net.bsrc.cbod.opencv.OpenCV;
import net.bsrc.cbod.pascal.EPascalType;
import net.bsrc.cbod.pascal.PascalVOC;
import net.bsrc.cbod.pascal.xml.PascalAnnotation;
import net.bsrc.cbod.pascal.xml.PascalBndBox;
import net.bsrc.cbod.pascal.xml.PascalObject;
import net.bsrc.cbod.pascal.xml.PascalXMLHelper;
import net.bsrc.cbod.svm.libsvm.LibSvm;
import net.bsrc.cbod.svm.libsvm.ScaleParameter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.Validate;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.management.resources.agent_it;

public class Main {

	private final static Logger logger = LoggerFactory.getLogger(Main.class);

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	private final static String IMG_DIR = CBODUtil.getCbodInputImageDirectory()
			+ "/";
	private final static String TMP_DIR = CBODUtil.getCbodTempDirectory() + "/";

	public static void main(String[] args) {

		JSEGParameter param = JSEGParameterFactory.createJSEGParameter(IMG_DIR
				+ "/test_1.jpg", CBODUtil.getCbodTempDirectory());
        //param.setColorQuantizationThreshold(1);
        //param.setRegionMergeThreshold(0.0);
        JSEG.doSegmentation(param);

		DB4O.getInstance().close();
	}

	private static void testObjectDetection(String imageName) {

		// Test yapilacak image
		ImageModel imageModel = ImageModelFactory.createImageModel(IMG_DIR
				+ imageName, true);

		// Image segmentlere ayriliyor
		List<ImageModel> imageSegments = CBODDemo.segmentImage(imageModel
				.getImagePath());

		findCandidateTires(imageModel, imageSegments);
		// findCandidateWindows(imageModel, imageSegments);

	}

	/**
	 * 
	 * @param imageModel
	 * @param imageSegments
	 */
	private static void findCandidateTires(ImageModel imageModel,
			List<ImageModel> imageSegments) {

		Scalar green = new Scalar(0, 255, 0);

		String tireModelFile = "EHD_TIRE.train.scale.model.txt";
		String tireRangeFile = "EHD_TIRE.range.txt";
		EDescriptorType tireDescriptorType = EDescriptorType.EHD;

		doPredict(imageModel, imageSegments, tireModelFile, tireRangeFile,
				tireDescriptorType, green);

	}

	/**
	 * 
	 * @param imageModel
	 * @param imageSegments
	 */
	private static void findCandidateWindows(ImageModel imageModel,
			List<ImageModel> imageSegments) {

		Scalar blue = new Scalar(255, 195, 0);

		String windowModelFile = "SCD_WINDOW.train.scale.model.txt";
		String windowRangeFile = "SCD_WINDOW.range.txt";
		EDescriptorType windowDescriptorType = EDescriptorType.SCD;

		doPredict(imageModel, imageSegments, windowModelFile, windowRangeFile,
				windowDescriptorType, blue);
	}

	private static void createModelFiles() {

		EDescriptorType tireDescType = EDescriptorType.EHD;
		EDescriptorType windowDescType = EDescriptorType.CLD;
		String tireFilePreffix = "EHD_TIRE";
		String windowFilePreffix = "CLD_WINDOW";

		ImageModelService service = ImageModelService.getInstance();

		List<ImageModel> imageModelsTire = service
				.getImageModelList(CBODConstants.CAR_TIRE_PART);
		List<ImageModel> negativeImageModelsTire = service
				.getRandomNegativeImageModelList(imageModelsTire.size());

		List<ImageModel> imageModelsWindow = service
				.getImageModelList(CBODConstants.CAR_WINDOW_PART);
		List<ImageModel> negativeImageModelsWindow = service
				.getRandomNegativeImageModelList(imageModelsWindow.size());

		String[] arr = CBODDemo.createScaledTrainFileAndRangeFile(
				tireFilePreffix, tireDescType, imageModelsTire,
				negativeImageModelsTire);

		CBODDemo.createModelFile(arr[0]);

		arr = CBODDemo.createScaledTrainFileAndRangeFile(windowFilePreffix,
				windowDescType, imageModelsWindow, negativeImageModelsWindow);

		CBODDemo.createModelFile(arr[0]);

	}

	private static void doPredict(ImageModel imageModel,
			List<ImageModel> imageSegments, String modelFile, String rangeFile,
			EDescriptorType descriptorType, Scalar scalar) {

		List<ImageModel> candidates = CBODDemo.doPredict(imageSegments,
				modelFile, rangeFile, descriptorType);

		Mat copy = OpenCV.copyImage(imageModel.getMat());

		for (ImageModel candidate : candidates) {
			Rect rect = candidate.getRelativeToOrg();
			OpenCV.drawRect(rect, copy, scalar);
		}

		StringBuilder sb = new StringBuilder();
		sb.append(imageModel.getRawImageName()).append(".");
		sb.append(descriptorType.getName().toLowerCase()).append(".out.jpg");

		String outputImagePath = TMP_DIR.concat(sb.toString());
		OpenCV.writeImage(copy, outputImagePath);

	}

	private static void saveImageModelsToDB() {

		String cbodDirPath = CBODUtil.getDefaultOutputDirectoryPath();

		List<ImageModel> positiveImageModelList = new ArrayList<ImageModel>();
		List<ImageModel> negativeImageModelList = new ArrayList<ImageModel>();

		List<ImageModel> testPositiveImageModelList = new ArrayList<ImageModel>();
		List<ImageModel> testNegativeImageModelList = new ArrayList<ImageModel>();

		String tireImageDirPath = cbodDirPath.concat("/train_data/tire");
		String windowImageDirPath = cbodDirPath.concat("/train_data/window");
		String negativeImageDirPath = cbodDirPath
				.concat("/train_data/negative");

		List<String> fileList = CBODUtil.getFileList(tireImageDirPath,
				CBODConstants.JPEG_SUFFIX);

		for (int i = 0; i < fileList.size(); i++) {

			String imgPath = fileList.get(i);

			ImageModel imgModel = new ImageModel();
			imgModel.setImagePath(imgPath);
			imgModel.setImageName(FilenameUtils.getName(imgPath));
			imgModel.setObjectClassType(CBODConstants.CAR_OBJECT_CLASS_TYPE);
			imgModel.setObjectPart(CBODConstants.CAR_TIRE_PART);

			if (i % 4 == 0) {
				imgModel.setDataType(EDataType.TEST);
				testPositiveImageModelList.add(imgModel);
			} else {
				imgModel.setDataType(EDataType.TRAIN);
				positiveImageModelList.add(imgModel);
			}

		}

		fileList = CBODUtil.getFileList(windowImageDirPath,
				CBODConstants.JPEG_SUFFIX);

		for (int i = 0; i < fileList.size(); i++) {

			String imgPath = fileList.get(i);

			ImageModel imgModel = new ImageModel();
			imgModel.setImagePath(imgPath);
			imgModel.setImageName(FilenameUtils.getName(imgPath));
			imgModel.setObjectClassType(CBODConstants.CAR_OBJECT_CLASS_TYPE);
			imgModel.setObjectPart(CBODConstants.CAR_WINDOW_PART);

			if (i % 4 == 0) {
				imgModel.setDataType(EDataType.TEST);
				testPositiveImageModelList.add(imgModel);
			} else {
				imgModel.setDataType(EDataType.TRAIN);
				positiveImageModelList.add(imgModel);
			}

		}

		fileList = CBODUtil.getFileList(negativeImageDirPath,
				CBODConstants.JPEG_SUFFIX);

		for (int i = 0; i < fileList.size(); i++) {

			String imgPath = fileList.get(i);

			ImageModel imgModel = new ImageModel();
			imgModel.setImagePath(imgPath);
			imgModel.setImageName(FilenameUtils.getName(imgPath));
			imgModel.setNegativeImg(true);

			if (i % 4 == 0) {
				imgModel.setDataType(EDataType.TEST);
				testNegativeImageModelList.add(imgModel);
			} else {
				imgModel.setDataType(EDataType.TRAIN);
				negativeImageModelList.add(imgModel);
			}

		}

		BilMpeg7Fex mpegFex = BilMpeg7Fex.getInstance();

		mpegFex.extractColorStructureDescriptors(positiveImageModelList, 256);
		mpegFex.extractColorStructureDescriptors(testPositiveImageModelList,
				256);
		mpegFex.extractColorStructureDescriptors(negativeImageModelList, 256);
		mpegFex.extractColorStructureDescriptors(testNegativeImageModelList,
				256);

		mpegFex.extractScalableColorDescriptors(positiveImageModelList, 256);
		mpegFex.extractScalableColorDescriptors(testPositiveImageModelList, 256);
		mpegFex.extractScalableColorDescriptors(negativeImageModelList, 256);
		mpegFex.extractScalableColorDescriptors(testNegativeImageModelList, 256);

		mpegFex.extractColorLayoutDescriptors(positiveImageModelList, 64, 28);
		mpegFex.extractColorLayoutDescriptors(testPositiveImageModelList, 64,
				28);
		mpegFex.extractColorLayoutDescriptors(negativeImageModelList, 64, 28);
		mpegFex.extractColorLayoutDescriptors(testNegativeImageModelList, 64,
				28);

		mpegFex.extractDominantColorDescriptors(positiveImageModelList, 1, 0,
				1, 32, 32, 32);
		mpegFex.extractDominantColorDescriptors(testPositiveImageModelList, 1,
				0, 1, 32, 32, 32);
		mpegFex.extractDominantColorDescriptors(negativeImageModelList, 1, 0,
				1, 32, 32, 32);
		mpegFex.extractDominantColorDescriptors(testNegativeImageModelList, 1,
				0, 1, 32, 32, 32);

		mpegFex.extractEdgeHistogramDescriptors(positiveImageModelList);
		mpegFex.extractEdgeHistogramDescriptors(testPositiveImageModelList);
		mpegFex.extractEdgeHistogramDescriptors(negativeImageModelList);
		mpegFex.extractEdgeHistogramDescriptors(testNegativeImageModelList);

		ImageModelService service = ImageModelService.getInstance();

		service.saveImageModelList(positiveImageModelList);
		service.saveImageModelList(negativeImageModelList);
		service.saveImageModelList(testPositiveImageModelList);
		service.saveImageModelList(testNegativeImageModelList);

	}

	private static void saveWholeImageModelsToDB() {

		PascalVOC pascalVOC = PascalVOC.getInstance();

		List<ImageModel> trainCarImages = pascalVOC.getImageModels(
				EPascalType.CAR, 0, 1);
		List<ImageModel> testCarImages = pascalVOC.getImageModels(
				EPascalType.CAR, 1, 1);
		List<ImageModel> trainNegativeImages = pascalVOC.getImageModels(
				EPascalType.CAR, 0, -1);
		List<ImageModel> testNegativeImages = pascalVOC.getImageModels(
				EPascalType.CAR, 1, -1);

		List<ImageModel> positiveTrainImageModels = new ArrayList<ImageModel>();
		List<ImageModel> negativeTrainImageModels = new ArrayList<ImageModel>();
		List<ImageModel> positiveTestImageModels = new ArrayList<ImageModel>();
		List<ImageModel> negativeTestImageModels = new ArrayList<ImageModel>();

		for (ImageModel imageModel : trainCarImages) {

			PascalAnnotation ann = pascalVOC.getAnnotation(imageModel
					.getRawImageName());
			List<PascalObject> objectList = ann.getObjectList(EPascalType.CAR);
			for (int i = 0; i < objectList.size(); i++) {
				PascalObject po = objectList.get(i);
				if (!po.isDifficult() && !po.isTruncated() && !po.isOccluded()) {
					Mat crop = OpenCV.getImageMat(imageModel.getImagePath(),
							po.getBndbox());

					ImageModel temp = new ImageModel();
					temp.setMat(crop);
					String rawImageName = imageModel.getRawImageName() + "_"
							+ i;
					temp.setRawImageName(rawImageName);
					temp.setImageName(rawImageName.concat(".jpg"));
					temp.setDataType(EDataType.TRAIN);
					temp.setNegativeImg(false);
					temp.setObjectClassType(EPascalType.CAR.getName());
					temp.setImagePath(TMP_DIR.concat(temp.getImageName()));

					positiveTrainImageModels.add(temp);
				}
			}

		}

		for (ImageModel imageModel : testCarImages) {

			PascalAnnotation ann = pascalVOC.getAnnotation(imageModel
					.getRawImageName());
			List<PascalObject> objectList = ann.getObjectList(EPascalType.CAR);
			for (int i = 0; i < objectList.size(); i++) {
				PascalObject po = objectList.get(i);
				if (!po.isDifficult() && !po.isTruncated() && !po.isOccluded()) {
					Mat crop = OpenCV.getImageMat(imageModel.getImagePath(),
							po.getBndbox());

					ImageModel temp = new ImageModel();
					temp.setMat(crop);
					String rawImageName = imageModel.getRawImageName() + "_"
							+ i;
					temp.setRawImageName(rawImageName);
					temp.setImageName(rawImageName.concat(".jpg"));
					temp.setDataType(EDataType.TEST);
					temp.setNegativeImg(false);
					temp.setObjectClassType(EPascalType.CAR.getName());
					temp.setImagePath(TMP_DIR.concat(temp.getImageName()));

					positiveTestImageModels.add(temp);
				}
			}

		}

		for (ImageModel imageModel : trainNegativeImages) {

			PascalAnnotation ann = pascalVOC.getAnnotation(imageModel
					.getRawImageName());
			List<PascalObject> objectList = ann.getObjectList(
					EPascalType.PERSON, EPascalType.AEROPLANE,
					EPascalType.BOAT, EPascalType.COW, EPascalType.HORSE,
					EPascalType.POTTED_PLANT, EPascalType.TV_MONITOR);
			for (int i = 0; i < objectList.size(); i++) {
				PascalObject po = objectList.get(i);

				Mat crop = OpenCV.getImageMat(imageModel.getImagePath(),
						po.getBndbox());

				ImageModel temp = new ImageModel();
				temp.setMat(crop);
				String rawImageName = imageModel.getRawImageName() + "_" + i;
				temp.setRawImageName(rawImageName);
				temp.setImageName(rawImageName.concat(".jpg"));
				temp.setDataType(EDataType.TRAIN);
				temp.setNegativeImg(true);
				temp.setObjectClassType(po.getName());
				temp.setImagePath(TMP_DIR.concat(temp.getImageName()));

				negativeTrainImageModels.add(temp);
			}

		}

		for (ImageModel imageModel : testNegativeImages) {

			PascalAnnotation ann = pascalVOC.getAnnotation(imageModel
					.getRawImageName());
			List<PascalObject> objectList = ann.getObjectList(
					EPascalType.PERSON, EPascalType.AEROPLANE,
					EPascalType.BOAT, EPascalType.COW, EPascalType.HORSE,
					EPascalType.POTTED_PLANT, EPascalType.TV_MONITOR);
			for (int i = 0; i < objectList.size(); i++) {
				PascalObject po = objectList.get(i);

				Mat crop = OpenCV.getImageMat(imageModel.getImagePath(),
						po.getBndbox());

				ImageModel temp = new ImageModel();
				temp.setMat(crop);
				String rawImageName = imageModel.getRawImageName() + "_" + i;
				temp.setRawImageName(rawImageName);
				temp.setImageName(rawImageName.concat(".jpg"));
				temp.setDataType(EDataType.TEST);
				temp.setNegativeImg(true);
				temp.setObjectClassType(po.getName());
				temp.setImagePath(TMP_DIR.concat(temp.getImageName()));

				negativeTestImageModels.add(temp);
			}

		}

		negativeTrainImageModels = filterImages(negativeTrainImageModels);
		negativeTestImageModels = filterImages(negativeTestImageModels);

		for (ImageModel imageModel : positiveTrainImageModels) {
			OpenCV.writeImage(imageModel.getMat(), imageModel.getImagePath());
		}

		for (ImageModel imageModel : negativeTrainImageModels) {
			OpenCV.writeImage(imageModel.getMat(), imageModel.getImagePath());
		}

		for (ImageModel imageModel : positiveTestImageModels) {
			OpenCV.writeImage(imageModel.getMat(), imageModel.getImagePath());
		}

		for (ImageModel imageModel : negativeTestImageModels) {
			OpenCV.writeImage(imageModel.getMat(), imageModel.getImagePath());
		}

		BilMpeg7Fex mpegFex = BilMpeg7Fex.getInstance();

		mpegFex.extractColorStructureDescriptors(positiveTrainImageModels, 256);
		mpegFex.extractScalableColorDescriptors(positiveTrainImageModels, 256);
		mpegFex.extractColorLayoutDescriptors(positiveTrainImageModels, 64, 28);
		mpegFex.extractDominantColorDescriptors(positiveTrainImageModels, 1, 0,
				1, 32, 32, 32);
		mpegFex.extractEdgeHistogramDescriptors(positiveTrainImageModels);

		mpegFex.extractColorStructureDescriptors(negativeTrainImageModels, 256);
		mpegFex.extractScalableColorDescriptors(negativeTrainImageModels, 256);
		mpegFex.extractColorLayoutDescriptors(negativeTrainImageModels, 64, 28);
		mpegFex.extractDominantColorDescriptors(negativeTrainImageModels, 1, 0,
				1, 32, 32, 32);
		mpegFex.extractEdgeHistogramDescriptors(negativeTrainImageModels);

		mpegFex.extractColorStructureDescriptors(positiveTestImageModels, 256);
		mpegFex.extractScalableColorDescriptors(positiveTestImageModels, 256);
		mpegFex.extractColorLayoutDescriptors(positiveTestImageModels, 64, 28);
		mpegFex.extractDominantColorDescriptors(positiveTestImageModels, 1, 0,
				1, 32, 32, 32);
		mpegFex.extractEdgeHistogramDescriptors(positiveTestImageModels);

		mpegFex.extractColorStructureDescriptors(negativeTestImageModels, 256);
		mpegFex.extractScalableColorDescriptors(negativeTestImageModels, 256);
		mpegFex.extractColorLayoutDescriptors(negativeTestImageModels, 64, 28);
		mpegFex.extractDominantColorDescriptors(negativeTestImageModels, 1, 0,
				1, 32, 32, 32);
		mpegFex.extractEdgeHistogramDescriptors(negativeTestImageModels);

		ImageModelService service = ImageModelService.getInstance();

		service.saveImageModelList(positiveTrainImageModels);
		service.saveImageModelList(negativeTrainImageModels);
		service.saveImageModelList(positiveTestImageModels);
		service.saveImageModelList(negativeTestImageModels);

	}

	private static List<ImageModel> filterImages(List<ImageModel> list) {

		List<ImageModel> result = new ArrayList<ImageModel>();
		for (ImageModel model : list) {

			int count = getCount(model.getObjectClassType(), result);
			if (count < 50) {
				result.add(model);
			} else {
				continue;
			}
		}
		return result;

	}

	private static int getCount(String classType, List<ImageModel> models) {
		int count = 0;
		for (ImageModel model : models) {
			if (classType.equals(model.getObjectClassType())) {
				count++;
			}
		}
		return count;
	}

	private static void segmentNegativeImages(int count) {

		PascalVOC pascal = PascalVOC.getInstance();

		List<ImageModel> imageModelList = pascal.getImageModels(
				EPascalType.CAR, 2, -1);

		Validate.isTrue(!(count > imageModelList.size()),
				"Count parameter must not be bigger than list size");

		String directoryName = CBODUtil.getDefaultOutputDirectoryPath().concat(
				"/train_data/negative");

		String tempDirectory = FileUtils.getTempDirectoryPath();

		Collections.shuffle(imageModelList);

		for (int i = 0; i < count; i++) {

			ImageModel imgModel = imageModelList.get(i);
			String imageRawName = imgModel.getRawImageName();

			JSEGParameter jsegParam = new JSEGParameter(imgModel.getImagePath());
			jsegParam.setRegionMapFileName(tempDirectory.concat("/").concat(
					imageRawName + CBODConstants.MAP_SUFFIX));
			jsegParam.setOutputFileImage(tempDirectory
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
