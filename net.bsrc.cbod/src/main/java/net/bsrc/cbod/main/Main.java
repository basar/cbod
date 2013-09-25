package net.bsrc.cbod.main;

import java.util.List;

import net.bsrc.cbod.jseg.JSEG;
import net.bsrc.cbod.pascal.EPascalType;
import net.bsrc.cbod.pascal.PascalVOC;

import org.apache.commons.configuration.ConfigurationException;
import org.opencv.core.Core;

public class Main {

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args) throws ConfigurationException {

		PascalVOC pascal = PascalVOC.getInstance();

		List<String> fileNames = pascal.getImageNames(EPascalType.CAR, 0);
		// List<String> filePaths = pascal.getImagePaths(EPascalType.CAR, 0);

		// String xml = pascal.getAnnotationXML("2008_000028");

		// PascalAnnotation ann=PascalXMLHelper.fromXML(xml);
		// System.out.println(ann);

		for (int i = 0; i < 10; i++) {
			String imgName = fileNames.get(i);
			String imgPath = pascal.getImagePath(imgName);
			JSEG.getInstance().executeWithDefaultParams(imgName, imgPath);
		}

	}

}
