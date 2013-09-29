package net.bsrc.cbod.core.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import net.bsrc.cbod.core.CBODConstants;
import net.bsrc.cbod.core.exception.CBODException;

/**
 * Util operations for
 * 
 * @author bsr
 * 
 */
public final class CBODUtil {

	private CBODUtil() {

	}

	public static File getDefaultOutputDirectory() {

		String outputDirPath = ConfigurationUtil
				.getString(CBODConstants.CBOD_OUTPUT_DIR);
		File file = FileUtils.getFile(outputDirPath);
		if (!file.exists())
			createDirectory(file);

		return file;
	}

	/**
	 * 
	 * @param dirPath
	 * @return
	 */
	public static void createDirectory(String dirPath) {

		File file = FileUtils.getFile(dirPath);
		try {
			if (!file.exists())
				FileUtils.forceMkdir(file);
		} catch (IOException e) {
			throw new CBODException(e);
		}
	}

	/**
	 * 
	 * @param file
	 */
	public static void createDirectory(File file) {

		try {
			FileUtils.forceMkdir(file);
		} catch (IOException e) {
			throw new CBODException(e);
		}

	}
}
