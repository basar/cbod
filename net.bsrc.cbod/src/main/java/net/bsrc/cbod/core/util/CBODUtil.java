package net.bsrc.cbod.core.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.bsrc.cbod.core.CBODConstants;
import net.bsrc.cbod.core.exception.CBODException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util operations for
 * 
 * @author bsr
 * 
 */
public final class CBODUtil {

	private final static Logger logger = LoggerFactory
			.getLogger(CBODUtil.class);

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
	public static List<String> getFileList(final String dirPath,
			final String suffix) {

		File dir = FileUtils.getFile(dirPath);

		if (!dir.exists()) {
			throw new CBODException(
					"Directory cannot be found: ".concat(dirPath));
		}

		if (!dir.isDirectory()) {
			throw new CBODException("File is not a directory");
		}

		List<String> fileNameList = new ArrayList<String>();

		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File file, String s) {

				if (!StringUtils.isEmpty(suffix))
					return s.endsWith(suffix);
				return true;
			}
		});

		for (File file : files) {
			fileNameList.add(file.getAbsolutePath());
		}

		return fileNameList;
	}

	public static String getFileName(String fileFullPath) {
		File file = new File(fileFullPath);
		return file.getName();
	}

}
