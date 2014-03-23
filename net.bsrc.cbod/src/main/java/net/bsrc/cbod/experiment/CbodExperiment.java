/*package net.bsrc.cbod.experiment;

import net.bsrc.cbod.core.CBODConstants;
import net.bsrc.cbod.core.model.EDataType;
import net.bsrc.cbod.core.model.EDescriptorType;
import net.bsrc.cbod.core.model.ImageModel;
import net.bsrc.cbod.core.persistence.ImageModelService;
import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.opencv.OpenCV;
import net.bsrc.cbod.pascal.EPascalType;
import net.bsrc.cbod.svm.libsvm.LibSvm;
import net.bsrc.cbod.svm.libsvm.ScaleParameter;

import java.util.List;

*//**
 * The class contains experiments methods
 *//*
public class CbodExperiment {

	public static void testWithWholePositiveImages(EDescriptorType descriptorType) {

		ImageModelService service = ImageModelService.getInstance();
		*//**
		 * Egitim verileri
		 *//*
		List<ImageModel> trainPositiveImageModelList = service
				.getImageModelListByClassType(EDataType.TRAIN, EPascalType.CAR.getName());
        List<ImageModel> trainNegativeImageModelList = service
                .getNegativeImageModelList(EDataType.TRAIN);

        for(ImageModel imgModel:trainPositiveImageModelList){
            OpenCV.writeImage(imgModel.getMat(),CBODUtil.getCbodTempDirectory()+"/positive/"+imgModel.getImageName());
        }

        System.out.println(trainNegativeImageModelList.size());
        System.out.println(trainPositiveImageModelList.size());

        *//**
         * Test verileri
         *//*
        List<ImageModel> testPositiveImageModelList = service.getImageModelListByClassType(EDataType.TEST,EPascalType.CAR.getName());


        LibSvm libSvm = LibSvm.getInstance();

        String descName = descriptorType.getName();

        String trainingFileName = descName + "." + CBODConstants.SVM_TRAIN
                + "." + CBODConstants.TXT_SUFFIX;
        String testFileName = descName + "." + CBODConstants.SVM_TEST + "."
                + CBODConstants.TXT_SUFFIX;
        String rangeFileName = descName + "." + CBODConstants.SVM_RANGE + "."
                + CBODConstants.TXT_SUFFIX;

        libSvm.createFormattedDataFile(trainingFileName, 0,
                trainNegativeImageModelList, 1, trainPositiveImageModelList,
                descriptorType);

        libSvm.createFormattedDataFile(testFileName, 1,
                testPositiveImageModelList, descriptorType);

        ScaleParameter scaleParameter = new ScaleParameter();
        scaleParameter.setSaveFileName(rangeFileName);

        String scaleTrainingFileName = libSvm.doScale(trainingFileName,
                scaleParameter);

        scaleParameter.setSaveFileName(null);
        scaleParameter.setRestoreFileName(rangeFileName);

        String scaleTestFileName = libSvm.doScale(testFileName, scaleParameter);

        String modelFileName = libSvm.doTrain(scaleTrainingFileName, null);

        libSvm.doPredict(scaleTestFileName, modelFileName, null);

	}


    public static void testWithWholeNegativeImages(EDescriptorType descriptorType){

        ImageModelService service = ImageModelService.getInstance();
        *//**
         * Egitim verileri
         *//*
        List<ImageModel> trainPositiveImageModelList = service
                .getImageModelListByClassType(EDataType.TRAIN, EPascalType.CAR.getName());
        List<ImageModel> trainNegativeImageModelList = service
                .getNegativeImageModelList(EDataType.TRAIN);

        *//**
         * Test verileri
         *//*
        List<ImageModel> testNegativeImageModelList = service.getNegativeImageModelList(EDataType.TEST);


        LibSvm libSvm = LibSvm.getInstance();

        String descName = descriptorType.getName();

        String trainingFileName = descName + "." + CBODConstants.SVM_TRAIN
                + "." + CBODConstants.TXT_SUFFIX;
        String testFileName = descName + "." + CBODConstants.SVM_TEST + "."
                + CBODConstants.TXT_SUFFIX;
        String rangeFileName = descName + "." + CBODConstants.SVM_RANGE + "."
                + CBODConstants.TXT_SUFFIX;

        libSvm.createFormattedDataFile(trainingFileName, 0,
                trainNegativeImageModelList, 1, trainPositiveImageModelList,
                descriptorType);

        libSvm.createFormattedDataFile(testFileName, 0,
                testNegativeImageModelList, descriptorType);

        ScaleParameter scaleParameter = new ScaleParameter();
        scaleParameter.setSaveFileName(rangeFileName);

        String scaleTrainingFileName = libSvm.doScale(trainingFileName,
                scaleParameter);

        scaleParameter.setSaveFileName(null);
        scaleParameter.setRestoreFileName(rangeFileName);

        String scaleTestFileName = libSvm.doScale(testFileName, scaleParameter);

        String modelFileName = libSvm.doTrain(scaleTrainingFileName, null);

        libSvm.doPredict(scaleTestFileName, modelFileName, null);


    }

    public static void testWithWholeBothNegativeAndPositiveImages(EDescriptorType descriptorType){

        ImageModelService service = ImageModelService.getInstance();

        *//**
         * Egitim verileri
         *//*
        *//**
         * Egitim verileri
         *//*
        List<ImageModel> trainPositiveImageModelList = service
                .getImageModelListByClassType(EDataType.TRAIN, EPascalType.CAR.getName());
        List<ImageModel> trainNegativeImageModelList = service
                .getNegativeImageModelList(EDataType.TRAIN);

        *//**
         * Test verileri
         *//*
        List<ImageModel> testPositiveImageModelList = service.getImageModelListByClassType(EDataType.TEST,EPascalType.CAR.getName());
        List<ImageModel> testNegativeImageModelList = service.getNegativeImageModelList(EDataType.TEST);



        LibSvm libSvm = LibSvm.getInstance();

        String descName = descriptorType.getName();

        String trainingFileName = descName + "." + CBODConstants.SVM_TRAIN
                + "." + CBODConstants.TXT_SUFFIX;
        String testFileName = descName + "." + CBODConstants.SVM_TEST + "."
                + CBODConstants.TXT_SUFFIX;
        String rangeFileName = descName + "." + CBODConstants.SVM_RANGE + "."
                + CBODConstants.TXT_SUFFIX;

        libSvm.createFormattedDataFile(trainingFileName, 0,
                trainNegativeImageModelList, 1, trainPositiveImageModelList,
                descriptorType);

        libSvm.createFormattedDataFile(testFileName, 0,
                testNegativeImageModelList, 1, testPositiveImageModelList,
                descriptorType);

        ScaleParameter scaleParameter = new ScaleParameter();
        scaleParameter.setSaveFileName(rangeFileName);

        String scaleTrainingFileName = libSvm.doScale(trainingFileName,
                scaleParameter);

        scaleParameter.setSaveFileName(null);
        scaleParameter.setRestoreFileName(rangeFileName);

        String scaleTestFileName = libSvm.doScale(testFileName, scaleParameter);

        String modelFileName = libSvm.doTrain(scaleTrainingFileName, null);

        libSvm.doPredict(scaleTestFileName, modelFileName, null);


    }

	public static void testWithPositiveImages(EDescriptorType descriptorType,
			String objectPart) {

		ImageModelService service = ImageModelService.getInstance();

		*//**
		 * Egitim verileri
		 *//*
		List<ImageModel> positiveImageModelList = service.getImageModelList(
				EDataType.TRAIN, objectPart);
		List<ImageModel> negativeImageModelList = service
				.getNegativeImageModelList(EDataType.TRAIN, 0);

		List<ImageModel> testPositiveImageModelList = service
				.getImageModelList(EDataType.TEST, objectPart);

		LibSvm libSvm = LibSvm.getInstance();

		String descName = descriptorType.getName();

		String trainingFileName = descName + "." + CBODConstants.SVM_TRAIN
				+ "." + CBODConstants.TXT_SUFFIX;
		String testFileName = descName + "." + CBODConstants.SVM_TEST + "."
				+ CBODConstants.TXT_SUFFIX;
		String rangeFileName = descName + "." + CBODConstants.SVM_RANGE + "."
				+ CBODConstants.TXT_SUFFIX;

		libSvm.createFormattedDataFile(trainingFileName, 0,
				negativeImageModelList, 1, positiveImageModelList,
				descriptorType);

		libSvm.createFormattedDataFile(testFileName, 1,
				testPositiveImageModelList, descriptorType);

		ScaleParameter scaleParameter = new ScaleParameter();
		scaleParameter.setSaveFileName(rangeFileName);

		String scaleTrainingFileName = libSvm.doScale(trainingFileName,
				scaleParameter);

		scaleParameter.setSaveFileName(null);
		scaleParameter.setRestoreFileName(rangeFileName);

		String scaleTestFileName = libSvm.doScale(testFileName, scaleParameter);

		String modelFileName = libSvm.doTrain(scaleTrainingFileName, null);

		libSvm.doPredict(scaleTestFileName, modelFileName, null);

	}

	public static void testWithNegativeImages(EDescriptorType descriptorType,
			String objectPart) {

		ImageModelService service = ImageModelService.getInstance();

		*//**
		 * Egitim verileri
		 *//*
		List<ImageModel> positiveImageModelList = service.getImageModelList(
				EDataType.TRAIN, objectPart);
		List<ImageModel> negativeImageModelList = service
				.getNegativeImageModelList(EDataType.TRAIN, 0);

		List<ImageModel> testNegativeImageModelList = service
				.getNegativeImageModelList(EDataType.TEST, 0);

		LibSvm libSvm = LibSvm.getInstance();

		String descName = descriptorType.getName();

		String trainingFileName = descName + "." + CBODConstants.SVM_TRAIN
				+ "." + CBODConstants.TXT_SUFFIX;
		String testFileName = descName + "." + CBODConstants.SVM_TEST + "."
				+ CBODConstants.TXT_SUFFIX;
		String rangeFileName = descName + "." + CBODConstants.SVM_RANGE + "."
				+ CBODConstants.TXT_SUFFIX;

		libSvm.createFormattedDataFile(trainingFileName, 0,
				negativeImageModelList, 1, positiveImageModelList,
				descriptorType);

		libSvm.createFormattedDataFile(testFileName, 0,
				testNegativeImageModelList, descriptorType);

		ScaleParameter scaleParameter = new ScaleParameter();
		scaleParameter.setSaveFileName(rangeFileName);

		String scaleTrainingFileName = libSvm.doScale(trainingFileName,
				scaleParameter);

		scaleParameter.setSaveFileName(null);
		scaleParameter.setRestoreFileName(rangeFileName);

		String scaleTestFileName = libSvm.doScale(testFileName, scaleParameter);

		String modelFileName = libSvm.doTrain(scaleTrainingFileName, null);

		libSvm.doPredict(scaleTestFileName, modelFileName, null);

	}

	public static void testBothNegativeAndPositiveImages(
			EDescriptorType descriptorType, String objectPart) {

		ImageModelService service = ImageModelService.getInstance();

		*//**
		 * Egitim verileri
		 *//*
		List<ImageModel> positiveImageModelList = service.getImageModelList(
				EDataType.TRAIN, objectPart);
		List<ImageModel> negativeImageModelList = service
				.getNegativeImageModelList(EDataType.TRAIN, 0);

		*//**
		 * Test verileri
		 *//*
		List<ImageModel> testPositiveImageModelList = service
				.getImageModelList(EDataType.TEST, objectPart);
		List<ImageModel> testNegativeImageModelList = service
				.getNegativeImageModelList(EDataType.TEST, 0);

		CBODUtil.compareTwoImageModelCollection(positiveImageModelList,
				testPositiveImageModelList);
		CBODUtil.compareTwoImageModelCollection(negativeImageModelList,
				testNegativeImageModelList);

		LibSvm libSvm = LibSvm.getInstance();

		String descName = descriptorType.getName();

		String trainingFileName = descName + "." + CBODConstants.SVM_TRAIN
				+ "." + CBODConstants.TXT_SUFFIX;
		String testFileName = descName + "." + CBODConstants.SVM_TEST + "."
				+ CBODConstants.TXT_SUFFIX;
		String rangeFileName = descName + "." + CBODConstants.SVM_RANGE + "."
				+ CBODConstants.TXT_SUFFIX;

		libSvm.createFormattedDataFile(trainingFileName, 0,
				negativeImageModelList, 1, positiveImageModelList,
				descriptorType);

		libSvm.createFormattedDataFile(testFileName, 0,
				testNegativeImageModelList, 1, testPositiveImageModelList,
				descriptorType);

		ScaleParameter scaleParameter = new ScaleParameter();
		scaleParameter.setSaveFileName(rangeFileName);

		String scaleTrainingFileName = libSvm.doScale(trainingFileName,
				scaleParameter);

		scaleParameter.setSaveFileName(null);
		scaleParameter.setRestoreFileName(rangeFileName);

		String scaleTestFileName = libSvm.doScale(testFileName, scaleParameter);

		String modelFileName = libSvm.doTrain(scaleTrainingFileName, null);

		libSvm.doPredict(scaleTestFileName, modelFileName, null);
	}

}
*/