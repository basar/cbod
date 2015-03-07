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
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: bsr
 * Date: 07/03/15
 * Time: 15:38
 */
public class BinarySVM {


    private static final Logger logger = LoggerFactory
            .getLogger(BinarySVM.class);

    private static BinarySVM binarySVM = null;

    /**
     * To prevent object creation
     */
    private BinarySVM() {

    }

    public static BinarySVM getInstance() {
        if (binarySVM == null) {
            synchronized (BinarySVM.class) {
                if (binarySVM == null) {
                    binarySVM = createBinaryFusedSVM();
                }
            }
        }
        return binarySVM;
    }

    private static BinarySVM createBinaryFusedSVM() {
        return new BinarySVM();
    }


    public SVMPredictionResult doPredictionWithBinarySVMs(String imageName, INormalization normalization) {

        SVMPredictionResult result = new SVMPredictionResult();
        result.setSvmType(ESVMType.BINARY_WITH_FEATURE_FUSION);

        // Test yapilacak image
        ImageModel imageModel = ImageModelFactory.createImageModel(CBODUtil.getCbodInputImageDirectory().concat("/").concat(imageName), true);
        result.setInputImageModel(imageModel);


        JSEGParameter jsegParam = JSEGParameterFactory.createJsegParameterWithDefultCbodValues(imageModel.getImagePath());

        //Image segmentlere ayriliyor
        List<ImageModel> imageSegments = JSEG.segmentImage(
                imageModel.getImagePath(), jsegParam);
        //Burada parca bazli en yuksek iki oznitelik kullanilacak teker ve plaka icin HOG arka far icin SCD.
        //Bu nedenle parcalardan sadece bu oznitelikler cikarilmali

        //HOG Descriptor
        for (ImageModel im : imageSegments) {
            Descriptor hogDesc = new Descriptor();
            hogDesc.setType(EDescriptorType.HOG);
            hogDesc.setDataList(CBODHog.extractHogDescriptor(im));
            im.getDescriptors().add(hogDesc);
        }

        BilMpeg7Fex mpeg7Fex = BilMpeg7Fex.getInstance();
        //SCD
        mpeg7Fex.extractScalableColorDescriptors(imageSegments, 256);

        //SVM models
        try {

            svm_model wheelNonCarPartModel = svm.svm_load_model(LibSvm.getSvmDirectoryPath().concat("/").concat("B.HOG.WHEEL_NON_CAR_PART.txt"));
            svm_model tailLightNonCarPartModel = svm.svm_load_model(LibSvm.getSvmDirectoryPath().concat("/").concat("B.SCD.TAIL_LIGHT_NON_CAR_PART.txt"));
            svm_model licensePlateNonCarPartModel = svm.svm_load_model(LibSvm.getSvmDirectoryPath().concat("/").concat("B.HOG.LICENSE_PLATE_NON_CAR_PART.txt"));

            Long componentId = 0l;
            for (ImageModel im : imageSegments) {

                Descriptor hogDescriptor = im.getDescriptor(EDescriptorType.HOG);
                Descriptor scdDescriptor = im.getDescriptor(EDescriptorType.SCD);
                List<Double> hogDataList = hogDescriptor.getDataList();
                List<Double> scdDataList = scdDescriptor.getDataList();

                normalization.applyNormalization(hogDataList);
                normalization.applyNormalization(scdDataList);

                result.addSegmentedInputImageModel(im);

                svm_node[] hogNodes = LibSvmUtil.createSVMNodeArray(hogDataList);
                svm_node[] scdNodes = LibSvmUtil.createSVMNodeArray(scdDataList);

                CandidateComponent candidateComponent = null;
                if (EObjectType.WHEEL.ordinal() == svm.svm_predict(wheelNonCarPartModel, hogNodes)) {
                    candidateComponent = new CandidateComponent();
                    candidateComponent.setId(++componentId);
                    candidateComponent.setRect(im.getRelativeToOrg());
                    candidateComponent.setObjectType(EObjectType.WHEEL);
                }

                if (EObjectType.TAIL_LIGHT.ordinal() == svm.svm_predict(tailLightNonCarPartModel, scdNodes)) {
                    candidateComponent = new CandidateComponent();
                    candidateComponent.setId(++componentId);
                    candidateComponent.setRect(im.getRelativeToOrg());
                    candidateComponent.setObjectType(EObjectType.TAIL_LIGHT);
                }

                if (EObjectType.LICENSE_PLATE.ordinal() == svm.svm_predict(licensePlateNonCarPartModel, hogNodes)) {
                    candidateComponent = new CandidateComponent();
                    candidateComponent.setId(++componentId);
                    candidateComponent.setRect(im.getRelativeToOrg());
                    candidateComponent.setObjectType(EObjectType.LICENSE_PLATE);
                }

                if (candidateComponent != null) {
                    result.addCandidateComponent(candidateComponent);
                }
            }


        } catch (IOException ex) {
            logger.error("", ex);
        }

        return result;
    }


