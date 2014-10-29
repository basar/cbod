package net.bsrc.cbod.main;

import java.util.ArrayList;
import java.util.List;

import libsvm.*;
import net.bsrc.cbod.core.*;
import net.bsrc.cbod.core.model.EDescriptorType;
import net.bsrc.cbod.core.model.EObjectType;
import net.bsrc.cbod.core.model.ImageModel;
import net.bsrc.cbod.core.persistence.DB4O;
import net.bsrc.cbod.core.persistence.ImageModelService;
import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.experiment.CbodExperiment;
import net.bsrc.cbod.jseg.JSEG;
import net.bsrc.cbod.jseg.JSEGParameter;
import net.bsrc.cbod.jseg.JSEGParameterFactory;
import net.bsrc.cbod.opencv.OpenCV;
import net.bsrc.cbod.pascal.EPascalType;
import net.bsrc.cbod.pascal.PascalVOC;
import net.bsrc.cbod.pascal.xml.PascalAnnotation;
import net.bsrc.cbod.pascal.xml.PascalObject;

import net.bsrc.cbod.svm.libsvm.LibSvm;
import net.bsrc.cbod.svm.libsvm.LibSvmUtil;
import net.bsrc.cbod.svm.libsvm.SvmModelPair;
import org.opencv.core.*;
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


        libSvmUsage();


//        CbodExperiment.doExperiment(new NormDivisionNormalization(),false,
//                EObjectType.TAIL_LIGHT, EObjectType.NONE_CAR_PART,
//                EObjectType.TAIL_LIGHT, EDescriptorType.EHD);

        // DBInitializeUtil.saveImageModelstoDB();

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


    private static void libSvmUsage() {

        ImageModelService service = ImageModelService.getInstance();

        //taillight and none-car part with ehd descriptors

        List<ImageModel> tailLightModels = service.getImageModelList(
                EObjectType.TAIL_LIGHT,false,400);
        List<ImageModel> wheelModels = service.getImageModelList(EObjectType.WHEEL,false,400);
        List<ImageModel> nonCarPartModels = service.getImageModelList(EObjectType.NONE_CAR_PART,false,400);

        List<ImageModel> testModels = service.getImageModelList(
                EObjectType.TAIL_LIGHT, true, 100);

        INormalization normalization = new ZScoreNormalization();

        List<List<Double>> tailLightEhdDataLists = ImageModel.getDescriptorDataLists(tailLightModels, EDescriptorType.EHD);
        List<List<Double>> nonCarPartEhdDataLists = ImageModel.getDescriptorDataLists(nonCarPartModels, EDescriptorType.EHD);
        List<List<Double>> wheelEhdDataLists = ImageModel.getDescriptorDataLists(wheelModels,EDescriptorType.EHD);
        List<List<Double>> testDataLists = ImageModel.getDescriptorDataLists(testModels, EDescriptorType.EHD);


        normalization.applyNormalizations(tailLightEhdDataLists);
        normalization.applyNormalizations(nonCarPartEhdDataLists);
        normalization.applyNormalizations(wheelEhdDataLists);
        normalization.applyNormalizations(testDataLists);

        List<SvmModelPair> pairs = new ArrayList<SvmModelPair>();
        pairs.add(new SvmModelPair(0, tailLightEhdDataLists));
        pairs.add(new SvmModelPair(1, nonCarPartEhdDataLists));
        pairs.add(new SvmModelPair(2,wheelEhdDataLists));

        svm_problem svmProblem = LibSvmUtil.createSvmProblem(pairs);


        svm_parameter param = new svm_parameter();
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.RBF;
        param.probability = 1;
        param.gamma = 1.0;
        param.C = 3268.0;

        //default values
        param.coef0 = 0;
        param.nu = 0.5;
        param.p = 0.1;
        param.cache_size = 20000;
        param.eps = 0.001;

        LibSvmUtil.doCrossValidation(svmProblem,param,5,3,4);


        /*
        svm_model svmModel=svm.svm_train(svmProblem,param);

        int success = 0;
        for(int i=0;i<testDataLists.size();i++){

            List<Double> temp = testDataLists.get(i);
            svm_node[] testNode = new svm_node[temp.size()];

            for(int j=0;j<temp.size();j++){
                svm_node node = new svm_node();
                node.index = j+1;
                node.value = temp.get(j);

                testNode[j] = node;
            }

            double[] prob_estimates = new double[2];
            double v = svm.svm_predict_probability(svmModel, testNode, prob_estimates);
           // double v=svm.svm_predict(svmModel,testNode);
            if(v==0.0){
                success++;
            }
        }

        logger.debug("success:"+success);
        */


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
