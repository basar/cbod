package net.bsrc.cbod.core;

/**
 * Constants for project
 * 
 * @author bsr
 * 
 */
public final class CBODConstants {

	/**
	 * Common project constants
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
	public static final String TXT_SUFFIX = ".txt";

    public static final String MC_OUT_SUFFIX = "_mc_out";
    public static final String F_OUT_SUFFIX = "_f_out";
    public static final String B_FUSED_OUT = "_b_fused_out";
    public static final String B_OUT = "_b_out";

	public static final String SVM_TRAIN = "train";
	public static final String SVM_TEST = "test";
	public static final String SVM_RANGE = "range";
	public static final String SVM_SCALE = "scale";
	public static final String SVM_MODEL = "model";
	public static final String SVM_PREDICT = "predict";

    public static final String T1_EX = "t1_ex";
    public static final String T2_EX = "t2_ex";
    public static final String LP_EX = "lp_ex";
    public static final String W1_EX = "w1_ex";
    public static final String W2_EX = "w2_ex";
    public static final String T1LP_NR = "t1lp_nr";
    public static final String T2LP_NR = "t2lp_nr";
    public static final String W1W2_FAR = "w1w2_far";
    public static final String W1w2_AL = "w1w2_al";
    public static final String T1T2_AL = "t1t2_al";
    public static final String CAR_EX = "car_ex";





    /**
     * Property files keys
     */

	/**
	 * CBOD Core
	 */
	public static final String CBOD_OUTPUT_DIR = "net.bsrc.cbod.dir";
	public static final String CBOD_TEMP_DIR = "net.bsrc.cbod.temp.dir";
	public static final String CBOD_INPUT_IMAGE_DIR = "net.bsrc.cbod.inputImage.dir";

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
	 * MPEG Fex
	 */
	public static final String MPEG_BIL_FEX_EXECUTE_COMMAND_KEY = "net.bsrc.cbod.mpeg.bil.fex.executeCommand";
	public static final String MPEG_FEX_DIR = "net.bsrc.cbod.mpeg.fex.dir";

	/**
	 * LIBSVM
	 */
	public static final String LIB_SVM_SCALE_EXECUTE_COMMAND = "net.bsrc.cbod.libsvm.scale.executeCommand";
	public static final String LIB_SVM_TRAIN_EXECUTE_COMMAND = "net.bsrc.cbod.libsvm.train.executeCommand";
	public static final String LIB_SVM_PREDICT_EXECUTE_COMMAND = "net.bsrc.cbod.libsvm.predict.executeCommand";
	public static final String LIB_SVM_DIR = "net.bsrc.cobd.libsvm.dir";

	/**
	 * DB4O
	 */
	public static final String DB4O_FILE_PATH = "net.bsrc.cbod.db4o.filePath";

	/**
	 * SIFT
	 */
	public static final String SIFT_DICTIONARY_FILE_NAME = "cbod_sift.xml";




	/**
	 * To prevent object creation
	 */
	private CBODConstants() {

	}

}
