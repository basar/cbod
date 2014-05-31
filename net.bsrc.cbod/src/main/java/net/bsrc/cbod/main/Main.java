package net.bsrc.cbod.main;

import java.util.List;

import net.bsrc.cbod.core.ImageModelFactory;
import net.bsrc.cbod.core.model.EDescriptorType;
import net.bsrc.cbod.core.model.EObjectType;
import net.bsrc.cbod.core.model.ImageModel;
import net.bsrc.cbod.core.persistence.DB4O;
import net.bsrc.cbod.core.persistence.ImageModelService;
import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.jseg.JSEGParameter;
import net.bsrc.cbod.opencv.OpenCV;

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
		
		
		//createModelFiles();
		testObjectDetection("test_21.jpg");
	
		// CbodExperiment.doExperiment(EObjectType.TAIL_LIGHT,
		// EObjectType.NONE_CAR_PART, EObjectType.NONE_CAR_PART, 400, 100,
		// EDescriptorType.DCD);

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
		List<ImageModel> imageSegments = CBODDemo.segmentImage(imageModel
				.getImagePath(),jsegParam);

		//findCandidateWheels(imageModel, imageSegments);
		findCandidateHeadlights(imageModel, imageSegments);
		//findCandidateTaillights(imageModel, imageSegments);

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

		Scalar yellow = new Scalar(0,255,255);
	

		String headlightModelFile = "HOG_HEADLIGHT.train.scale.model.txt";
		String headlightRangeFile = "HOG_HEADLIGHT.range.txt";
		EDescriptorType hog = EDescriptorType.HOG;

		doPredict(imageModel, imageSegments, headlightModelFile, headlightRangeFile,
				hog, yellow);
	}
	
	/**
	 * 
	 * @param imageModel
	 * @param imageSegments
	 */
	private static void findCandidateTaillights(ImageModel imageModel,
			List<ImageModel> imageSegments) {

		Scalar blue = new Scalar(255, 195, 0);

		String taillightModelFile = "SCD_TAILLIGHT.train.scale.model.txt";
		String taillightRangeFile = "SCD_TAILLIGHT.range.txt";
		EDescriptorType scd = EDescriptorType.SCD;

		doPredict(imageModel, imageSegments, taillightModelFile, taillightRangeFile,
				scd, blue);
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

	

}
