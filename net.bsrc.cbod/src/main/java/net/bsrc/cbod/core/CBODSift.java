package net.bsrc.cbod.core;

import static com.googlecode.javacv.cpp.opencv_core.CV_TERMCRIT_ITER;
import static com.googlecode.javacv.cpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImageM;

import java.util.ArrayList;
import java.util.List;

import net.bsrc.cbod.core.model.ImageModel;
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

	
	public static CvMat createDictionary(List<ImageModel> imageModelList,int dictionarySize){
		
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
			//Descriptor is not null
			if (!descriptors.isNull()) {
				cvMatList.add(descriptors);
			}
		}
		
		CvMat totalDescriptors = OpenCV.concatenateDescriptors(cvMatList, 0);
		// KMEANS_PP_CENTER
		int flags = 2;

		CvTermCriteria termCriteria = new CvTermCriteria(CV_TERMCRIT_ITER, 100,
				0.001);
		BOWKMeansTrainer bowKMeansTrainer = new BOWKMeansTrainer(
				dictionarySize, termCriteria, 1, flags);

		dictionary = bowKMeansTrainer.cluster(totalDescriptors);
		
		return dictionary;
		
	}
	
	
	
	public static CvMat extractSIFTDescriptor(ImageModel imgModel,CvMat dictionary){
		
		SIFT sift = new SIFT();
		DescriptorExtractor extractor = sift.getDescriptorExtractor();
		FeatureDetector detector = sift.getFeatureDetector();
		
		DescriptorMatcher descMatcher = new FlannBasedMatcher();

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
		bowDE.compute(imageMat, keyPoints, outputImgDesc, intVectorVector,
				temp);
		
		return outputImgDesc;
	}
	
	
}
