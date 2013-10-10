package net.bsrc.cbod.opencv;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.bsrc.cbod.core.model.RegionMap;
import net.bsrc.cbod.core.RegionMapFactory;
import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.pascal.xml.PascalBndBox;

import org.apache.commons.io.FileUtils;
import org.opencv.core.*;
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
				new Point(box.getXmin(), box.getYmax() - 1),
				new Point(box.getXmax() - 1, box.getYmin()),
				new Point(box.getXmax() - 1, box.getYmax() - 1) };
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

		File file = FileUtils.getFile(imgPath);
		File parent = file.getParentFile();
		if (!parent.exists()) {
			CBODUtil.createDirectory(parent);
		}

		Highgui.imwrite(imgPath, m);
	}

	/**
	 * 
	 * @param imageName
	 *            name of the orginal image
	 * @param mapName
	 *            name of the orginal image's map file
	 * @return
	 */
	public static List<Mat> getSegmentedRegions(String imageName,
			String mapName, boolean isBlackBg) {

		Mat org = getImageMat(imageName);
		RegionMap regionMap = RegionMapFactory.getRegionMap(imageName, mapName);

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
}
