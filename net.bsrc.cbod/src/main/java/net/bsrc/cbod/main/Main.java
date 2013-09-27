package net.bsrc.cbod.main;

import java.util.List;

import net.bsrc.cbod.opencv.OpenCV;
import net.bsrc.cbod.pascal.EPascalType;
import net.bsrc.cbod.pascal.PascalVOC;
import net.bsrc.cbod.pascal.xml.PascalAnnotation;
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

		PascalVOC pascal = PascalVOC.getInstance();

		List<String> imgNames = pascal.getImageNames(EPascalType.CAR, 0);
		// List<String> filePaths = pascal.getImagePaths(EPascalType.CAR, 0);

		// String xml = pascal.getAnnotationXML("2008_000028");

		// PascalAnnotation ann=PascalXMLHelper.fromXML(xml);
		// System.out.println(ann);

		for (String imgName : imgNames) {
			PascalAnnotation ann = pascal.getAnnotation(imgName);
			for (PascalObject po : ann.getObjectList(EPascalType.CAR)) {
				System.out.println(po.getPose());
			}

		}

		/**
		 * for (int i = 0; i < 10; i++) { String imgName = imgNames.get(i);
		 * String imgPath = pascal.getImagePath(imgName); PascalAnnotation ann =
		 * PascalXMLHelper.fromXML(pascal .getAnnotationXML(imgName)); for (int
		 * j = 0; j < ann.getObjectList(EPascalType.CAR).size(); j++) {
		 * 
		 * PascalObject po = ann.getObjectList(EPascalType.CAR).get(j); Mat crop
		 * = OpenCV.getImageMat(imgPath, po.getBndbox());
		 * OpenCV.writeImage(crop, "/Users/bsr/Desktop/" + imgName + "_" + j +
		 * ".jpg"); }
		 * 
		 * }
		 **/

	}

}
