package net.bsrc.cbod.core;

import com.googlecode.javacv.cpp.opencv_core;
import libsvm.*;
import net.bsrc.cbod.core.model.*;
import net.bsrc.cbod.core.persistence.ImageModelService;
import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.jseg.JSEG;
import net.bsrc.cbod.jseg.JSEGParameter;
import net.bsrc.cbod.jseg.JSEGParameterFactory;
import net.bsrc.cbod.mpeg.bil.BilMpeg7Fex;
import net.bsrc.cbod.opencv.OpenCV;
import net.bsrc.cbod.svm.libsvm.LibSvm;
import net.bsrc.cbod.svm.libsvm.LibSvmUtil;
import net.bsrc.cbod.svm.libsvm.SvmModelPair;
import org.apache.commons.configuration.CompositeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: bsr
 * Date: 07/03/15
 * Time: 13:54
 */
public class MultiClassSVM {


    private static final Logger logger = LoggerFactory
            .getLogger(MultiClassSVM.class);

    private static MultiClassSVM multiClassSVM = null;

    /**
     * To prevent object creation
     */
    private MultiClassSVM() {

    }

    public static MultiClassSVM getInstance() {
        if (multiClassSVM == null) {
            synchronized (MultiClassSVM.class) {
                if (multiClassSVM == null) {
                    multiClassSVM = createMultiClassSVM();
                }
            }
        }
        return multiClassSVM;
    }

    private static MultiClassSVM createMultiClassSVM() {
        return new MultiClassSVM();
    }


