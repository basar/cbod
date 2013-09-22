package net.bsrc.cbod.main;

import net.bsrc.cbod.util.ConfigurationUtil;

import org.apache.commons.configuration.ConfigurationException;
import org.opencv.core.Core;

public class Main {

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args) throws ConfigurationException {
		
		
		
		String s=ConfigurationUtil.getString("deneme2");
		String s1=ConfigurationUtil.getString("target.env");
		
		System.out.println(s);
	}

}
