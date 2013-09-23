package net.bsrc.cbod.pascal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.bsrc.cbod.core.exception.CBODException;
import net.bsrc.cbod.util.ConfigurationUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Pascal Visual Object Classes
 * 
 * @author bsr
 * 
 */
public class PascalVOC {

	private static final String TYPE_FILE_SUFFIX = "_trainval.txt";
	private static final String JPEG_SUFFIX = ".jpg";

	private static PascalVOC instance = null;

	private String mainDir;

	private String imageDir;

	private String annotationDir;

	private String indexDir;

	/**
	 * To prevent object creation
	 */
	private PascalVOC() {

	}

	public static PascalVOC getInstance() {

		if (instance == null) {
			synchronized (PascalVOC.class) {
				if (instance == null) {
					instance = new PascalVOC();
					instance.initialize();
				}
			}
		}

		return instance;
	}

	private void initialize() {
		mainDir = ConfigurationUtil.getString("net.bsrc.cbod.pascal.mainDir");

		imageDir = mainDir.concat(ConfigurationUtil
				.getString("net.bsrc.cbod.pascal.imageDir"));
		annotationDir = mainDir.concat(ConfigurationUtil
				.getString("net.bsrc.cbod.pascal.annotationDir"));
		indexDir = mainDir.concat(ConfigurationUtil
				.getString("net.bsrc.cbod.pascal.indexDir"));
	}

	public String getMainDir() {
		return mainDir;
	}

	public String getImageDir() {
		return imageDir;
	}

	public String getAnnotationDir() {
		return annotationDir;
	}

	public String getIndexDir() {
		return indexDir;
	}

	/**
	 * Returns image names that belong to given pascal type
	 * 
	 * @param type
	 * @return
	 */
	public List<String> getImageNames(EPascalType type) {

		List<String> resultList = new ArrayList<String>();
		String filePath = indexDir.concat("/").concat(type.getName())
				.concat(TYPE_FILE_SUFFIX);
		File file = FileUtils.getFile(filePath);

		try {

			List<String> lines = FileUtils.readLines(file);

			for (String line : lines) {
				String[] arr = StringUtils.split(line);

				if (arr.length < 2) {
					throw new CBODException(
							"Line does not suitable for parsing");
				}

				String specifier = arr[1];
				// Only positive images
				if (StringUtils.isNotEmpty(specifier) && specifier.equals("1")) {
					resultList.add(arr[0]);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return resultList;
	}

	/**
	 * Returns image paths that belong to given pascal type
	 * 
	 * @param type
	 * @return
	 */
	public List<String> getImagePaths(EPascalType type) {

		List<String> resultList = new ArrayList<String>();

		List<String> imageNames = getImageNames(type);

		for (String imageName : imageNames) {
			String imagePath = imageDir.concat("/").concat(imageName)
					.concat(JPEG_SUFFIX);
			resultList.add(imagePath);
		}

		return resultList;

	}
	
	/**
	 * Returns full pascal image path
	 * @param imageName
	 * @return
	 */
	public String getImagePath(String imageName) {
		return imageDir.concat("/").concat(imageName).concat(JPEG_SUFFIX);
	}

}
