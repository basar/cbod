package net.bsrc.cbod.pascal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.bsrc.cbod.core.CBODConstants;
import net.bsrc.cbod.core.exception.CBODException;
import net.bsrc.cbod.core.util.ConfigurationUtil;
import net.bsrc.cbod.pascal.xml.PascalAnnotation;
import net.bsrc.cbod.pascal.xml.PascalXMLHelper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Pascal Visual Object Classes
 * 
 * @author bsr
 * 
 */
public class PascalVOC {

	private static PascalVOC instance = null;

	private String mainDir;

	private String imageDir;

	private String annotationDir;

	private String indexDir;

	private String segmentationClassDir;

	private String segmentationObjectDir;

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
		mainDir = ConfigurationUtil
				.getString(CBODConstants.PASCAL_MAIN_DIR_KEY);

		imageDir = mainDir.concat(ConfigurationUtil
				.getString(CBODConstants.PASCAL_IMAGE_DIR_KEY));
		annotationDir = mainDir.concat(ConfigurationUtil
				.getString(CBODConstants.PASCAL_ANNOTATION_DIR_KEY));
		indexDir = mainDir.concat(ConfigurationUtil
				.getString(CBODConstants.PASCAL_INDEX_DIR_KEY));
		segmentationClassDir = mainDir.concat(ConfigurationUtil
				.getString(CBODConstants.PASCAL_SEGMENTATION_CLASS_DIR_KEY));
		segmentationObjectDir = mainDir.concat(ConfigurationUtil
				.getString(CBODConstants.PASCAL_SEGMENTATION_OBJECT_DIR_KEY));

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

	public String getSegmentationClassDir() {
		return segmentationClassDir;
	}

	public String getSegmentationObjectDir() {
		return segmentationObjectDir;
	}

	/**
	 * Returns image names that belong to given pascal type
	 * 
	 * @param type
	 * @param suffix
	 *            0 train, 1 val, 2 both
	 * @return
	 */
	public List<String> getImageNames(EPascalType type, int suffix) {

		List<String> resultList = new ArrayList<String>();
		String filePath = indexDir.concat("/").concat(type.getName())
				.concat(getTrainValSuffix(suffix));
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
			// TODO logger yazilacak!
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
	public List<String> getImagePaths(EPascalType type, int suffix) {

		List<String> resultList = new ArrayList<String>();

		List<String> imageNames = getImageNames(type, suffix);

		for (String imageName : imageNames) {
			String imagePath = imageDir.concat("/").concat(imageName)
					.concat(CBODConstants.JPEG_SUFFIX);
			resultList.add(imagePath);
		}

		return resultList;

	}

	/**
	 * Returns full pascal image path
	 * 
	 * @param imageName
	 * @return
	 */
	public String getImagePath(String imageName) {
		return imageDir.concat("/").concat(imageName)
				.concat(CBODConstants.JPEG_SUFFIX);
	}

	/**
	 * Returns annotaion xml data
	 * 
	 * @param imageName
	 * @return
	 */
	public String getAnnotationXML(String imageName) {

		String xml = null;

		File xmlFile = FileUtils.getFile(annotationDir.concat("/")
				.concat(imageName).concat(CBODConstants.XML_SUFFIX));
		try {
			xml = FileUtils.readFileToString(xmlFile);
		} catch (IOException e) {
			// TODO Logger yazilacak!
			e.printStackTrace();
		}

		return xml;

	}

	/**
	 * 
	 * @param imageName
	 * @return
	 */
	public PascalAnnotation getAnnotation(String imageName) {

		String xml = getAnnotationXML(imageName);
		return PascalXMLHelper.fromXML(xml);

	}

	/**
	 * 0 train, 1 val, 2 both
	 * 
	 * @param suffix
	 * @return
	 */
	private String getTrainValSuffix(int suffix) {
		String result = null;
		switch (suffix) {
		case 0:
			result = PascalConstants.TRAIN_SUFFIX;
			break;
		case 1:
			result = PascalConstants.VAL_SUFFIX;
			break;
		case 2:
			result = PascalConstants.TRAIN_VAL_SUFFIX;
			break;
		default:
			// TODO uyari verilecek!
			break;
		}
		return result;
	}

}
