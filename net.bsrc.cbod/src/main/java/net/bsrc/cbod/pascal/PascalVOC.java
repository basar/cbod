package net.bsrc.cbod.pascal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.bsrc.cbod.core.CBODConstants;
import net.bsrc.cbod.core.exception.CBODException;
import net.bsrc.cbod.core.model.ImageModel;
import net.bsrc.cbod.core.util.ConfigurationUtil;
import net.bsrc.cbod.pascal.xml.PascalAnnotation;
import net.bsrc.cbod.pascal.xml.PascalXMLHelper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pascal Visual Object Classes
 * 
 * @author bsr
 * 
 */
public class PascalVOC {

	private final static Logger logger = LoggerFactory
			.getLogger(PascalVOC.class);

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
	 *            pascal type
	 * @param suffix
	 *            0 train, 1 val, 2 both
	 * @param label
	 *            -1 negative images, 1 positive images, 0 difficult
	 * @return
	 */
	public List<ImageModel> getImageModels(EPascalType type, int suffix,
			int label) {

		List<ImageModel> resultList = new ArrayList<ImageModel>();
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
				// specifier determines the types of an image in terms of
				// negative,positive or dificult
				String specifier = arr[1];

				if (StringUtils.isNotEmpty(specifier)
						&& specifier.equals(Integer.toString(label))) {

					ImageModel imgModel = new ImageModel();
					imgModel.setImageName(arr[0]
							.concat(CBODConstants.JPEG_SUFFIX));
					imgModel.setImagePath(getImagePath(arr[0]));
					resultList.add(imgModel);
				}
			}

		} catch (IOException e) {
			throw new CBODException(e);
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
			throw new CBODException(e);
		}

		return xml;

	}

	/**
	 * 
	 * @param rawImageName
	 * @return
	 */
	public PascalAnnotation getAnnotation(String rawImageName) {

		String xml = getAnnotationXML(rawImageName);
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
			logger.error("Invalid suffix parameter");
			break;
		}
		return result;
	}

}