    public SVMPredictionResult doPredictionWithFusedBinarySVMs(String imageName, INormalization normalization) {

        SVMPredictionResult result = new SVMPredictionResult();
        result.setSvmType(ESVMType.BINARY_WITH_FEATURE_FUSION);

        EDescriptorType[] descriptorTypes = new EDescriptorType[]{EDescriptorType.EHD,
                EDescriptorType.SIFT, EDescriptorType.CLD, EDescriptorType.CSD,
                EDescriptorType.SCD, EDescriptorType.DCD, EDescriptorType.HOG};

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

        try {

            //SVM models
            svm_model wheelNonCarPartModel = svm.svm_load_model(LibSvm.getSvmDirectoryPath().concat("/").concat("B.FUSED.WHEEL_NON_CAR_PART.txt"));
            svm_model tailLightNonCarPartModel = svm.svm_load_model(LibSvm.getSvmDirectoryPath().concat("/").concat("B.FUSED.TAIL_LIGHT_NON_CAR_PART.txt"));
            svm_model licensePlateNonCarPartModel = svm.svm_load_model(LibSvm.getSvmDirectoryPath().concat("/").concat("B.FUSED.LICENSE_PLATE_NON_CAR_PART.txt"));

            Long componentId = 0l;
            for (ImageModel im : imageSegments) {

                List<Double> dataListForWheel = CBODUtil.concatDataList(createDescriptorList(im, EDescriptorType.HOG,
                        EDescriptorType.CSD, EDescriptorType.EHD,
                        EDescriptorType.SCD, EDescriptorType.CLD,
                        EDescriptorType.SIFT, EDescriptorType.DCD));
                List<Double> dataListForTailLights = CBODUtil.concatDataList(createDescriptorList(im, EDescriptorType.SCD, EDescriptorType.DCD));
                List<Double> dataListForLicensePlates = CBODUtil.concatDataList(createDescriptorList(im, EDescriptorType.HOG, EDescriptorType.EHD, EDescriptorType.CLD));
                normalization.applyNormalization(dataListForWheel);
                normalization.applyNormalization(dataListForTailLights);
                normalization.applyNormalization(dataListForLicensePlates);

                result.addSegmentedInputImageModel(im);

                svm_node[] wheelNodes = LibSvmUtil.createSVMNodeArray(dataListForWheel);
                svm_node[] tailLightNodes = LibSvmUtil.createSVMNodeArray(dataListForTailLights);
                svm_node[] licensePlateNodes = LibSvmUtil.createSVMNodeArray(dataListForLicensePlates);

                CandidateComponent candidateComponent = null;

                if (EObjectType.WHEEL.ordinal() == svm.svm_predict(wheelNonCarPartModel, wheelNodes)) {
                    candidateComponent = new CandidateComponent();
                    candidateComponent.setId(++componentId);
                    candidateComponent.setRect(im.getRelativeToOrg());
                    candidateComponent.setObjectType(EObjectType.WHEEL);
                }

                if (EObjectType.TAIL_LIGHT.ordinal() == svm.svm_predict(tailLightNonCarPartModel, tailLightNodes)) {
                    candidateComponent = new CandidateComponent();
                    candidateComponent.setId(++componentId);
                    candidateComponent.setRect(im.getRelativeToOrg());
                    candidateComponent.setObjectType(EObjectType.TAIL_LIGHT);
                }

                if (EObjectType.LICENSE_PLATE.ordinal() == svm.svm_predict(licensePlateNonCarPartModel, licensePlateNodes)) {
                    candidateComponent = new CandidateComponent();
                    candidateComponent.setId(++componentId);
                    candidateComponent.setRect(im.getRelativeToOrg());
                    candidateComponent.setObjectType(EObjectType.LICENSE_PLATE);
                }

                if (candidateComponent != null) {
                    result.addCandidateComponent(candidateComponent);
                }
            }


        } catch (IOException ex) {
            logger.error("", ex);
        }

        return result;
    }


