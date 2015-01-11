package net.bsrc.cbod.main;

import com.googlecode.javacv.cpp.opencv_core.CvMat;
import jsr166y.ForkJoinPool;
import jsr166y.RecursiveAction;
import libsvm.*;
import net.bsrc.cbod.core.*;
import net.bsrc.cbod.core.model.Descriptor;
import net.bsrc.cbod.core.model.EDescriptorType;
import net.bsrc.cbod.core.model.EObjectType;
import net.bsrc.cbod.core.model.ImageModel;
import net.bsrc.cbod.core.persistence.DB4O;
import net.bsrc.cbod.core.persistence.ImageModelService;
import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.jseg.JSEG;
import net.bsrc.cbod.jseg.JSEGParameter;
import net.bsrc.cbod.mpeg.bil.BilMpeg7Fex;
import net.bsrc.cbod.opencv.OpenCV;
import net.bsrc.cbod.svm.libsvm.LibSvm;
import net.bsrc.cbod.svm.libsvm.LibSvmUtil;
import net.bsrc.cbod.svm.libsvm.SvmModelPair;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Main class for application
 */
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


        //doPredictionWithBinarySVMs("IMG_9.jpg",new ZScoreNormalization());
        doPredictionWithMultiClassSVMs("10.jpg",new ZScoreNormalization());

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

            List<ImageModel> possibleTailLights = new ArrayList<ImageModel>();
            List<ImageModel> possibleWheels = new ArrayList<ImageModel>();
            List<ImageModel> possibleLicensePlate = new ArrayList<ImageModel>();

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

                int maxIndex = CBODUtil.getIndexWithMaxValue(bayesNonCarPart, bayesTailLight, bayesWheel, bayesLicensePlate);

                switch (maxIndex) {
                    case 0:
                        break;
                    case 1:
                        if (bayesTailLight > 0.50) {
                            possibleTailLights.add(im);
                        }
                        break;
                    case 2:
                        if (bayesWheel > 0.40)
                            possibleWheels.add(im);
                        break;
                    case 3:
                        if (bayesLicensePlate > 0.50)
                            possibleLicensePlate.add(im);
                        break;
                    default:
                }

            }

            List<ImageModel> possibleCarParts = new ArrayList<ImageModel>();
            possibleCarParts.addAll(possibleTailLights);
            possibleCarParts.addAll(possibleWheels);
            possibleCarParts.addAll(possibleLicensePlate);

            Mat copy = OpenCV.copyImage(imageModel.getMat());

            Scalar blue = new Scalar(255, 0, 0);
            Scalar green = new Scalar(0, 255, 0);
            Scalar red = new Scalar(0, 0, 255);

            for (ImageModel candidate : possibleTailLights) {
                Rect rect = candidate.getRelativeToOrg();
                OpenCV.drawRect(rect, copy, green);
            }

            for (ImageModel candidate : possibleWheels) {
                Rect rect = candidate.getRelativeToOrg();
                OpenCV.drawRect(rect, copy, red);
            }

            for (ImageModel candidate : possibleLicensePlate) {
                Rect rect = candidate.getRelativeToOrg();
                OpenCV.drawRect(rect, copy, blue);
            }

            String outputImagePath = TMP_DIR.concat(imageModel.getRawImageName() + ".out.jpg");
            OpenCV.writeImage(copy, outputImagePath);

        } catch (IOException e) {
            logger.error("", e);
        }

    }


    private static double getPredictedPropability(svm_model svmModel, ImageModel im, EDescriptorType descriptorType, int index) {


        Descriptor descriptor = im.getDescriptor(descriptorType);
        if (descriptor == null)
            return 0.0;
        List<Double> dataList = descriptor.getDataList();

        svm_node[] nodes = LibSvmUtil.createSVMNodeArray(dataList);
        double[] probs = new double[4];
        svm.svm_predict_probability(svmModel, nodes, probs);

        return probs[index];
    }


    private static void doPredictionWithBinarySVMs(String imageName, INormalization normalization) {

        EDescriptorType[] descriptorTypes = new EDescriptorType[]{EDescriptorType.EHD,
                EDescriptorType.SIFT, EDescriptorType.CLD, EDescriptorType.CSD,
                EDescriptorType.SCD, EDescriptorType.DCD, EDescriptorType.HOG};

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

        try {

            //SVM models
            svm_model wheelNonCarPartModel = svm.svm_load_model(LibSvm.getSvmDirectoryPath().concat("/").concat("B.WHEEL_NON_CAR_PART.txt"));
            svm_model tailLightNonCarPartModel = svm.svm_load_model(LibSvm.getSvmDirectoryPath().concat("/").concat("B.TAIL_LIGHT_NON_CAR_PART.txt"));
            svm_model licensePlateNonCarPartModel = svm.svm_load_model(LibSvm.getSvmDirectoryPath().concat("/").concat("B.LICENSE_PLATE_NON_CAR_PART.txt"));

            List<ImageModel> possibleTailLights = new ArrayList<ImageModel>();
            List<ImageModel> possibleWheels = new ArrayList<ImageModel>();
            List<ImageModel> possibleLicensePlates = new ArrayList<ImageModel>();

            for (ImageModel im : imageSegments) {

                List<Double> dataListForWheel = CBODUtil.concatDataList(createDescriptorList(im,EDescriptorType.HOG,
                        EDescriptorType.CSD, EDescriptorType.EHD,
                        EDescriptorType.SCD, EDescriptorType.CLD,
                        EDescriptorType.SIFT, EDescriptorType.DCD));
                List<Double> dataListForTailLights = CBODUtil.concatDataList(createDescriptorList(im,EDescriptorType.SCD,EDescriptorType.DCD));
                List<Double> dataListForLicensePlates = CBODUtil.concatDataList(createDescriptorList(im,EDescriptorType.HOG,EDescriptorType.EHD,EDescriptorType.CLD));
                normalization.applyNormalization(dataListForWheel);
                normalization.applyNormalization(dataListForTailLights);
                normalization.applyNormalization(dataListForLicensePlates);

                svm_node[] wheelNodes = LibSvmUtil.createSVMNodeArray(dataListForWheel);
                svm_node[] tailLightNodes = LibSvmUtil.createSVMNodeArray(dataListForTailLights);
                svm_node[] licensePlateNodes = LibSvmUtil.createSVMNodeArray(dataListForLicensePlates);


                if (EObjectType.WHEEL.ordinal() == svm.svm_predict(wheelNonCarPartModel, wheelNodes)) {
                    possibleWheels.add(im);
                }

                if (EObjectType.TAIL_LIGHT.ordinal() == svm.svm_predict(tailLightNonCarPartModel, tailLightNodes)) {
                    possibleTailLights.add(im);
                }

                if (EObjectType.LICENSE_PLATE.ordinal() == svm.svm_predict(licensePlateNonCarPartModel, licensePlateNodes)) {
                    possibleLicensePlates.add(im);
                }
            }

            Mat copy = OpenCV.copyImage(imageModel.getMat());

            Scalar blue = new Scalar(255, 0, 0);
            Scalar green = new Scalar(0, 255, 0);
            Scalar red = new Scalar(0, 0, 255);

            for (ImageModel candidate : possibleTailLights) {
                Rect rect = candidate.getRelativeToOrg();
                OpenCV.drawRect(rect, copy, green);
            }

            for (ImageModel candidate : possibleWheels) {
                Rect rect = candidate.getRelativeToOrg();
                OpenCV.drawRect(rect, copy, red);
            }

            for (ImageModel candidate : possibleLicensePlates) {
                Rect rect = candidate.getRelativeToOrg();
                OpenCV.drawRect(rect, copy, blue);
            }

            String outputImagePath = TMP_DIR.concat(imageModel.getRawImageName() + ".out.jpg");
            OpenCV.writeImage(copy, outputImagePath);

        } catch (IOException ex) {
            logger.error("", ex);
        }
    }


    private static List<List<Double>> createDescriptorList(ImageModel im,EDescriptorType... descriptorTypes){

        List<List<Double>> descriptors = new ArrayList<List<Double>>();
        for (int i = 0; i < descriptorTypes.length; i++) {

            EDescriptorType descriptorType = descriptorTypes[i];
            List<Double> descsDataList = im.getDescriptorDataList(descriptorType);
            descriptors.add(descsDataList);
        }

        return descriptors;
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


    private static void createBinarySVMModels(INormalization normalization) {

        createAndSaveBinarySVMModel("B.WHEEL_NON_CAR_PART.txt", EObjectType.WHEEL, EObjectType.NONE_CAR_PART, normalization, Math.pow(2.0,15.0),Math.pow(2.0,-12.0), EDescriptorType.HOG,
                EDescriptorType.CSD, EDescriptorType.EHD,
                EDescriptorType.SCD, EDescriptorType.CLD,
                EDescriptorType.SIFT, EDescriptorType.DCD);
        createAndSaveBinarySVMModel("B.TAIL_LIGHT_NON_CAR_PART.txt", EObjectType.TAIL_LIGHT, EObjectType.NONE_CAR_PART, normalization, Math.pow(2.0,10.0),Math.pow(2.0,-12.0),
                EDescriptorType.SCD,EDescriptorType.DCD);
        createAndSaveBinarySVMModel("B.LICENSE_PLATE_NON_CAR_PART.txt", EObjectType.LICENSE_PLATE, EObjectType.NONE_CAR_PART, normalization, Math.pow(2.0,15.0),Math.pow(2.0,-6.0),
                EDescriptorType.HOG,EDescriptorType.EHD,EDescriptorType.CLD);
    }


    private static void doCrossValidationForMultiClassSVM(EDescriptorType descriptorType, INormalization normalization) {

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


    private static void doCrossValidationForBinarySVM(EObjectType objectType1, EObjectType objectType2, INormalization normalization, EDescriptorType... descriptorTypes) {

        ImageModelService service = ImageModelService.getInstance();

        List<List<Double>>[] trainDataArr1 = new List[descriptorTypes.length];
        List<List<Double>>[] trainDataArr2 = new List[descriptorTypes.length];
        List<ImageModel> imageModels1 = service.getImageModelList(
                objectType1, 500);
        List<ImageModel> imageModels2 = service.getImageModelList(
                objectType2, 500);

        for (int i = 0; i < descriptorTypes.length; i++) {
            EDescriptorType descriptorType = descriptorTypes[i];
            trainDataArr1[i] = ImageModel
                    .getDescriptorDataLists(imageModels1, descriptorType);
            trainDataArr2[i] = ImageModel
                    .getDescriptorDataLists(imageModels2, descriptorType);
        }
        List<List<Double>> concatLists1 = CBODUtil
                .concatDataLists(trainDataArr1);
        List<List<Double>> concatLists2 = CBODUtil

                .concatDataLists(trainDataArr2);
        normalization.applyNormalizations(concatLists1);
        normalization.applyNormalizations(concatLists2);

        List<SvmModelPair> pairs = new ArrayList<SvmModelPair>();
        pairs.add(new SvmModelPair(objectType1.ordinal(), concatLists1));
        pairs.add(new SvmModelPair(objectType2.ordinal(), concatLists2));

        svm_problem svmProblem = LibSvmUtil.createSvmProblem(pairs);
        svm_parameter param = LibSvmUtil.createSvmParameter(0.0, 0.0, false);

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
        svm_parameter param = LibSvmUtil.createSvmParameter(c, gamma, true);

        svm_model svmModel = svm.svm_train(svmProblem, param);

        try {
            svm.svm_save_model(LibSvm.getSvmDirectoryPath().concat("/").concat(modelName), svmModel);
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    private static void createAndSaveBinarySVMModel(String modelName, EObjectType objectType1, EObjectType objectType2,
                                                    INormalization normalization, double c, double gamma, EDescriptorType... descriptorTypes) {

        ImageModelService service = ImageModelService.getInstance();

        List<List<Double>>[] trainDataArr1 = new List[descriptorTypes.length];
        List<List<Double>>[] trainDataArr2 = new List[descriptorTypes.length];

        List<ImageModel> imageModels1 = service.getImageModelList(
                objectType1, 500);
        List<ImageModel> imageModels2 = service.getImageModelList(
                objectType2, 500);

        for (int i = 0; i < descriptorTypes.length; i++) {
            EDescriptorType descriptorType = descriptorTypes[i];
            trainDataArr1[i] = ImageModel
                    .getDescriptorDataLists(imageModels1, descriptorType);
            trainDataArr2[i] = ImageModel
                    .getDescriptorDataLists(imageModels2, descriptorType);
        }

        List<List<Double>> concatLists1 = CBODUtil
                .concatDataLists(trainDataArr1);
        List<List<Double>> concatLists2 = CBODUtil
                .concatDataLists(trainDataArr2);

        normalization.applyNormalizations(concatLists1);
        normalization.applyNormalizations(concatLists2);

        List<SvmModelPair> pairs = new ArrayList<SvmModelPair>();
        pairs.add(new SvmModelPair(objectType1.ordinal(), concatLists1));
        pairs.add(new SvmModelPair(objectType2.ordinal(), concatLists2));

        svm_problem svmProblem = LibSvmUtil.createSvmProblem(pairs);
        svm_parameter param = LibSvmUtil.createSvmParameter(c, gamma, false);
        svm_model svmModel = svm.svm_train(svmProblem, param);
        try {
            svm.svm_save_model(LibSvm.getSvmDirectoryPath().concat("/").concat(modelName), svmModel);
        } catch (IOException e) {
            logger.error("", e);
        }
    }




}