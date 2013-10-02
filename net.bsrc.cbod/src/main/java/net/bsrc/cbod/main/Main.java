package net.bsrc.cbod.main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.bsrc.cbod.core.CBODConstants;
import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.jseg.JSEG;
import net.bsrc.cbod.jseg.JSEGParameter;
import net.bsrc.cbod.opencv.OpenCV;
import net.bsrc.cbod.pascal.EPascalType;
import net.bsrc.cbod.pascal.PascalVOC;
import net.bsrc.cbod.pascal.xml.PascalAnnotation;
import net.bsrc.cbod.pascal.xml.PascalBndBox;
import net.bsrc.cbod.pascal.xml.PascalObject;
import net.bsrc.cbod.pascal.xml.PascalXMLHelper;

import org.apache.commons.io.FileUtils;
import org.opencv.core.Core;
import org.opencv.core.Mat;

public class Main {

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args) {

		cropVehiclesFromImageDataSetAndSegmentThem();
	}

	/**
     *
     */
	public static void cropVehiclesFromImageDataSetAndSegmentThem() {

		String outputDir = CBODUtil.getDefaultOutputDirectory()
				.getAbsolutePath();
		String vehicleOutputDir = outputDir.concat("/Vehicle");
		PascalVOC pascal = PascalVOC.getInstance();

		List<String> imgNames = pascal.getImageNames(EPascalType.CAR, 2);
		List<String> segmentedImageNames = new ArrayList<String>();

		final String jpg = CBODConstants.JPEG_SUFFIX;
		final String seg = CBODConstants.SEG_SUFFIX;
		final String map = CBODConstants.MAP_SUFFIX;

		for (int i = 0; i < imgNames.size(); i++) {

			String imgName = imgNames.get(i);
			String imgPath = pascal.getImagePath(imgName);

			PascalAnnotation ann = PascalXMLHelper.fromXML(pascal
					.getAnnotationXML(imgName));

			List<PascalObject> objectList = getTrainedPascalObjectList(ann);

			for (int j = 0; j < objectList.size(); j++) {

				PascalObject po = objectList.get(j);

				Mat crop = OpenCV.getImageMat(imgPath, po.getBndbox());

				String outputImgPath = vehicleOutputDir
						.concat("/" + po.getPose() + "/")
						.concat(imgName + "_" + j).concat(jpg);
				OpenCV.writeImage(crop, outputImgPath);
				segmentedImageNames.add(outputImgPath);
			}
		}

		for (int i = 0; i < segmentedImageNames.size(); i++) {

			String imageName = segmentedImageNames.get(i);
			File imageFile = FileUtils.getFile(imageName);
			File parentDir = imageFile.getParentFile();

			String imageRawName = imageFile.getName().replaceAll(jpg, "");
			String directoryName = parentDir.getAbsolutePath().concat("/")
					.concat(imageRawName + seg);

			CBODUtil.createDirectory(directoryName);

			// JSEG Parameter
			JSEGParameter jsegParam = new JSEGParameter(imageName);
			jsegParam.setRegionMapFileName(directoryName.concat("/").concat(
					imageRawName + map));
			jsegParam.setOutputFileImage(directoryName.concat("/")
					.concat(imageRawName).concat(seg + jpg));

			String mapName = jsegParam.getRegionMapFileName();
			JSEG.getInstance().execute(jsegParam);

			List<Mat> regions = OpenCV.getSegmentedRegions(imageName, mapName,
					true);

			for (int r = 0; r < regions.size(); r++) {

				Mat region = regions.get(r);
				String regionName = directoryName.concat("/")
						.concat(imageRawName).concat(seg + r + jpg);
				// Save region file to disk
				OpenCV.writeImage(region, regionName);
			}

		}
	}

	public static List<PascalObject> getTrainedPascalObjectList(
			PascalAnnotation ann) {

		List<PascalObject> result = new ArrayList<PascalObject>();

		List<PascalObject> list = ann.getObjectList(EPascalType.CAR);
		for (PascalObject po : list) {
			if (!po.isDifficult() && !po.isTruncated() && !po.isOccluded()) {
				// Size hesaplamasi
				PascalBndBox box = po.getBndbox();
				int width = box.getXmax() - box.getXmin();
				int height = box.getYmax() - box.getYmin();
				if (width >= JSEG.MIN_IMG_WIDTH
						&& height >= JSEG.MIN_IMG_HEIGHT) {
					result.add(po);
				}
			}
		}

		return result;
	}

}