    private List<List<Double>> createDescriptorList(ImageModel im, EDescriptorType... descriptorTypes) {

        List<List<Double>> descriptors = new ArrayList<List<Double>>();
        for (int i = 0; i < descriptorTypes.length; i++) {

            EDescriptorType descriptorType = descriptorTypes[i];
            List<Double> descsDataList = im.getDescriptorDataList(descriptorType);
            descriptors.add(descsDataList);
        }

        return descriptors;
    }


    public void createBinarySVMFusedModels(INormalization normalization) {

        createAndSaveBinarySVMModel("B.WHEEL_NON_CAR_PART.txt", EObjectType.WHEEL, EObjectType.NONE_CAR_PART,
                normalization, Math.pow(2.0, 15.0), Math.pow(2.0, -12.0), EDescriptorType.HOG,
                EDescriptorType.CSD, EDescriptorType.EHD,
                EDescriptorType.SCD, EDescriptorType.CLD,
                EDescriptorType.SIFT, EDescriptorType.DCD);
        createAndSaveBinarySVMModel("B.TAIL_LIGHT_NON_CAR_PART.txt", EObjectType.TAIL_LIGHT,
                EObjectType.NONE_CAR_PART, normalization, Math.pow(2.0, 10.0), Math.pow(2.0, -12.0),
                EDescriptorType.SCD, EDescriptorType.DCD);
        createAndSaveBinarySVMModel("B.LICENSE_PLATE_NON_CAR_PART.txt",
                EObjectType.LICENSE_PLATE, EObjectType.NONE_CAR_PART, normalization, Math.pow(2.0, 15.0), Math.pow(2.0, -6.0),
                EDescriptorType.HOG, EDescriptorType.EHD, EDescriptorType.CLD);
    }


    public void createBinarySVMModels(INormalization normalization) {

        createAndSaveBinarySVMModel("B.HOG.WHEEL_NON_CAR_PART.txt", EObjectType.WHEEL, EObjectType.NONE_CAR_PART,
                new ZScoreNormalization(), Math.pow(2.0, 5.0), Math.pow(2.0, -12.0), EDescriptorType.HOG);

        createAndSaveBinarySVMModel("B.SCD.TAIL_LIGHT_NON_CAR_PART.txt", EObjectType.TAIL_LIGHT, EObjectType.NONE_CAR_PART,
                new ZScoreNormalization(), Math.pow(2.0, 10.0), Math.pow(2.0, -9.0), EDescriptorType.SCD);

        createAndSaveBinarySVMModel("B.HOG.LICENSE_PLATE_NON_CAR_PART.txt", EObjectType.LICENSE_PLATE, EObjectType.NONE_CAR_PART,
                new ZScoreNormalization(), Math.pow(2.0, 5.0), Math.pow(2.0, -12.0), EDescriptorType.HOG);
    }


    public void doCrossValidationForBinarySVM(EObjectType objectType1, EObjectType objectType2, INormalization normalization, EDescriptorType... descriptorTypes) {

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


    private void createAndSaveBinarySVMModel(String modelName, EObjectType objectType1, EObjectType objectType2,
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
