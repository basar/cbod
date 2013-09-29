package net.bsrc.cbod.main;

import java.util.ArrayList;
import java.util.List;

import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.jseg.JSEG;
import net.bsrc.cbod.opencv.OpenCV;
import net.bsrc.cbod.pascal.EPascalType;
import net.bsrc.cbod.pascal.PascalConstants;
import net.bsrc.cbod.pascal.PascalVOC;
import net.bsrc.cbod.pascal.xml.PascalAnnotation;
import net.bsrc.cbod.pascal.xml.PascalBndBox;
import net.bsrc.cbod.pascal.xml.PascalObject;
import net.bsrc.cbod.pascal.xml.PascalXMLHelper;

import org.apache.commons.configuration.ConfigurationException;
import org.opencv.core.Core;
import org.opencv.core.Mat;

public class Main {

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args) throws ConfigurationException {

		String outputDir = CBODUtil.getDefaultOutputDirectory()
				.getAbsolutePath();
		PascalVOC pascal = PascalVOC.getInstance();

		List<String> imgNames = pascal.getImageNames(EPascalType.CAR, 2);

		for (int i = 0; i < imgNames.size(); i++) {

			String imgName = imgNames.get(i);
			String imgPath = pascal.getImagePath(imgName);

			PascalAnnotation ann = PascalXMLHelper.fromXML(pascal
					.getAnnotationXML(imgName));

			List<PascalObject> objectList = getTrainedPascalObjectList(ann);

			for (int j = 0; j < objectList.size(); j++) {

				PascalObject po = objectList.get(j);

				Mat crop = OpenCV.getImageMat(imgPath, po.getBndbox());

				String outputImgPath = outputDir
						.concat("/" + po.getPose() + "/")
						.concat(imgName + "_" + j).concat(".jpg");
				OpenCV.writeImage(crop, outputImgPath);

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
