package net.bsrc.cbod.core;

import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvResize;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.javacpp.FloatPointer;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect.HOGDescriptor;

import net.bsrc.cbod.core.model.ImageModel;

public class CBODHog {

	public static List<Double> extractHogDescriptor(ImageModel imageModel) {

		List<Double> list = new ArrayList<Double>();

		IplImage rawImg = cvLoadImage(imageModel.getImagePath());

		// resize image
		IplImage resizedImg = IplImage.create(64, 64, rawImg.depth(),
				rawImg.nChannels());
		cvResize(rawImg, resizedImg);
		// gray image
		IplImage gray = IplImage.create(resizedImg.cvSize(),
				resizedImg.depth(), 1);
		cvCvtColor(resizedImg, gray, CV_BGR2GRAY);

		CvSize winSize = new CvSize(64, 64);
		CvSize winStride = new CvSize(8, 8);
		CvSize padding = new CvSize(0, 0);
		CvPoint locations = new CvPoint();
		FloatPointer featureVector = new FloatPointer();

		HOGDescriptor hog = new HOGDescriptor();
		hog.winSize(winSize);
		hog.compute(gray, featureVector, winStride, padding, locations);

		for (int i = 0; i < featureVector.capacity(); i++) {
			list.add(new Double(featureVector.get(i)));
		}

		return list;

	}

}