    public SVMPredictionResult doPredictionWithMultiClassSVMs(String imageName, Double threshold, INormalization normalization) {


        SVMPredictionResult result = new SVMPredictionResult();
        result.setSvmType(ESVMType.MULTI_CLASS_WITH_DECISION);

        // Test yapilacak image
        ImageModel imageModel = ImageModelFactory.createImageModel(CBODUtil.getCbodInputImageDirectory().concat("/").concat(imageName), true);
        result.setInputImageModel(imageModel);

        JSEGParameter jsegParam = JSEGParameterFactory.createJsegParameterWithDefultCbodValues(imageModel.getImagePath());

        //Image segmentlere ayriliyor
        List<ImageModel> imageSegments = JSEG.segmentImage(
                imageModel.getImagePath(), jsegParam);
        //Her bir image model icin featurelar cikartiliyor

        //SIFT and HOG Descriptor
        opencv_core.CvMat dictMat = OpenCV.loadCvMatFromFile(CBODUtil.getCbodTempDirectory().concat("/").concat("sift_dict.xml"), "sift_dict");
        for (ImageModel im : imageSegments) {
            Descriptor siftDesc = new Descriptor();
            siftDesc.setType(EDescriptorType.SIFT);
            siftDesc.setDataList(CBODSift.extractSIFTDescriptorAsList(im, dictMat));
            im.getDescriptors().add(siftDesc);

            Descriptor hogDesc = new Descriptor();
            hogDesc.setType(EDescriptorType.HOG);
            hogDesc.setDataList(CBODHog.extractHogDescriptor(im));
            im.getDescriptors().add(hogDesc);
        }

        BilMpeg7Fex mpeg7Fex = BilMpeg7Fex.getInstance();
        //MPEG descriptors
        mpeg7Fex.extractEdgeHistogramDescriptors(imageSegments);
        mpeg7Fex.extractScalableColorDescriptors(imageSegments, 256);
        mpeg7Fex.extractColorStructureDescriptors(imageSegments, 256);
        mpeg7Fex.extractColorLayoutDescriptors(imageSegments, 64, 28);
        mpeg7Fex.extractDominantColorDescriptors(imageSegments, 1, 0, 1, 32,
                32, 32);

        //Apply normalization
        for (ImageModel im : imageSegments) {
            for (Descriptor descriptor : im.getDescriptors()) {
                normalization.applyNormalization(descriptor.getDataList());
            }
            result.addSegmentedInputImageModel(im);
        }

        try {
            final double siftWeight = 0.7581,
                    hogWeight = 0.9068, ehdWeight = 0.8600, csdWeight = 0.7725,
                    cldWeight = 0.9031, scdWeight = 0.9075, dcdWeight = 0.8231;
            final double totalWeight = siftWeight + hogWeight + ehdWeight + csdWeight + cldWeight + scdWeight + dcdWeight;

            //SVM models
            svm_model siftModel = svm.svm_load_model(LibSvm.getSvmDirectoryPath().concat("/").concat("MC.SIFT.txt"));
            svm_model hogModel = svm.svm_load_model(LibSvm.getSvmDirectoryPath().concat("/").concat("MC.HOG.txt"));
            svm_model ehdModel = svm.svm_load_model(LibSvm.getSvmDirectoryPath().concat("/").concat("MC.EHD.txt"));
            svm_model csdModel = svm.svm_load_model(LibSvm.getSvmDirectoryPath().concat("/").concat("MC.CSD.txt"));
            svm_model cldModel = svm.svm_load_model(LibSvm.getSvmDirectoryPath().concat("/").concat("MC.CLD.txt"));
            svm_model scdModel = svm.svm_load_model(LibSvm.getSvmDirectoryPath().concat("/").concat("MC.SCD.txt"));
            svm_model dcdModel = svm.svm_load_model(LibSvm.getSvmDirectoryPath().concat("/").concat("MC.DCD.txt"));

            Long componentId = 0l;
            for (ImageModel im : imageSegments) {

                double siftProbNonCar = getPredictedPropability(siftModel, im, EDescriptorType.SIFT, 0);
                double hogProbNonCar = getPredictedPropability(hogModel, im, EDescriptorType.HOG, 0);
                double ehdProbNonCar = getPredictedPropability(ehdModel, im, EDescriptorType.EHD, 0);
                double csdProbNonCar = getPredictedPropability(csdModel, im, EDescriptorType.CSD, 0);
                double cldProbNonCar = getPredictedPropability(cldModel, im, EDescriptorType.CLD, 0);
                double scdProbNonCar = getPredictedPropability(scdModel, im, EDescriptorType.SCD, 0);
                double dcdProbNonCar = getPredictedPropability(dcdModel, im, EDescriptorType.DCD, 0);

                double nonCarPartDecisionResult = ((siftWeight * siftProbNonCar)
                        + (hogWeight * hogProbNonCar) + (ehdWeight * ehdProbNonCar)
                        + (csdWeight * csdProbNonCar) + (cldWeight * cldProbNonCar)
                        + (scdWeight * scdProbNonCar) + (dcdWeight * dcdProbNonCar)) / totalWeight;

                double siftProbTailLight = getPredictedPropability(siftModel, im, EDescriptorType.SIFT, 1);
                double hogProbTailLight = getPredictedPropability(hogModel, im, EDescriptorType.HOG, 1);
                double ehdProbTailLight = getPredictedPropability(ehdModel, im, EDescriptorType.EHD, 1);
                double csdProbTailLight = getPredictedPropability(csdModel, im, EDescriptorType.CSD, 1);
                double cldProbTailLight = getPredictedPropability(cldModel, im, EDescriptorType.CLD, 1);
                double scdProbTailLight = getPredictedPropability(scdModel, im, EDescriptorType.SCD, 1);
                double dcdProbTailLight = getPredictedPropability(dcdModel, im, EDescriptorType.DCD, 1);

                double tailLightDecisionResult = ((siftWeight * siftProbTailLight)
                        + (hogWeight * hogProbTailLight) + (ehdWeight * ehdProbTailLight)
                        + (csdWeight * csdProbTailLight) + (cldWeight * cldProbTailLight)
                        + (scdWeight * scdProbTailLight) + (dcdWeight * dcdProbTailLight)) / totalWeight;

                double siftProbWheel = getPredictedPropability(siftModel, im, EDescriptorType.SIFT, 2);
                double hogProbWheel = getPredictedPropability(hogModel, im, EDescriptorType.HOG, 2);
                double ehdProbWheel = getPredictedPropability(ehdModel, im, EDescriptorType.EHD, 2);
                double csdProbWheel = getPredictedPropability(csdModel, im, EDescriptorType.CSD, 2);
                double cldProbWheel = getPredictedPropability(cldModel, im, EDescriptorType.CLD, 2);
                double scdProbWheel = getPredictedPropability(scdModel, im, EDescriptorType.SCD, 2);
                double dcdProbWheel = getPredictedPropability(dcdModel, im, EDescriptorType.DCD, 2);

                double wheelDecisionResult = ((siftWeight * siftProbWheel)
                        + (hogWeight * hogProbWheel) + (ehdWeight * ehdProbWheel)
                        + (csdWeight * csdProbWheel) + (cldWeight * cldProbWheel)
                        + (scdWeight * scdProbWheel) + (dcdWeight * dcdProbWheel)) / totalWeight;

                double siftProbLicensePlate = getPredictedPropability(siftModel, im, EDescriptorType.SIFT, 3);
                double hogProbLicensePlate = getPredictedPropability(hogModel, im, EDescriptorType.HOG, 3);
                double ehdProbLicensePlate = getPredictedPropability(ehdModel, im, EDescriptorType.EHD, 3);
                double csdProbLicensePlate = getPredictedPropability(csdModel, im, EDescriptorType.CSD, 3);
                double cldProbLicensePlate = getPredictedPropability(cldModel, im, EDescriptorType.CLD, 3);
                double scdProbLicensePlate = getPredictedPropability(scdModel, im, EDescriptorType.SCD, 3);
                double dcdProbLicensePlate = getPredictedPropability(dcdModel, im, EDescriptorType.DCD, 3);


                double licensePlateDecisionResult = ((siftWeight * siftProbLicensePlate)
                        + (hogWeight * hogProbLicensePlate) + (ehdWeight * ehdProbLicensePlate)
                        + (csdWeight * csdProbLicensePlate) + (cldWeight * cldProbLicensePlate)
                        + (scdWeight * scdProbLicensePlate) + (dcdWeight * dcdProbLicensePlate)) / totalWeight;

                int maxIndex = CBODUtil.getIndexWithMaxValue(nonCarPartDecisionResult, tailLightDecisionResult, wheelDecisionResult, licensePlateDecisionResult);

                CandidateComponent candidateComponent = null;
                switch (maxIndex) {
                    case 0:
                        break;
                    case 1:
                        if (tailLightDecisionResult > threshold) {
                            candidateComponent = new CandidateComponent();
                            candidateComponent.setId(++componentId);
                            candidateComponent.setDecisionFusionResult(tailLightDecisionResult);
                            candidateComponent.setRect(im.getRelativeToOrg());
                            candidateComponent.setObjectType(EObjectType.TAIL_LIGHT);
                        }
                        break;
                    case 2:
                        if (wheelDecisionResult > threshold) {
                            candidateComponent = new CandidateComponent();
                            candidateComponent.setId(++componentId);
                            candidateComponent.setDecisionFusionResult(wheelDecisionResult);
                            candidateComponent.setRect(im.getRelativeToOrg());
                            candidateComponent.setObjectType(EObjectType.WHEEL);
                        }
                        break;
                    case 3:
                        if (licensePlateDecisionResult > threshold) {
                            candidateComponent = new CandidateComponent();
                            candidateComponent.setId(++componentId);
                            candidateComponent.setDecisionFusionResult(licensePlateDecisionResult);
                            candidateComponent.setRect(im.getRelativeToOrg());
                            candidateComponent.setObjectType(EObjectType.LICENSE_PLATE);
                        }
                        break;
                    default:
                }

                if (candidateComponent != null) {
                    result.addCandidateComponent(candidateComponent);
                }

            }

        } catch (IOException e) {
            logger.error("", e);
        }

        return result;

    }


