package net.bsrc.cbod.opencv;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.bsrc.cbod.core.RegionMapFactory;
import net.bsrc.cbod.core.model.CandidateComponent;
import net.bsrc.cbod.core.model.EObjectType;
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
 * @author bsr
 */
public final class OpenCV {

    private static final Logger logger = LoggerFactory.getLogger(OpenCV.class);

    private OpenCV() {

    }

    /**
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
     * @param imgPath
     * @param box
     * @return
     */
    public static Mat getImageMat(String imgPath, PascalBndBox box) {

        Mat result = null;
        Mat org = getImageMat(imgPath);

        Point[] arr = new Point[]{new Point(box.getXmin(), box.getYmin()),
                new Point(box.getXmin(), box.getYmax() - 1),
                new Point(box.getXmax() - 1, box.getYmin()),
                new Point(box.getXmax() - 1, box.getYmax() - 1)};
        try {
            Rect r = Imgproc.boundingRect(new MatOfPoint(arr));
            result = org.submat(r);

        } catch (CvException ex) {
            logger.error("", ex);
        }

        return result;
    }

    /**
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

    public static void drawComponentsToImage(List<CandidateComponent> candidateComponents, ImageModel imageModel, String outputSuffix) {


        Mat copy = OpenCV.copyImage(imageModel.getMat());
        Scalar blue = new Scalar(255, 0, 0);
        Scalar green = new Scalar(0, 255, 0);
        Scalar red = new Scalar(0, 0, 255);

        for (CandidateComponent cc : candidateComponents) {
            Rect rect = cc.getRect();
            if (cc.getObjectType().equals(EObjectType.WHEEL)) {
                OpenCV.drawRect(rect, copy, red);
            }
            if (cc.getObjectType().equals(EObjectType.TAIL_LIGHT)) {
                OpenCV.drawRect(rect, copy, green);
            }
            if (cc.getObjectType().equals(EObjectType.LICENSE_PLATE)) {
                OpenCV.drawRect(rect, copy, blue);
            }
        }

        String outputImagePath = CBODUtil.getCbodTempDirectory().concat("/").
                concat(imageModel.getRawImageName() + outputSuffix + "." + imageModel.getExtension());
        OpenCV.writeImage(copy, outputImagePath);


    }


    public static void drawComponentsToImage(List<CandidateComponent> candidateComponents, CandidateComponent pivot,
                                             ImageModel imageModel, String outputSuffix) {


        Mat copy = OpenCV.copyImage(imageModel.getMat());
        Scalar blue = new Scalar(255, 0, 0);
        Scalar green = new Scalar(0, 255, 0);
        Scalar red = new Scalar(0, 0, 255);
        Scalar yellow = new Scalar(0,255,255);

        for (CandidateComponent cc : candidateComponents) {
            Rect rect = cc.getRect();
            if (cc.getObjectType().equals(EObjectType.WHEEL)) {
                OpenCV.drawRect(rect, copy, red);
            }
            if (cc.getObjectType().equals(EObjectType.TAIL_LIGHT)) {
                OpenCV.drawRect(rect, copy, green);
            }
            if (cc.getObjectType().equals(EObjectType.LICENSE_PLATE)) {
                OpenCV.drawRect(rect, copy, blue);
            }
        }

        if(pivot!=null) {
            OpenCV.drawRect(pivot.getRect(), copy, yellow);
        }

        String outputImagePath = CBODUtil.getCbodTempDirectory().concat("/").
                concat(imageModel.getRawImageName() + outputSuffix + "." + imageModel.getExtension());
        OpenCV.writeImage(copy, outputImagePath);


    }

    public static void drawText(Mat m, Point p, String text) {
        Core.putText(m, text, p, Core.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 0));
    }

    public static Point getCenterPoint(Rect rect) {

        double centerX = rect.x + (rect.width / 2.0);
        double centerY = rect.y + (rect.height / 2.0);

        return new Point(centerX, centerY);

    }

    /**
     * @param imagePath   name of the orginal image
     * @param mapFilePath name of the orginal image's map file
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

            Rect rect = null;
            try {
                rect = Imgproc.boundingRect(new MatOfPoint(arr));
            } catch (Exception ex) {
                logger.error("", ex);
                continue;
            }

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

	/*
     * public static CvMat concatenateDescriptors(List<CvMat> descriptorsList,
	 * int index) { if (descriptorsList.size() == (index + 1)) { return
	 * descriptorsList.get(index); } else { CvMat head =
	 * descriptorsList.get(index); CvMat next =
	 * concatenateDescriptors(descriptorsList, index + 1); CvMat desc =
	 * cvCreateMat(head.rows() + next.rows(), head.cols(), next.type());
	 * desc.put(head); int offset = head.rows(); for (int i = offset; i <
	 * desc.rows(); i++) { for (int j = 0; j < desc.cols(); j++) { desc.put(i,
	 * j, next.get(i - offset, j)); } } return desc; } }
	 */

