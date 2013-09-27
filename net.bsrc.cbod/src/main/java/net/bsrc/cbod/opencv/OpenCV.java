package net.bsrc.cbod.opencv;

import net.bsrc.cbod.pascal.xml.PascalBndBox;

import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

/**
 * 
 * @author bsr
 * 
 */
public final class OpenCV {

	private OpenCV() {

	}

	/**
	 * 
	 * @param imgPath
	 * @return
	 */
	public static Mat getImageMat(String imgPath) {
		Mat mat = Highgui.imread(imgPath);
		return mat;
	}

	/**
	 * 
	 * @param imgPath
	 * @param box
	 * @return
	 */
	public static Mat getImageMat(String imgPath, PascalBndBox box) {

		Mat result = null;
		Mat org = getImageMat(imgPath);

		Point[] arr = new Point[] { new Point(box.getXmin(), box.getYmin()),
				new Point(box.getXmin(), box.getYmax()-1),
				new Point(box.getXmax()-1, box.getYmin()),
				new Point(box.getXmax()-1, box.getYmax()-1) };
		try {
		
			Rect r = Imgproc.boundingRect(new MatOfPoint(arr));
			result = org.submat(r);
		
		} catch (CvException ex) {
			System.out.println(ex);
		}

		return result;
	}

	/**
	 * 
	 * @param m
	 * @param imgPath
	 */
	public static void writeImage(Mat m, String imgPath) {
		Highgui.imwrite(imgPath, m);
	}
}
