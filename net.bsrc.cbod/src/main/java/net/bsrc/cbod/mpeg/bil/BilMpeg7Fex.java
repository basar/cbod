package net.bsrc.cbod.mpeg.bil;

import java.io.File;
import java.util.List;
import java.util.Map;

import net.bsrc.cbod.core.CBODConstants;
import net.bsrc.cbod.core.IProcessExecute;
import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.core.util.ConfigurationUtil;
import net.bsrc.cbod.core.util.ProcessUtil;
import net.bsrc.cbod.mpeg.IMpegFex;

import org.apache.commons.io.FileUtils;

/**
 * Wrapper class for mpeg 7 feature extractor
 * 
 * Thanks to "http://www.cs.bilkent.edu.tr/~bilmdg/bilvideo-7/Software.html"
 * 
 * User: bsr Date: 10/7/13 Time: 9:25 PM
 */
public class BilMpeg7Fex implements IMpegFex, IProcessExecute {

	private static BilMpeg7Fex instance = null;

	private String executeCommand;

	private String mpegFexDir;

	private final static String CSD = "CSD";
    private final static String IN_SUFFIX = "_in.txt";
    private final static String OUT_SUFFIX = "_out.txt";

	private BilMpeg7Fex() {

	}

	public static BilMpeg7Fex getInstance() {
		if (instance == null) {
			synchronized (BilMpeg7Fex.class) {
				if (instance == null) {
					instance = new BilMpeg7Fex();
					instance.initialize();
				}
			}
		}

		return instance;
	}

	private void initialize() {
		executeCommand = ConfigurationUtil
				.getString(CBODConstants.MPEG_BIL_FEX_EXECUTE_COMMAND_KEY);
		String temp = ConfigurationUtil.getString(CBODConstants.MPEG_FEX_DIR);
		mpegFexDir = CBODUtil.getDefaultOutputDirectoryPath().concat(temp);
	}

	private void writeImageNamesToFile(String descName) {

	}

	@Override
	public List<Map<String, int[]>> extractColorStructureDescriptors(
			List<String> imgNames, int descriptorSize) {

		File tempDir = FileUtils.getTempDirectory();

		return null;
	}

	@Override
	public void execute(String parameter) {
		StringBuilder sb = new StringBuilder();
		sb.append(executeCommand);
		sb.append(" ");
		sb.append(parameter);
		ProcessUtil.execute(sb.toString());
	}


    public String getMpegFexDir() {
        return mpegFexDir;
    }
}
