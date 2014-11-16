package net.bsrc.cbod.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.googlecode.javacv.cpp.opencv_core.*;
import libsvm.*;
import net.bsrc.cbod.core.*;
import net.bsrc.cbod.core.model.Descriptor;
import net.bsrc.cbod.core.model.EDescriptorType;
import net.bsrc.cbod.core.model.EObjectType;
import net.bsrc.cbod.core.model.ImageModel;
import net.bsrc.cbod.core.persistence.DB4O;
import net.bsrc.cbod.core.persistence.ImageModelService;
import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.core.util.DBInitializeUtil;
import net.bsrc.cbod.experiment.CbodExperiment;
import net.bsrc.cbod.jseg.JSEG;
import net.bsrc.cbod.jseg.JSEGParameter;
import net.bsrc.cbod.jseg.JSEGParameterFactory;
import net.bsrc.cbod.mpeg.bil.BilMpeg7Fex;
import net.bsrc.cbod.opencv.OpenCV;
import net.bsrc.cbod.pascal.EPascalType;
import net.bsrc.cbod.pascal.PascalVOC;
import net.bsrc.cbod.pascal.xml.PascalAnnotation;
import net.bsrc.cbod.pascal.xml.PascalObject;

import net.bsrc.cbod.svm.libsvm.LibSvm;
import net.bsrc.cbod.svm.libsvm.LibSvmUtil;
import net.bsrc.cbod.svm.libsvm.SvmModelPair;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.util.MathArrays;
import org.opencv.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.googlecode.javacv.cpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImageM;

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


        doPredictionWithMultiClassSVMs("1.jpg", new ZScoreNormalization());

//        CbodExperiment.doExperiment(new NormDivisionNormalization(),false,
//                EObjectType.TAIL_LIGHT, EObjectType.NONE_CAR_PART,
//                EObjectType.TAIL_LIGHT, EDescriptorType.EHD);

        //DBInitializeUtil.saveImageModelstoDB();

        // createModelFiles();
        // testObjectDetection("test_32.jpg");

        // CbodExperiment.doExperiment(EObjectType.WHEEL,
        // EObjectType.NONE_CAR_PART, EObjectType.WHEEL, 400, 100,
        // EDescriptorType.HOG);


