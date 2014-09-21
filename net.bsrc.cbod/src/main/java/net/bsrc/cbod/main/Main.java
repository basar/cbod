package net.bsrc.cbod.main;

import java.util.List;

import net.bsrc.cbod.core.ImageModelFactory;
import net.bsrc.cbod.core.model.EDescriptorType;
import net.bsrc.cbod.core.model.EObjectType;
import net.bsrc.cbod.core.model.ImageModel;
import net.bsrc.cbod.core.persistence.DB4O;
import net.bsrc.cbod.core.persistence.ImageModelService;
import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.experiment.CbodExperiment;
import net.bsrc.cbod.jseg.JSEGParameter;
import net.bsrc.cbod.opencv.OpenCV;
import net.bsrc.cbod.pascal.EPascalType;
import net.bsrc.cbod.pascal.PascalVOC;
import net.bsrc.cbod.pascal.xml.PascalAnnotation;
import net.bsrc.cbod.pascal.xml.PascalObject;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private final static Logger logger = LoggerFactory.getLogger(Main.class);

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	private final static String IMG_DIR = CBODUtil.getCbodInputImageDirectory()
			+ "/";
	private final static String TMP_DIR = CBODUtil.getCbodTempDirectory() + "/";

	/**
	 * Main entry point for program
	 * 
	 * @param args
	 */
	public static void main(String[] args) {


		//DBInitializeUtil.saveImageModelstoDB();

		// createModelFiles();
		// testObjectDetection("test_32.jpg");

		CbodExperiment.doExperiment(EObjectType.CAR,
		EObjectType.NONE_CAR, EObjectType.CAR, 400, 100,
		EDescriptorType.DCD);

		// JSEGParameter param =
		// JSEGParameterFactory.createJSEGParameter(IMG_DIR
		// + "/test_4.jpg", CBODUtil.getCbodTempDirectory());
		// param.setFactor(0.5);
		// param.setColorQuantizationThreshold(1);
		// param.setRegionMergeThreshold(0.1);
		// param.setNumberOfScales(2);
		// JSEG.doSegmentation(param);

		DB4O.getInstance().close();
	}

	private static void testObjectDetection(String imageName) {

		// Test yapilacak image
		ImageModel imageModel = ImageModelFactory.createImageModel(IMG_DIR
				+ imageName, true);

		JSEGParameter jsegParam = new JSEGParameter(imageModel.getImagePath());
		jsegParam.setFactor(0.5);
		jsegParam.setColorQuantizationThreshold(1);
		jsegParam.setRegionMergeThreshold(0.1);
		jsegParam.setNumberOfScales(2);

		// Image segmentlere ayriliyor
		List<ImageModel> imageSegments = CBODDemo.segmentImage(
				imageModel.getImagePath(), null);

		findCandidateWheels(imageModel, imageSegments);
		// findCandidateHeadlights(imageModel, imageSegments);
		// findCandidateTaillights(imageModel, imageSegments);

	}

	/**
	 * 
	 * @param imageModel
	 * @param imageSegments
	 */
	private static void findCandidateWheels(ImageModel imageModel,
			List<ImageModel> imageSegments) {

		Scalar green = new Scalar(0, 255, 0);

		String wheelModelFile = "HOG_WHEEL.train.scale.model.txt";
		String wheelRangeFile = "HOG_WHEEL.range.txt";
		EDescriptorType hog = EDescriptorType.HOG;

		doPredict(imageModel, imageSegments, wheelModelFile, wheelRangeFile,
				hog, green);

	}

	/**
	 * 
	 * @param imageModel
	 * @param imageSegments
	 */
	private static void findCandidateHeadlights(ImageModel imageModel,
			List<ImageModel> imageSegments) {

		Scalar blue = new Scalar(255, 195, 0);

		String headlightModelFile = "HOG_HEADLIGHT.train.scale.model.txt";
		String headlightRangeFile = "HOG_HEADLIGHT.range.txt";
		EDescriptorType hog = EDescriptorType.HOG;

		doPredict(imageModel, imageSegments, headlightModelFile,
				headlightRangeFile, hog, blue);
	}

	/**
	 * 
	 * @param imageModel
	 * @param imageSegments
	 */
	private static void findCandidateTaillights(ImageModel imageModel,
			List<ImageModel> imageSegments) {

		Scalar yellow = new Scalar(0, 255, 255);

		String taillightModelFile = "SCD_TAILLIGHT.train.scale.model.txt";
		String taillightRangeFile = "SCD_TAILLIGHT.range.txt";
		EDescriptorType scd = EDescriptorType.SCD;

		doPredict(imageModel, imageSegments, taillightModelFile,
				taillightRangeFile, scd, yellow);
	}

	private static void createModelFiles() {

		EDescriptorType hog = EDescriptorType.HOG;
		EDescriptorType scd = EDescriptorType.SCD;

		String wheelFilePreffix = "HOG_WHEEL";
		String headlightFilePreffix = "HOG_HEADLIGHT";
		String taillightFilePreffix = "SCD_TAILLIGHT";

		ImageModelService service = ImageModelService.getInstance();

		List<ImageModel> imageModelsWheel = service
				.getImageModelList(EObjectType.WHEEL);

		List<ImageModel> imageModelsHeadlight = service
				.getImageModelList(EObjectType.HEAD_LIGHT);

		List<ImageModel> imageModelsTaillight = service
				.getImageModelList(EObjectType.TAIL_LIGHT);

		List<ImageModel> negativeImageModels = service.getImageModelList(
				EObjectType.NONE_CAR_PART, 500);

		String[] arr = CBODDemo.createScaledTrainFileAndRangeFile(
				wheelFilePreffix, hog, imageModelsWheel, negativeImageModels);

		CBODDemo.createModelFile(arr[0]);

		arr = CBODDemo.createScaledTrainFileAndRangeFile(headlightFilePreffix,
				hog, imageModelsHeadlight, negativeImageModels);

		CBODDemo.createModelFile(arr[0]);

		arr = CBODDemo.createScaledTrainFileAndRangeFile(taillightFilePreffix,
				scd, imageModelsTaillight, negativeImageModels);

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

	private static void createNoneCarImageModel() {

		final int trainImgCount = 40;
		final int testImgCount = 10;
		final String trainFolder = "/Users/bsr/Documents/Phd/cbod/image_db/none_car/train/";
		final String testFolder = "/Users/bsr/Documents/Phd/cbod/image_db/none_car/test/";

		final int trainImgCountCar = 400;
		final int testImgCountCar = 100;

		final String trainFolderCar = "/Users/bsr/Documents/Phd/cbod/image_db/car/train/";
		final String testFolderCar = "/Users/bsr/Documents/Phd/cbod/image_db/car/test/";

		// none cars
		saveImageToDisk(EPascalType.BIRD, false, trainImgCount, trainFolder);
		saveImageToDisk(EPascalType.BIRD, true, testImgCount, testFolder);

		saveImageToDisk(EPascalType.PERSON, false, trainImgCount, trainFolder);
		saveImageToDisk(EPascalType.PERSON, true, testImgCount, testFolder);

		saveImageToDisk(EPascalType.COW, false, trainImgCount, trainFolder);
		saveImageToDisk(EPascalType.COW, true, testImgCount, testFolder);

		saveImageToDisk(EPascalType.BOAT, false, trainImgCount, trainFolder);
		saveImageToDisk(EPascalType.BOAT, true, testImgCount, testFolder);

		saveImageToDisk(EPascalType.AEROPLANE, false, trainImgCount,
				trainFolder);
		saveImageToDisk(EPascalType.AEROPLANE, true, testImgCount, testFolder);

		saveImageToDisk(EPascalType.HORSE, false, trainImgCount, trainFolder);
		saveImageToDisk(EPascalType.HORSE, true, testImgCount, testFolder);

		saveImageToDisk(EPascalType.TV_MONITOR, false, trainImgCount,
				trainFolder);
		saveImageToDisk(EPascalType.TV_MONITOR, true, testImgCount, testFolder);

		saveImageToDisk(EPascalType.POTTED_PLANT, false, trainImgCount,
				trainFolder);
		saveImageToDisk(EPascalType.POTTED_PLANT, true, testImgCount,
				testFolder);

		saveImageToDisk(EPascalType.CHAIR, false, trainImgCount, trainFolder);
		saveImageToDisk(EPascalType.CHAIR, true, testImgCount, testFolder);

		saveImageToDisk(EPascalType.DINING_TABLE, false, trainImgCount,
				trainFolder);
		saveImageToDisk(EPascalType.DINING_TABLE, true, testImgCount,
				testFolder);

		// cars
		saveImageToDisk(EPascalType.CAR, false, trainImgCountCar,
				trainFolderCar);
		saveImageToDisk(EPascalType.CAR, true, testImgCountCar, testFolderCar);

	}

	private static void saveImageToDisk(EPascalType pascalType, boolean isTest,
			int count, String folder) {

		PascalVOC pascalVOC = PascalVOC.getInstance();

		// 0, train, 1 test
		int suffix = 0;
		if (isTest)
			suffix = 1;

		List<ImageModel> imageModels = pascalVOC.getImageModels(pascalType,
				suffix, 1);

		int localCount = 0;
		for (ImageModel imgModel : imageModels) {

			if (localCount == count)
				break;

			PascalAnnotation pascalAnnotation = pascalVOC
					.getAnnotation(imgModel.getRawImageName());
			List<PascalObject> pascalObjects = pascalAnnotation
					.getObjectList(pascalType);
			for (PascalObject pascalObject : pascalObjects) {

				if (!pascalObject.isTruncated()) {

					Mat imgMat = OpenCV.getImageMat(imgModel.getImagePath(),
							pascalObject.getBndbox());
					OpenCV.writeImage(
							imgMat,
							folder.concat(imgModel.getRawImageName())
									.concat("_")
									.concat(pascalType.getName())
									.concat("_")
									.concat(Integer.toString(localCount++)
											.concat(".jpg")));

				}

				if (localCount == count)
					break;
			}
		}

	}

}