    public static CvMat concatenateDescriptors(List<CvMat> descriptorsList,
                                               int type) {

        CvMat concated = null;
        int maxCol = 0;
        int totalRow = 0;

        for (CvMat mat : descriptorsList) {
            totalRow = totalRow + mat.rows();
            if (maxCol == 0) {
                maxCol = mat.cols();
                continue;
            }
            if (maxCol < mat.cols())
                maxCol = mat.cols();
        }

        int rows = totalRow;
        int cols = maxCol;

        concated = CvMat.create(rows, cols, type);

        int globalRows = 0;
        for (CvMat mat : descriptorsList) {
            for (int i = 0; i < mat.rows(); i++) {
                for (int j = 0; j < mat.cols(); j++) {
                    concated.put(globalRows, j, mat.get(i, j));
                }
                globalRows++;
            }
        }

        return concated;
    }

    public static void storeCvMatToFile(String filePath, String varName,
                                        CvMat mat) {

        CvFileStorage fileStorage = cvOpenFileStorage(filePath, null,
                CV_STORAGE_WRITE, null);
        cvWrite(fileStorage, varName, mat);
        cvReleaseFileStorage(fileStorage);
    }

    public static CvMat loadCvMatFromFile(String filePath, String varName) {

        CvMat result = null;

        CvFileStorage fileStorage = cvOpenFileStorage(filePath, null,
                CV_STORAGE_READ, null);
        if (fileStorage == null) {
            logger.error("File could not be opened:{}", filePath);
            return null;
        }

        Pointer pointer = cvReadByName(fileStorage, null, varName);
        result = new CvMat(pointer);

        return result;
    }

