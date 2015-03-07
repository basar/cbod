package net.bsrc.cbod.jseg;

import net.bsrc.cbod.core.CBODConstants;
import net.bsrc.cbod.core.IProcessExecute;
import net.bsrc.cbod.core.model.ImageModel;
import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.core.util.ConfigurationUtil;
import net.bsrc.cbod.core.util.ProcessUtil;
import net.bsrc.cbod.opencv.OpenCV;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * JSEG image operations
 * 
 * @author bsr
 * 
 */
public class JSEG implements IProcessExecute {

	private static final Logger logger = LoggerFactory.getLogger(JSEG.class);

	private static JSEG instance = null;

	private String executeCommand;



	public final static int MIN_IMG_WIDTH = 64;
	public final static int MIN_IMG_HEIGHT = 64;

	private JSEG() {

	}

	public static JSEG getInstance() {
		if (instance == null) {
			synchronized (JSEG.class) {
				if (instance == null) {
					instance = new JSEG();
					instance.initialize();
				}
			}
		}

		return instance;
	}

	public static void doSegmentation(JSEGParameter parameter) {
		JSEG.getInstance().execute(parameter);
	}

	private void initialize() {
		executeCommand = ConfigurationUtil
				.getString(CBODConstants.JSEG_EXECUTE_COMMAND_KEY);
	}


    public static List<ImageModel> segmentImage(String imagePath,
                                                JSEGParameter jsegParam) {
        // Image raw name
        String imageRawName = FilenameUtils.getBaseName(imagePath);
        // OS temp dir
        String tempDirectory = FileUtils.getTempDirectoryPath();
        // App temp dir
        String cbodTempDir = CBODUtil.getCbodTempDirectory();

        if (jsegParam == null) {
            jsegParam = new JSEGParameter(imagePath);
        }

        jsegParam.setRegionMapFileName(tempDirectory.concat("/").concat(
                imageRawName + CBODConstants.MAP_SUFFIX));
        jsegParam.setOutputFileImage(cbodTempDir.concat("/")
                .concat(imageRawName)
                .concat(CBODConstants.SEG_SUFFIX + CBODConstants.JPEG_SUFFIX));

        String mapName = jsegParam.getRegionMapFileName();

        getInstance().execute(jsegParam);

        List<ImageModel> imageSegments = OpenCV
                .getSegmentedRegionsAsImageModels(imagePath, mapName, true);

        // Orginal resmin segmentleri
        for (int i = 0; i < imageSegments.size(); i++) {

            ImageModel model = imageSegments.get(i);
            String regionName = tempDirectory
                    .concat("/")
                    .concat(imageRawName)
                    .concat("." + i)
                    .concat(CBODConstants.SEG_SUFFIX
                            + CBODConstants.JPEG_SUFFIX);

            model.setImagePath(regionName);
            model.setImageName(FilenameUtils.getName(regionName));

            // Her bir segment diske yazilacak
            // Save region file to disk
            OpenCV.writeImage(model.getMat(), regionName);
        }

        return imageSegments;
    }

	public String getExecuteCommand() {
		return executeCommand;
	}

	/**
	 * 
	 * @param param
	 */
	public void execute(JSEGParameter param) {
		execute(param.toString());
	}

	/**
	 * 
	 * @param parameter
	 *            process parameter
	 */
	public void execute(String parameter) {
		StringBuilder sb = new StringBuilder();
		sb.append(executeCommand);
		sb.append(" ");
		sb.append(parameter);
		ProcessUtil.execute(sb.toString(), null);
	}

}
