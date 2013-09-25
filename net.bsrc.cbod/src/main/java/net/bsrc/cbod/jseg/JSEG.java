package net.bsrc.cbod.jseg;

import net.bsrc.cbod.core.CBODConstants;
import net.bsrc.cbod.core.util.ConfigurationUtil;
import net.bsrc.cbod.core.util.ProcessUtil;

/**
 * JSEG image operations
 * 
 * @author bsr
 * 
 */
public class JSEG {

	private static JSEG instance = null;

	private String outputDir;

	private String executeCommand;

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

	private void initialize() {
		outputDir = ConfigurationUtil
				.getString(CBODConstants.JSEG_OUTPUT_DIR_KEY);
		executeCommand = ConfigurationUtil
				.getString(CBODConstants.JSEG_EXECUTE_COMMAND_KEY);
	}

	public String getOutputDir() {
		return outputDir;
	}

	public String getExecuteCommand() {
		return executeCommand;
	}

	public void execute(String params) {

		StringBuilder sb = new StringBuilder();
		sb.append(executeCommand);
		sb.append(" ");
		sb.append(params);

		ProcessUtil.execute(sb.toString());
	}

	public void execute(String... params) {

		StringBuilder sb = new StringBuilder();
		sb.append(executeCommand);

		for (int i = 0; i < params.length; i = i + 2) {
			sb.append(" ");
			sb.append(params[i]);
			sb.append(" ");
			sb.append(params[i + 1]);
		}

		ProcessUtil.execute(sb.toString());

	}

	public void executeWithDefaultParams(String imgName, String imgPath) {

		String mapFile = outputDir.concat("/").concat(imgName).concat(".map");

		String segmentedImg = outputDir.concat("/").concat(imgName)
				.concat(".seg.jpg");

		execute("-i", imgPath, "-t", "6", "-o", segmentedImg + " 0.9", "-r3",
				mapFile);

	}
}