    public static double getMinimumDistance(Rect r1, Rect r2) {

        double result = 0.0;
        if (intersect(r1, r2)) {
            return result;
        }

        Point o1 = getCenterPoint(r1);
        Point o2 = getCenterPoint(r2);

        if (o1.x == o2.x) {
            //r1:upper r2:lower  or r1:lower and r2:upper
            result = Math.abs(o2.y - o1.y) - ((r1.height / 2.0) + (r2.height / 2.0));
        }
        if (o1.y == o2.y) {
            //r1:left r2:right or r1:right r2:left
            result = Math.abs(o2.x - o1.x) - ((r1.width / 2.0) + (r2.width / 2.0));
        }

        //r1:left r2:right
        if (o1.x < o2.x) {
            //r1:upper r2:lower
            if (o1.y < o2.y) {

                Point p1_2 = getPoint(r1, 2);
                Point p2_0 = getPoint(r2, 0);

                if (p1_2.x <= p2_0.x && p1_2.y <= p2_0.y) {
                    result = distance(p1_2,p2_0);
                }
                if (p1_2.x > p2_0.x && p1_2.y < p2_0.y) {
                    result = p2_0.y - p1_2.y;
                }
                if (p1_2.x < p2_0.x && p1_2.y > p2_0.y) {
                    result = p2_0.x - p1_2.x;
                }
            }
            //r1:lower r2:upper
            if (o1.y > o2.y) {

                Point p1_1 = getPoint(r1, 1);
                Point p2_3 = getPoint(r2, 3);

                if (p1_1.x <= p2_3.x && p1_1.y >= p2_3.y) {
                    result=distance(p1_1,p2_3);
                }
                if (p1_1.x < p2_3.x && p1_1.y < p2_3.y) {
                    result = p2_3.x - p1_1.x;
                }
                if (p1_1.x > p2_3.x && p1_1.y > p2_3.y) {
                    result = p1_1.y - p2_3.y;
                }
            }
        }

        //r1:right r2:left
        if (o1.x > o2.x) {
            //r1:upper r2:lower
            if (o1.y < o2.y) {

                Point p1_3 = getPoint(r1, 3);
                Point p2_1 = getPoint(r2, 1);

                if (p1_3.x >= p2_1.x && p1_3.y <= p2_1.y) {
                    result=distance(p1_3,p2_1);
                }
                if (p1_3.x > p2_1.x && p1_3.y > p2_1.y) {
                    result = p1_3.x - p2_1.x;
                }
                if (p1_3.x < p2_1.x && p1_3.y < p2_1.y) {
                    result = p2_1.y - p1_3.y;
                }
            }
            //r1:lower r2:upper
            if (o1.y > o2.y) {

                Point p1_0 = getPoint(r1, 0);
                Point p2_2 = getPoint(r2, 2);

                if (p1_0.x >= p2_2.x && p1_0.y >= p2_2.y) {
                    result = distance(p1_0,p2_2);
                }
                if (p1_0.x > p2_2.x && p1_0.y < p2_2.y) {
                    result = p1_0.x - p2_2.x;
                }

                if (p1_0.x < p2_2.x && p1_0.y > p2_2.y) {
                    result = p1_0.y - p2_2.y;
                }

            }

        }

        return result;
    }

    public static double distance(Point p1,Point p2){
        return Math.sqrt(Math.pow(Math.abs(p1.x-p2.x),2) + Math.pow(Math.abs(p1.y-p2.y),2));
    }

    public static boolean intersect(Rect r1, Rect r2) {

        Point[] points1 = getCornerPoints(r1);
        Point[] points2 = getCornerPoints(r2);

        for (int i = 0; i < points1.length; i++) {
            if (r2.contains(points1[i])) {
                return true;
            }
        }

        for (int i = 0; i < points2.length; i++) {
            if (r1.contains(points2[i])) {
                return true;
            }
        }

        return false;
    }

    public static Rect createRect(int x,int y,int width,int height){
        Rect r = new Rect();
        r.x = x;
        r.y = y;
        r.width = width;
        r.height = height;
        return r;
    }


    /**
     * 0------1
     * |      |
     * |      |
     * 3------2
     */
    public static Point[] getCornerPoints(Rect rect) {
        Point[] arr = new Point[]{new Point(rect.x, rect.y),
                new Point(rect.x + rect.width, rect.y),
                new Point(rect.x + rect.width, rect.y + rect.height),
                new Point(rect.x, rect.y + rect.height)};
        return arr;
    }

    /**
     * 0------1
     * |      |
     * |      |
     * 3------2
     */
    public static double getCornerX(Rect r, int index) {
        return getCornerPoints(r)[index].x;
    }

    /**
     * 0------1
     * |      |
     * |      |
     * 3------2
     */
    public static double getCornerY(Rect r, int index) {
        return getCornerPoints(r)[index].y;
    }


    public static Point getPoint(Rect r, int index) {
        return getCornerPoints(r)[index];
    }

    /**
     *
     * @param p1 source
     * @param p2 target
     * @return
     */
    public static double getAngle(Point p1,Point p2){

        double angle = Math.toDegrees(Math.atan2((p2.y - p1.y),(p2.x - p1.x)));


        if(p2.y < p1.y){
            angle = angle * (-1);
        }else {
            angle = 360 - angle;
        }

        return angle;
    }


    /**
     *
     * @param r1 source
     * @param r2 target
     * @return
     */
    public static double getAngle(Rect r1,Rect r2){

        return getAngle(getCenterPoint(r1),getCenterPoint(r2));
    }


}
