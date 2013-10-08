package net.bsrc.cbod.core.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.bsrc.cbod.core.CBODConstants;
import net.bsrc.cbod.core.exception.CBODException;

import org.apache.commons.io.FileUtils;

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

	public static String getDefaultOutputDirectoryPath() {
		return getDefaultOutputDirectory().getAbsolutePath();
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

	/**
	 * get all files that place in the given directory path
	 * 
	 * @param dirPath
	 * @return
	 */
	public static List<String> getFileList(String dirPath) {

		File dir = FileUtils.getFile(dirPath);

		if (!dir.exists()) {
			throw new CBODException(
					"Directory cannot be found: ".concat(dirPath));
		}

		if (!dir.isDirectory()) {
			throw new CBODException("File is not a directory");
		}

		List<String> fileNameList = new ArrayList<String>();

		File[] files = dir.listFiles();

		for (File file : files) {
			fileNameList.add(file.getAbsolutePath());
		}

		return fileNameList;
	}
}