    private double getPredictedPropability(svm_model svmModel, ImageModel im, EDescriptorType descriptorType, int index) {


        Descriptor descriptor = im.getDescriptor(descriptorType);
        if (descriptor == null)
            return 0.0;
        List<Double> dataList = descriptor.getDataList();

        svm_node[] nodes = LibSvmUtil.createSVMNodeArray(dataList);
        double[] probs = new double[4];
        svm.svm_predict_probability(svmModel, nodes, probs);

        return probs[index];
    }


    public void createMultiClassSVMModels(INormalization normalization) {

        final double sift_c = Math.pow(2.0, 15.0), sift_gamma = Math.pow(2.0, -6.0);
        final double hog_c = Math.pow(2.0, 15.0), hog_gamma = Math.pow(2.0, -12.0);
        final double ehd_c = Math.pow(2.0, 5.0), ehd_gamma = Math.pow(2.0, -6.0);
        final double csd_c = Math.pow(2.0, 15.0), csd_gamma = Math.pow(2.0, -9.0);
        final double cld_c = Math.pow(2.0, 5.0), cld_gamma = Math.pow(2.0, -6.0);
        final double scd_c = Math.pow(2.0, 5.0), scd_gamma = Math.pow(2.0, -9.0);
        final double dcd_c = Math.pow(2.0, 5.0), dcd_gamma = Math.pow(2.0, -3.0);

        createAndSaveMultiClassSvmModel("MC.SIFT.txt", EDescriptorType.SIFT, normalization, sift_c, sift_gamma);
        createAndSaveMultiClassSvmModel("MC.HOG.txt", EDescriptorType.HOG, normalization, hog_c, hog_gamma);
        createAndSaveMultiClassSvmModel("MC.EHD.txt", EDescriptorType.EHD, normalization, ehd_c, ehd_gamma);
        createAndSaveMultiClassSvmModel("MC.CSD.txt", EDescriptorType.CSD, normalization, csd_c, csd_gamma);
        createAndSaveMultiClassSvmModel("MC.CLD.txt", EDescriptorType.CLD, normalization, cld_c, cld_gamma);
        createAndSaveMultiClassSvmModel("MC.SCD.txt", EDescriptorType.SCD, normalization, scd_c, scd_gamma);
        createAndSaveMultiClassSvmModel("MC.DCD.txt", EDescriptorType.DCD, normalization, dcd_c, dcd_gamma);
    }


