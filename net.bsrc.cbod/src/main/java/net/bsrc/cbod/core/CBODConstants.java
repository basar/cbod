package net.bsrc.cbod.core;

/**
 * Constants for project
 * 
 * @author bsr
 * 
 */
public final class CBODConstants {

	/**
	 * Command project constants
	 */
	public static final String CONFIG_BASE = "META-INF/config/";
	public static final String PROJECT_PROPERTIES = "project.properties";
	public static final String TARGET_ENV_KEY = "target.env";
	public static final String PROJECT_TARGET_ENV_PROPERTIES = "project.${"
			+ TARGET_ENV_KEY + "}.properties";

	public static final String JPEG_SUFFIX = ".jpg";
	public static final String XML_SUFFIX = ".xml";
	public static final String MAP_SUFFIX = ".map";
	public static final String SEG_SUFFIX = ".seg";

	/**
	 * Property files keys
	 */

	/**
	 * CBOD Core
	 */
	public static final String CBOD_OUTPUT_DIR = "net.bsrc.cbod.outputDir";

	/**
	 * Pascal VOC
	 */
	public static final String PASCAL_MAIN_DIR_KEY = "net.bsrc.cbod.pascal.mainDir";
	public static final String PASCAL_IMAGE_DIR_KEY = "net.bsrc.cbod.pascal.imageDir";
	public static final String PASCAL_ANNOTATION_DIR_KEY = "net.bsrc.cbod.pascal.annotationDir";
	public static final String PASCAL_INDEX_DIR_KEY = "net.bsrc.cbod.pascal.indexDir";
	public static final String PASCAL_SEGMENTATION_CLASS_DIR_KEY = "net.bsrc.cbod.pascal.segmentationClassDir";
	public static final String PASCAL_SEGMENTATION_OBJECT_DIR_KEY = "net.bsrc.cbod.pascal.segmentationObjectDir";

	/**
	 * JSEG
	 */
	public static final String JSEG_EXECUTE_COMMAND_KEY = "net.bsrc.cbod.jseg.executeCommand";

	/**
	 * To prevent object creation
	 */
	private CBODConstants() {

	}

}
