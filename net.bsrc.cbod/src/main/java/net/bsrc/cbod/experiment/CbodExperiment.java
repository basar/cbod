package net.bsrc.cbod.experiment;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import net.bsrc.cbod.core.CBODConstants;
import net.bsrc.cbod.core.INormalization;
import net.bsrc.cbod.core.model.EDescriptorType;
import net.bsrc.cbod.core.model.EObjectType;
import net.bsrc.cbod.core.model.ImageModel;
import net.bsrc.cbod.core.persistence.ImageModelService;
import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.svm.libsvm.LibSvm;
import net.bsrc.cbod.svm.libsvm.ScaleParameter;
import org.apache.commons.io.FileUtils;

/**
 * The class contains experiments methods
 */
public class CbodExperiment {


	@SuppressWarnings("unchecked")
    public static void doExperiment(INormalization normalization,
			EObjectType positiveObjType, EObjectType negativeObjType,
			EObjectType testObjType, EDescriptorType... descriptorTypes) {

		ImageModelService service = ImageModelService.getInstance();

		List<List<Double>>[] trainPositiveDataArr = new List[descriptorTypes.length];
		List<List<Double>>[] trainNegativeDataArr = new List[descriptorTypes.length];
		List<List<Double>>[] testDataArr = new List[descriptorTypes.length];

		for (int i = 0; i < descriptorTypes.length; i++) {

			EDescriptorType descriptorType = descriptorTypes[i];

			List<List<Double>> trainPositive = ImageModel
					.getDescriptorDataLists(service.getImageModelList(
                            positiveObjType, false, 400), descriptorType);
			List<List<Double>> trainNegative = ImageModel
					.getDescriptorDataLists(service.getImageModelList(
                            negativeObjType, false, 400), descriptorType);
			// test data
			List<List<Double>> testList = ImageModel.getDescriptorDataLists(
					service.getImageModelList(testObjType, true, 100),
					descriptorType);

			trainPositiveDataArr[i] = trainPositive;
			trainNegativeDataArr[i] = trainNegative;
			testDataArr[i] = testList;
		}

        List<List<Double>> positiveConcatList=CBODUtil.concatDataLists(trainPositiveDataArr);
        List<List<Double>> negativeConcatList=CBODUtil.concatDataLists(trainNegativeDataArr);
        List<List<Double>> testConcatList=CBODUtil.concatDataLists(testDataArr);


        normalization.applyNormalizations(positiveConcatList);
        normalization.applyNormalizations(negativeConcatList);
        normalization.applyNormalizations(testConcatList);

        LibSvm svm=LibSvm.getInstance();

        svm.doClassification("test_1",positiveConcatList,negativeConcatList,testConcatList,(positiveObjType == testObjType),true);



    }

}