//        JSEGParameter param =
//                JSEGParameterFactory.createJSEGParameter(IMG_DIR
//                        + "7.jpg", CBODUtil.getCbodTempDirectory());
//        param.setFactor(0.5);
//        param.setColorQuantizationThreshold(150);
//        param.setRegionMergeThreshold(0.4);
//        param.setNumberOfScales(3);
//        JSEG.doSegmentation(param);

        DB4O.getInstance().close();
    }


    private static void doPredictionWithMultiClassSVMs(String imageName, INormalization normalization) {

        // Test yapilacak image
        ImageModel imageModel = ImageModelFactory.createImageModel(IMG_DIR.concat(imageName), true);

        JSEGParameter jsegParam = new JSEGParameter(imageModel.getImagePath());
        jsegParam.setFactor(0.5);
        jsegParam.setColorQuantizationThreshold(150);
        jsegParam.setRegionMergeThreshold(0.4);
        jsegParam.setNumberOfScales(3);

        //Image segmentlere ayriliyor
        List<ImageModel> imageSegments = JSEG.segmentImage(
                imageModel.getImagePath(), jsegParam);
        //Her bir image model icin featurelar cikartiliyor

        //SIFT and HOG Descriptor
        CvMat dictMat = OpenCV.loadCvMatFromFile(TMP_DIR.concat("sift_dict.xml"), "sift_dict");
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

            for (ImageModel im : imageSegments) {

                double siftProbNonCar = getPredictedPropability(siftModel, im, EDescriptorType.SIFT, 0);
                double hogProbNonCar = getPredictedPropability(hogModel, im, EDescriptorType.HOG, 0);
                double ehdProbNonCar = getPredictedPropability(ehdModel, im, EDescriptorType.EHD, 0);
                double csdProbNonCar = getPredictedPropability(csdModel, im, EDescriptorType.CSD, 0);
                double cldProbNonCar = getPredictedPropability(cldModel, im, EDescriptorType.CLD, 0);
                double scdProbNonCar = getPredictedPropability(scdModel, im, EDescriptorType.SCD, 0);
                double dcdProbNonCar = getPredictedPropability(dcdModel, im, EDescriptorType.DCD, 0);

                double bayesNonCarPart = ((siftWeight * siftProbNonCar)
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

                double bayesTailLight = ((siftWeight * siftProbTailLight)
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


                double bayesWheel = ((siftWeight * siftProbWheel)
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


                double bayesLicensePlate = ((siftWeight * siftProbLicensePlate)
                        + (hogWeight * hogProbLicensePlate) + (ehdWeight * ehdProbLicensePlate)
                        + (csdWeight * csdProbLicensePlate) + (cldWeight * cldProbLicensePlate)
                        + (scdWeight * scdProbLicensePlate) + (dcdWeight * dcdProbLicensePlate)) / totalWeight;

                int maxIndex = CBODUtil.getIndexWithMaxValue(bayesNonCarPart,bayesTailLight,bayesWheel,bayesLicensePlate);

                switch  (maxIndex){
                    case 0:

                        break;
                    case 1:
                        System.out.println("taillight");
                        break;
                    case 2:
                        System.out.println("wheel");
                        break;
                    case 3:
                        System.out.println("license plate");
                        break;
                    default:
                }

            }

        } catch (IOException e) {
            logger.error("", e);
        }


    }


    private static double getPredictedPropability(svm_model svmModel, ImageModel im, EDescriptorType descriptorType, int index) {


        Descriptor descriptor = im.getDescriptor(descriptorType);
        if (descriptor == null)
            return 0.0;
        List<Double> dataList = descriptor.getDataList();

        svm_node[] nodes = createSVMNodeArray(dataList);
        double[] probs = new double[4];
        svm.svm_predict_probability(svmModel, nodes, probs);

        return probs[index];
    }


    private static void createMultiClassSVMModels(INormalization normalization) {

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


    private static void doCrossValidationForMultiClassSVM(EDescriptorType descriptorType, INormalization normalization) {

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

        svm_parameter param = new svm_parameter();
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.RBF;
        param.probability = 1;
        param.gamma = 0.015625;
        param.C = 32768.0;

        //default values
        param.coef0 = 0;
        param.nu = 0.5;
        param.p = 0.1;
        param.cache_size = 20000;
        param.eps = 0.001;

        LibSvmUtil.doCrossValidation(svmProblem, param, 5, 3, 4);

    }


    private static void createAndSaveMultiClassSvmModel(String modelName, EDescriptorType descriptorType, INormalization normalization, double c, double gamma) {


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

        svm_parameter param = new svm_parameter();
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.RBF;
        param.probability = 1;
        param.gamma = gamma;
        param.C = c;

        //default values
        param.coef0 = 0;
        param.nu = 0.5;
        param.p = 0.1;
        param.cache_size = 20000;
        param.eps = 0.001;


        svm_model svmModel = svm.svm_train(svmProblem, param);

        try {
            svm.svm_save_model(LibSvm.getSvmDirectoryPath().concat("/").concat(modelName), svmModel);
        } catch (IOException e) {
            logger.error("", e);
        }

    }


    private static void libSvmUsage() {

        ImageModelService service = ImageModelService.getInstance();


        List<ImageModel> tailLightModels = service.getImageModelList(
                EObjectType.TAIL_LIGHT, false, 400);
        List<ImageModel> wheelModels = service.getImageModelList(EObjectType.WHEEL, false, 400);
        List<ImageModel> licensePlateModels = service.getImageModelList(EObjectType.LICENSE_PLATE, false, 400);
        List<ImageModel> nonCarPartModels = service.getImageModelList(EObjectType.NONE_CAR_PART, false, 400);

        List<ImageModel> testModels = service.getImageModelList(
                EObjectType.TAIL_LIGHT, true, 100);

        INormalization normalization = new ZScoreNormalization();

        List<List<Double>> tailLightEhdDataLists = ImageModel.getDescriptorDataLists(tailLightModels, EDescriptorType.EHD);
        List<List<Double>> nonCarPartEhdDataLists = ImageModel.getDescriptorDataLists(nonCarPartModels, EDescriptorType.EHD);
        List<List<Double>> wheelEhdDataLists = ImageModel.getDescriptorDataLists(wheelModels, EDescriptorType.EHD);
        List<List<Double>> licensePlateEhdDataLists = ImageModel.getDescriptorDataLists(licensePlateModels, EDescriptorType.EHD);
        List<List<Double>> testDataLists = ImageModel.getDescriptorDataLists(testModels, EDescriptorType.EHD);


        normalization.applyNormalizations(tailLightEhdDataLists);
        normalization.applyNormalizations(nonCarPartEhdDataLists);
        normalization.applyNormalizations(wheelEhdDataLists);
        normalization.applyNormalizations(licensePlateEhdDataLists);
        normalization.applyNormalizations(testDataLists);

        List<SvmModelPair> pairs = new ArrayList<SvmModelPair>();
        pairs.add(new SvmModelPair(EObjectType.NONE_CAR_PART.ordinal(), nonCarPartEhdDataLists));
        pairs.add(new SvmModelPair(EObjectType.TAIL_LIGHT.ordinal(), tailLightEhdDataLists));
        pairs.add(new SvmModelPair(EObjectType.WHEEL.ordinal(), wheelEhdDataLists));
        pairs.add(new SvmModelPair(EObjectType.LICENSE_PLATE.ordinal(), licensePlateEhdDataLists));

        svm_problem svmProblem = LibSvmUtil.createSvmProblem(pairs);


        svm_parameter param = new svm_parameter();
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.RBF;
        param.probability = 1;
        param.gamma = 0.015625;
        param.C = 32768.0;

        //default values
        param.coef0 = 0;
        param.nu = 0.5;
        param.p = 0.1;
        param.cache_size = 20000;
        param.eps = 0.001;

        //LibSvmUtil.doCrossValidation(svmProblem, param, 5, 3, 4);


        svm_model svmModel = svm.svm_train(svmProblem, param);


        int success = 0;
        for (int i = 0; i < testDataLists.size(); i++) {

            List<Double> temp = testDataLists.get(i);
            svm_node[] testNode = new svm_node[temp.size()];

            for (int j = 0; j < temp.size(); j++) {
                svm_node node = new svm_node();
                node.index = j + 1;
                node.value = temp.get(j);

                testNode[j] = node;
            }

            double[] prob_estimates = new double[4];
            double v = svm.svm_predict_probability(svmModel, testNode, prob_estimates);
            // double v=svm.svm_predict(svmModel,testNode);
            if (v == EObjectType.TAIL_LIGHT.ordinal()) {
                success++;
            }
        }

        logger.debug("success:" + success);


    }


    private static svm_node[] createSVMNodeArray(List<Double> list) {

        svm_node[] svmNodeArr = new svm_node[list.size()];

        for (int j = 0; j < list.size(); j++) {
            svm_node node = new svm_node();
            node.index = j + 1;
            node.value = list.get(j);

            svmNodeArr[j] = node;
        }

        return svmNodeArr;
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
        List<ImageModel> imageSegments = JSEG.segmentImage(
                imageModel.getImagePath(), null);

        findCandidateWheels(imageModel, imageSegments);
        // findCandidateHeadlights(imageModel, imageSegments);
        // findCandidateTaillights(imageModel, imageSegments);

    }

    /**
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
