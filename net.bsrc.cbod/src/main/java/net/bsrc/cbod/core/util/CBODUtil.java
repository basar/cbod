package net.bsrc.cbod.core.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.bsrc.cbod.core.CBODConstants;
import net.bsrc.cbod.core.exception.CBODException;
import net.bsrc.cbod.core.model.ImageModel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
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

	public static byte[] getFileData(String filePath) {

		Validate.notEmpty(filePath);
		File file = FileUtils.getFile(filePath);

		if (!file.exists())
			throw new CBODException("File could not be found. Path: "
					+ filePath);

		byte[] result = null;

		try {
			result = FileUtils.readFileToByteArray(file);
		} catch (IOException e) {
			logger.error("", e);
			result = null;
		}

		return result;
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

	public static String getCbodTempDirectory() {

		String tmp = ConfigurationUtil.getString(CBODConstants.CBOD_TEMP_DIR);
		return getDefaultOutputDirectoryPath().concat(tmp);
	}

	public static String getCbodInputImageDirectory() {
		String tmp = ConfigurationUtil
				.getString(CBODConstants.CBOD_INPUT_IMAGE_DIR);
		return getDefaultOutputDirectoryPath().concat(tmp);
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

	public static void compareTwoImageModelCollection(List<ImageModel> models1,
			List<ImageModel> models2) {

		for (ImageModel model1 : models1) {
			for (ImageModel model2 : models2) {
				if (model1.getImageName().equals(model2.getImageName())) {
					throw new CBODException(
							"The collections cotain equals image model elements!");
				}
			}
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

	/**
	 * 
	 * @param sb
	 * @param key
	 * @param values
	 */
	public static void appendParam(StringBuilder sb, String key, Object[] values) {
		appendParam(sb, " ", key, values);
	}

	/**
	 * 
	 * @param sb
	 * @param delimiter
	 * @param key
	 * @param values
	 */
	public static void appendParam(StringBuilder sb, String delimiter,
			String key, Object[] values) {

		appendParamCommon(sb, delimiter, key, values);
	}

	private static void appendParamCommon(StringBuilder sb, String delimiter,
			String key, Object[] values) {

		if (!isAllParamsNull(values)) {
			sb.append(key).append(delimiter);
			for (Object o : values) {
				if (o != null)
					sb.append(o).append(" ");
			}

		}
	}

	/**
	 * 
	 * @param objects
	 * @return
	 */
	public static boolean isAllParamsNull(Object... objects) {

		boolean result = true;

		if (!ArrayUtils.isEmpty(objects)) {
			for (Object object : objects) {
				if (object != null) {
					result = false;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * 
	 * @param relativeDir
	 * @return
	 */
	public static List<ImageModel> getImageModelList(String relativeDir) {

		List<ImageModel> resultList = new ArrayList<ImageModel>();

		String defaultOutputDir = getDefaultOutputDirectoryPath();
		String imageDirPath = defaultOutputDir.concat(relativeDir);

		List<String> imgPathList = getFileList(imageDirPath,
				CBODConstants.JPEG_SUFFIX);
		for (String imgPath : imgPathList) {

			ImageModel imgModel = new ImageModel();
			imgModel.setImagePath(imgPath);
			imgModel.setImageName(FilenameUtils.getName(imgPath));

			resultList.add(imgModel);
		}

		return resultList;
	}

	/**
	 * 
	 * @param relativeDir
	 * @param testProportion
	 * @return
	 */
	public static List<List<ImageModel>> seperateImageModelList(
			String relativeDir, int testProportion) {

		List<ImageModel> trainImageModelList = new ArrayList<ImageModel>();
		List<ImageModel> testImageModelList = new ArrayList<ImageModel>();

		List<ImageModel> imageModelList = getImageModelList(relativeDir);

		int k = 0;
		for (ImageModel imageModel : imageModelList) {
			if ((k++) % testProportion == 0) {
				testImageModelList.add(imageModel);
			} else {
				trainImageModelList.add(imageModel);
			}
		}

		List<List<ImageModel>> result = new ArrayList<List<ImageModel>>();
		result.add(trainImageModelList);
		result.add(testImageModelList);

		return result;

	}

}
