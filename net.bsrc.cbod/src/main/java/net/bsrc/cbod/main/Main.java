package net.bsrc.cbod.main;

import java.util.List;

import net.bsrc.cbod.pascal.EPascalType;
import net.bsrc.cbod.pascal.PascalVOC;
import net.bsrc.cbod.util.ConfigurationUtil;

import org.apache.commons.configuration.ConfigurationException;
import org.opencv.core.Core;

public class Main {

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args) throws ConfigurationException {
		
		List<String> fileNames=PascalVOC.getInstance().getImageNames(EPascalType.CAR);
		List<String> filePaths = PascalVOC.getInstance().getImagePaths(EPascalType.CAR);
		
		
		
		
		System.out.println(filePaths);
		
	}

}
