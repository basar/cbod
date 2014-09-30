package net.bsrc.cbod.core;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImageM;
import static com.googlecode.javacv.cpp.opencv_features2d.drawKeypoints;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_features2d;
import net.bsrc.cbod.core.model.ImageModel;
import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.opencv.OpenCV;

import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvTermCriteria;
import com.googlecode.javacv.cpp.opencv_core.IntVectorVector;
import com.googlecode.javacv.cpp.opencv_features2d.BOWImgDescriptorExtractor;
import com.googlecode.javacv.cpp.opencv_features2d.BOWKMeansTrainer;
import com.googlecode.javacv.cpp.opencv_features2d.DescriptorExtractor;
import com.googlecode.javacv.cpp.opencv_features2d.DescriptorMatcher;
import com.googlecode.javacv.cpp.opencv_features2d.FeatureDetector;
import com.googlecode.javacv.cpp.opencv_features2d.FlannBasedMatcher;
import com.googlecode.javacv.cpp.opencv_features2d.KeyPoint;
import com.googlecode.javacv.cpp.opencv_nonfree.SIFT;

public class CBODSift {

	public static CvMat createDictionary(List<ImageModel> imageModelList,
			int dictionarySize) {

		CvMat dictionary = null;

		SIFT sift = new SIFT();
		DescriptorExtractor extractor = sift.getDescriptorExtractor();
		FeatureDetector detector = sift.getFeatureDetector();

		List<CvMat> cvMatList = new ArrayList<CvMat>();

		for (int i = 0; i < imageModelList.size(); i++) {

			ImageModel imgModel = imageModelList.get(i);
			CvMat imageMat = cvLoadImageM(imgModel.getImagePath(),
					CV_LOAD_IMAGE_GRAYSCALE);

			KeyPoint keyPoints = new KeyPoint();
			// detect key points in the image
			detector.detect(imageMat, keyPoints, null);

			CvMat descriptors = new CvMat(null);
			// extractor descriptor for each keypoint
			extractor.compute(imageMat, keyPoints, descriptors);
			// Descriptor is not null
			if (!descriptors.isNull()) {
				cvMatList.add(descriptors);
			}
		}

		CvMat totalDescriptors = OpenCV.concatenateDescriptors(cvMatList,
				CV_32F);

		// KMEANS_PP_CENTER
		int flags = 2;

		CvTermCriteria termCriteria = new CvTermCriteria(CV_TERMCRIT_ITER, 100,
				0.001);
		BOWKMeansTrainer bowKMeansTrainer = new BOWKMeansTrainer(
				dictionarySize, termCriteria, 1, flags);

		dictionary = bowKMeansTrainer.cluster(totalDescriptors);

		return dictionary;

	}

	public static CvMat extractSIFTDescriptor(ImageModel imgModel,
			CvMat dictionary) {

		SIFT sift = new SIFT();
		DescriptorExtractor extractor = sift.getDescriptorExtractor();
		FeatureDetector detector = sift.getFeatureDetector();

		DescriptorMatcher descMatcher = new FlannBasedMatcher();
		//DescriptorMatcher descMatcher = new BFMatcher();

		BOWImgDescriptorExtractor bowDE = new BOWImgDescriptorExtractor(
				extractor, descMatcher);
		bowDE.setVocabulary(dictionary);

		CvMat imageMat = cvLoadImageM(imgModel.getImagePath(),
				CV_LOAD_IMAGE_GRAYSCALE);

		KeyPoint keyPoints = new KeyPoint();
		detector.detect(imageMat, keyPoints, null);

		CvMat outputImgDesc = new CvMat(null);
		CvMat temp = new CvMat(null);
		IntVectorVector intVectorVector = new IntVectorVector();
		bowDE.compute(imageMat, keyPoints, outputImgDesc, intVectorVector, temp);

		return outputImgDesc;
	}

	public static List<Double> extractSIFTDescriptorAsList(ImageModel imgModel,
			CvMat dictionary) {

		CvMat mat = extractSIFTDescriptor(imgModel, dictionary);

		List<Double> list = new ArrayList<Double>();
		if (!mat.isNull()) {
			for (int i = 0; i < mat.rows(); i++) {
				for (int j = 0; j < mat.cols(); j++) {
					list.add(mat.get(i, j));
				}
			}
		}
		return list;
	}


    public static void drawKeypointsFromImageModel(ImageModel imageModel){


        KeyPoint keyPoint = new KeyPoint();

        SIFT sift = new SIFT();
        //SIFT sift = new SIFT(0,3,0.04,5,1.2);
        FeatureDetector detector = sift.getFeatureDetector();

        //Orginal image
        CvMat orginalImg=cvLoadImageM(imageModel.getImagePath());
        //Gray image
        CvMat grayImg = CvMat.create(orginalImg.rows(),orginalImg.cols(),CV_8U);
        //Fill gray image
        cvCvtColor(orginalImg,grayImg,CV_BGR2GRAY);

        // detect key points in the image
        detector.detect(grayImg, keyPoint, null);

        CvMat outputImgDesc = CvMat.create(orginalImg.rows(),orginalImg.cols(),orginalImg.type());

        drawKeypoints(orginalImg,keyPoint,outputImgDesc, opencv_core.CvScalar.YELLOW, opencv_features2d.DrawMatchesFlags.DRAW_RICH_KEYPOINTS);
        // App temp dir
        String cbodTempDir = CBODUtil.getCbodTempDirectory();

        String path=cbodTempDir.concat("/")
                .concat(imageModel.getRawImageName())
                .concat("_key_points" + CBODConstants.JPEG_SUFFIX);

        cvSaveImage(path,outputImgDesc);

    }

}
