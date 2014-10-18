package net.bsrc.cbod.experiment;

import java.util.List;

import net.bsrc.cbod.core.IDimensionReduction;
import net.bsrc.cbod.core.INormalization;
import net.bsrc.cbod.core.model.EDescriptorType;
import net.bsrc.cbod.core.model.EObjectType;
import net.bsrc.cbod.core.model.ImageModel;
import net.bsrc.cbod.core.persistence.ImageModelService;
import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.svm.libsvm.LibSvm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class contains experiments methods
 */
public class CbodExperiment {


    private static final Logger logger = LoggerFactory.getLogger(CbodExperiment.class);

    @SuppressWarnings("unchecked")
    public static void doExperiment(INormalization normalization,
                                    EObjectType positiveObjType, EObjectType negativeObjType,
                                    EObjectType testObjType, EDescriptorType... descriptorTypes) {

        ImageModelService service = ImageModelService.getInstance();

        List<List<Double>>[] trainPositiveDataArr = new List[descriptorTypes.length];
        List<List<Double>>[] trainNegativeDataArr = new List[descriptorTypes.length];
        List<List<Double>>[] testDataArr = new List[descriptorTypes.length];

        List<ImageModel> trainPositiveImages = service.getImageModelList(
                positiveObjType, false, 400);
        List<ImageModel> trainNegativeImages = service.getImageModelList(
                negativeObjType, false, 400);
        List<ImageModel> testImages = service.getImageModelList(testObjType,
                true, 100);

        for (int i = 0; i < descriptorTypes.length; i++) {

            EDescriptorType descriptorType = descriptorTypes[i];

            List<List<Double>> trainPositive = ImageModel
                    .getDescriptorDataLists(trainPositiveImages, descriptorType);
            List<List<Double>> trainNegative = ImageModel
                    .getDescriptorDataLists(trainNegativeImages, descriptorType);
            // test data
            List<List<Double>> testList = ImageModel.getDescriptorDataLists(
                    testImages, descriptorType);

            trainPositiveDataArr[i] = trainPositive;
            trainNegativeDataArr[i] = trainNegative;
            testDataArr[i] = testList;
        }

        List<List<Double>> positiveConcatList = CBODUtil
                .concatDataLists(trainPositiveDataArr);
        List<List<Double>> negativeConcatList = CBODUtil
                .concatDataLists(trainNegativeDataArr);
        List<List<Double>> testConcatList = CBODUtil
                .concatDataLists(testDataArr);

        normalization.applyNormalizations(positiveConcatList);
        normalization.applyNormalizations(negativeConcatList);
        normalization.applyNormalizations(testConcatList);



        LibSvm svm = LibSvm.getInstance();

        svm.doClassification("test_1", positiveConcatList, negativeConcatList,
                testConcatList, (positiveObjType == testObjType), true);

    }

}