    private void createAndSaveMultiClassSvmModel(String modelName, EDescriptorType descriptorType, INormalization normalization, double c, double gamma) {

        ImageModelService service = ImageModelService.getInstance();

        List<List<Double>> tailLightDataLists = ImageModel.getDescriptorDataLists(service.getImageModelList(
                EObjectType.TAIL_LIGHT, false, 400), descriptorType);
        List<List<Double>> wheelDataLists = ImageModel.getDescriptorDataLists(service.getImageModelList(EObjectType.WHEEL, false, 400), descriptorType);
        List<List<Double>> licensePlateDataLists = ImageModel.getDescriptorDataLists(service.getImageModelList(EObjectType.LICENSE_PLATE, false, 400), descriptorType);
        List<List<Double>> nonCarPartDataLists = ImageModel.getDescriptorDataLists(service.getImageModelList(EObjectType.NONE_CAR_PART, false, 400), descriptorType);

        normalization.applyNormalizations(tailLightDataLists);
        normalization.applyNormalizations(nonCarPartDataLists);
        normalization.applyNormalizations(wheelDataLists);
        normalization.applyNormalizations(licensePlateDataLists);

        List<SvmModelPair> pairs = new ArrayList<SvmModelPair>();
        pairs.add(new SvmModelPair(EObjectType.NONE_CAR_PART.ordinal(), nonCarPartDataLists));
        pairs.add(new SvmModelPair(EObjectType.TAIL_LIGHT.ordinal(), tailLightDataLists));
        pairs.add(new SvmModelPair(EObjectType.WHEEL.ordinal(), wheelDataLists));
        pairs.add(new SvmModelPair(EObjectType.LICENSE_PLATE.ordinal(), licensePlateDataLists));

        svm_problem svmProblem = LibSvmUtil.createSvmProblem(pairs);
        svm_parameter param = LibSvmUtil.createSvmParameter(c, gamma, true);

        svm_model svmModel = svm.svm_train(svmProblem, param);

        try {
            svm.svm_save_model(LibSvm.getSvmDirectoryPath().concat("/").concat(modelName), svmModel);
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    public void doCrossValidationForMultiClassSVM(EDescriptorType descriptorType, INormalization normalization) {

        ImageModelService service = ImageModelService.getInstance();

        List<List<Double>> tailLightDataLists = ImageModel.getDescriptorDataLists(service.getImageModelList(
                EObjectType.TAIL_LIGHT, false, 500), descriptorType);
        List<List<Double>> wheelDataLists = ImageModel.getDescriptorDataLists(service.getImageModelList(EObjectType.WHEEL, 500), descriptorType);
        List<List<Double>> licensePlateDataLists = ImageModel.getDescriptorDataLists(service.getImageModelList(EObjectType.LICENSE_PLATE, 500), descriptorType);
        List<List<Double>> nonCarPartDataLists = ImageModel.getDescriptorDataLists(service.getImageModelList(EObjectType.NONE_CAR_PART, 500), descriptorType);

        normalization.applyNormalizations(tailLightDataLists);
        normalization.applyNormalizations(nonCarPartDataLists);
        normalization.applyNormalizations(wheelDataLists);
        normalization.applyNormalizations(licensePlateDataLists);

        List<SvmModelPair> pairs = new ArrayList<SvmModelPair>();
        pairs.add(new SvmModelPair(EObjectType.NONE_CAR_PART.ordinal(), nonCarPartDataLists));
        pairs.add(new SvmModelPair(EObjectType.TAIL_LIGHT.ordinal(), tailLightDataLists));
        pairs.add(new SvmModelPair(EObjectType.WHEEL.ordinal(), wheelDataLists));
        pairs.add(new SvmModelPair(EObjectType.LICENSE_PLATE.ordinal(), licensePlateDataLists));

        svm_problem svmProblem = LibSvmUtil.createSvmProblem(pairs);
        svm_parameter param = LibSvmUtil.createSvmParameter(0.0, 0.0, true);

        LibSvmUtil.doCrossValidation(svmProblem, param, 5, 3, 4);
    }



    public CandidateComponent findComponentWithMaximumDecisionFusionResult(List<CandidateComponent> candidateComponents){

        CandidateComponent result = null;
        for (CandidateComponent candidateComponent : candidateComponents) {
            if(result==null){
                result = candidateComponent;
                continue;
            }
            if(result.getDecisionFusionResult()<candidateComponent.getDecisionFusionResult()){
                result = candidateComponent;
            }

        }

        return result;
    }
}
