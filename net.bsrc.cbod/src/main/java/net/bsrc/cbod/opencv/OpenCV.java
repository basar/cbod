package net.bsrc.cbod.opencv;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.bsrc.cbod.core.RegionMapFactory;
import net.bsrc.cbod.core.model.ImageModel;
import net.bsrc.cbod.core.model.RegionMap;
import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.pascal.xml.PascalBndBox;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.javacpp.Pointer;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvMat;

import static com.googlecode.javacv.cpp.opencv_core.*;


/**
 * 
 * @author bsr
 * 
 */
public final class OpenCV {

	private static final Logger logger = LoggerFactory.getLogger(OpenCV.class);

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

	public static Mat getImageMatAsGrayScale(String imgPath) {
		Mat mat = Highgui.imread(imgPath, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		return mat;
	}

	/**
	 * 
	 * @param imageModel
	 * @return
	 */
	public static Mat getImageMat(ImageModel imageModel) {
		Validate.notNull(imageModel, "Imagemodel must not be null");
		return getImageMat(imageModel.getImagePath());
	}

	public static Mat getImageMatAsGrayScale(ImageModel imageModel) {
		Validate.notNull(imageModel, "Imagemodel must not be null");
		return getImageMatAsGrayScale(imageModel.getImagePath());
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
				new Point(box.getXmin(), box.getYmax() - 1),
				new Point(box.getXmax() - 1, box.getYmin()),
				new Point(box.getXmax() - 1, box.getYmax() - 1) };
		try {
			Rect r = Imgproc.boundingRect(new MatOfPoint(arr));
			result = org.submat(r);

		} catch (CvException ex) {
			logger.error("",ex);
		}

		return result;
	}

	/**
	 * 
	 * @param m
	 * @param imgPath
	 */
	public static void writeImage(Mat m, String imgPath) {

		File file = FileUtils.getFile(imgPath);
		File parent = file.getParentFile();
		if (!parent.exists()) {
			CBODUtil.createDirectory(parent);
		}

		Highgui.imwrite(imgPath, m);
	}

	public static void drawRect(Rect rect, Mat mat) {
		drawRect(rect, mat, null);
	}

	public static void drawRect(Rect rect, Mat mat, Scalar scalar) {

		Point p1 = new Point(rect.x, rect.y);
		Point p2 = new Point(rect.x + rect.width, rect.y);
		Point p3 = new Point(rect.x + rect.width, rect.y + rect.height);
		Point p4 = new Point(rect.x, rect.y + rect.height);

		if (scalar == null)
			scalar = new Scalar(0, 255, 0);
		Core.line(mat, p1, p2, scalar, 2);
		Core.line(mat, p2, p3, scalar, 2);
		Core.line(mat, p3, p4, scalar, 2);
		Core.line(mat, p4, p1, scalar, 2);
	}

	/**
	 * 
	 * @param imagePath
	 *            name of the orginal image
	 * @param mapFilePath
	 *            name of the orginal image's map file
	 * @return
	 */
	public static List<Mat> getSegmentedRegions(String imagePath,
			String mapFilePath, boolean isBlackBg) {

		Mat org = getImageMat(imagePath);
		RegionMap regionMap = RegionMapFactory.getRegionMap(imagePath,
				mapFilePath);

		List<Mat> result = new ArrayList<Mat>();

		Mat map = regionMap.getMap();

		for (Integer label : regionMap.getLabels()) {

			List<Point> points = new ArrayList<Point>();

			for (int i = 0; i < map.rows(); i++) {
				for (int j = 0; j < map.cols(); j++) {

					double[] temp = map.get(i, j);
					if (temp[0] == label) {
						// Warning! col=x=j , row=y=i
						points.add(new Point(j, i));
					}
				}
			}

			Point[] arr = points.toArray(new Point[points.size()]);
			Rect rect = Imgproc.boundingRect(new MatOfPoint(arr));

			Mat region;
			if (isBlackBg) {
				region = getImageWithBlackBg(org, points).submat(rect);
			} else {
				region = org.submat(rect);
			}
			result.add(region);
		}

		return result;
	}

	/**
	 * 
	 * @param imagePath
	 * @param mapFilePath
	 * @param isBlackBg
	 * @return
	 */
	public static List<ImageModel> getSegmentedRegionsAsImageModels(
			String imagePath, String mapFilePath, boolean isBlackBg) {

		Mat org = getImageMat(imagePath);
		RegionMap regionMap = RegionMapFactory.getRegionMap(imagePath,
				mapFilePath);

		List<ImageModel> result = new ArrayList<ImageModel>();

		Mat map = regionMap.getMap();

		for (Integer label : regionMap.getLabels()) {

			List<Point> points = new ArrayList<Point>();

			for (int i = 0; i < map.rows(); i++) {
				for (int j = 0; j < map.cols(); j++) {

					double[] temp = map.get(i, j);
					if (temp[0] == label) {
						// Warning! col=x=j , row=y=i
						points.add(new Point(j, i));
					}
				}
			}

			Point[] arr = points.toArray(new Point[points.size()]);
			Rect rect = Imgproc.boundingRect(new MatOfPoint(arr));

			Mat region;
			if (isBlackBg) {
				region = getImageWithBlackBg(org, points).submat(rect);
			} else {
				region = org.submat(rect);
			}

			ImageModel imgModel = new ImageModel();
			imgModel.setMat(region);
			imgModel.setRelativeToOrg(rect);

			result.add(imgModel);
		}

		return result;
	}

	public static Mat copyImage(Mat org) {
		Mat copy = new Mat();
		org.copyTo(copy);
		return copy;
	}

	/**
	 * Helper method
	 * 
	 * @param org
	 * @param list
	 * @return
	 */
	private static Mat getImageWithBlackBg(Mat org, List<Point> list) {

		Mat region = Mat.zeros(org.size(), org.type());

		for (Point p : list) {
			int row = (int) p.y;
			int col = (int) p.x;
			region.put(row, col, org.get(row, col));
		}

		return region;
	}

	public static CvMat concatenateDescriptors(List<CvMat> descriptorsList,
			int index) {
		if (descriptorsList.size() == (index + 1)) {
			return descriptorsList.get(index);
		} else {
			CvMat head = descriptorsList.get(index);
			CvMat next = concatenateDescriptors(descriptorsList, index + 1);
			CvMat desc = cvCreateMat(head.rows() + next.rows(),
					head.cols(), next.type());
			desc.put(head);
			int offset = head.rows();
			for (int i = offset; i < desc.rows(); i++) {
				for (int j = 0; j < desc.cols(); j++) {
					desc.put(i, j, next.get(i - offset, j));
				}
			}
			return desc;
		}
	}
	
	
	public static void storeCvMatToFile(String filePath,String varName,CvMat mat){
		
		CvFileStorage fileStorage=cvOpenFileStorage(filePath,null,CV_STORAGE_WRITE,null);
		cvWrite(fileStorage,varName,mat);
		cvReleaseFileStorage(fileStorage);
	}
	
	public static CvMat loadCvMatFromFile(String filePath,String varName){
		
		CvMat result = null;
		
		CvFileStorage fileStorage = cvOpenFileStorage(filePath,null,CV_STORAGE_READ,null);
		if(fileStorage==null){
			logger.error("File could not be opened:{}",filePath);
			return null;
		}
		
		Pointer pointer = cvReadByName(fileStorage, null, varName);
		result = new CvMat(pointer);

		return result;
	}
	


}
