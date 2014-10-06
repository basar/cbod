package net.bsrc.cbod.jseg;

import net.bsrc.cbod.core.CBODConstants;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.Validate;

/**
 * User: bsr
 */
public class JSEGParameterFactory {

	public static JSEGParameter createJSEGParameter(String imagePath,
			String outputDir) {

		Validate.notEmpty(imagePath);
		Validate.notEmpty(outputDir);

		String baseName = FilenameUtils.getBaseName(imagePath);

		JSEGParameter jsegParameter = new JSEGParameter();
		jsegParameter.setInputFileName(imagePath);
		jsegParameter.setOutputFileImage(outputDir.concat("/").concat(baseName)
				.concat(CBODConstants.JPEG_SUFFIX));
		jsegParameter.setRegionMapFileName(outputDir.concat("/")
				.concat(baseName).concat(CBODConstants.MAP_SUFFIX));

		return jsegParameter;
	}

}
