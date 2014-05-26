package net.bsrc.cbod.experiment;

import java.util.List;

import net.bsrc.cbod.core.CBODConstants;
import net.bsrc.cbod.core.model.EDescriptorType;
import net.bsrc.cbod.core.model.EObjectType;
import net.bsrc.cbod.core.model.ImageModel;
import net.bsrc.cbod.core.persistence.ImageModelService;
import net.bsrc.cbod.svm.libsvm.LibSvm;
import net.bsrc.cbod.svm.libsvm.ScaleParameter;

/**
 * The class contains experiments methods
 */
public class CbodExperiment {

	/**
	 * 
	 * @param trainPositiveObjType
	 *            positive image type
	 * @param trainNegativeObjType
	 *            negative image type
	 * @param testObjType
	 *            test image type
	 * @param countTrain
	 *            number of training images
	 * @param countTest
	 *            number of test images
	 * @param descriptorType
	 *            descriptor type
	 */
	public static void doExperiment(EObjectType trainPositiveObjType,
			EObjectType trainNegativeObjType, EObjectType testObjType,
			int countTrain, int countTest, EDescriptorType descriptorType) {

		ImageModelService service = ImageModelService.getInstance();

		// training images
		List<ImageModel> trainPositiveImageList = service.getImageModelList(
				trainPositiveObjType, false, countTrain);
		List<ImageModel> trainNegativeImageList = service.getImageModelList(
				trainNegativeObjType, false, countTrain);

		// test images
		List<ImageModel> testImageList = service.getImageModelList(testObjType,
				true, countTest);

		LibSvm libSvm = LibSvm.getInstance();

		String descName = descriptorType.getName();

		String trainingFileName = descName + "." + CBODConstants.SVM_TRAIN
				+ CBODConstants.TXT_SUFFIX;
		String testFileName = descName + "." + CBODConstants.SVM_TEST
				+ CBODConstants.TXT_SUFFIX;
		String rangeFileName = descName + "." + CBODConstants.SVM_RANGE
				+ CBODConstants.TXT_SUFFIX;

		int positiveLabel = 0;
		int negativeLabel = 1;

		libSvm.createFormattedDataFile(trainingFileName, positiveLabel,
				trainPositiveImageList, negativeLabel, trainNegativeImageList,
				descriptorType);

		int testLabel = -1;
		if (testObjType.equals(trainPositiveObjType))
			testLabel = positiveLabel;
		if (testObjType.equals(trainNegativeObjType))
			testLabel = negativeLabel;

		if (testLabel == -1)
			throw new IllegalArgumentException(
					"testObjectType must be equal trainPositiveObjType or trainNegativeObjType");

		libSvm.createFormattedDataFile(testFileName, testLabel, testImageList,
				descriptorType);

		ScaleParameter scaleParameter = new ScaleParameter();
		scaleParameter.setSaveFileName(rangeFileName);
		// scaleParameter.setLower(-1);

		String scaleTrainingFileName = libSvm.doScale(trainingFileName,
				scaleParameter);

		scaleParameter.setSaveFileName(null);
		scaleParameter.setRestoreFileName(rangeFileName);

		String scaleTestFileName = libSvm.doScale(testFileName, scaleParameter);

		String modelFileName = libSvm.doTrain(scaleTrainingFileName, null);

		libSvm.doPredict(scaleTestFileName, modelFileName, null);

	}

}
