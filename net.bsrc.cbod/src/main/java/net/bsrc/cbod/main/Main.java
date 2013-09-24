package net.bsrc.cbod.main;

import net.bsrc.cbod.pascal.PascalVOC;
import net.bsrc.cbod.pascal.xml.PascalAnnotation;
import net.bsrc.cbod.pascal.xml.PascalXMLHelper;

import org.apache.commons.configuration.ConfigurationException;
import org.opencv.core.Core;

public class Main {

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args) throws ConfigurationException {

		PascalVOC pascal = PascalVOC.getInstance();

		//List<String> fileNames = pascal.getImageNames(EPascalType.CAR, 0);
		//List<String> filePaths = pascal.getImagePaths(EPascalType.CAR, 0);
		
		String xml = pascal.getAnnotationXML("2008_000028");
		
		
		PascalAnnotation ann=PascalXMLHelper.fromXML(xml);
		
		System.out.println(ann);

	}

}
