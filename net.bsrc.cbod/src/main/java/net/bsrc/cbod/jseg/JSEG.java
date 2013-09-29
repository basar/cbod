package net.bsrc.cbod.jseg;

import java.io.File;

import org.apache.commons.io.FileUtils;

import net.bsrc.cbod.core.CBODConstants;
import net.bsrc.cbod.core.exception.CBODException;
import net.bsrc.cbod.core.util.CBODUtil;
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

	private void initialize() {
		executeCommand = ConfigurationUtil
				.getString(CBODConstants.JSEG_EXECUTE_COMMAND_KEY);
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

	/**
	 * 
	 * @param imgPath
	 */
	public void executeWithDefaultParams(String imgPath) {

		String outputDir = CBODUtil.getDefaultOutputDirectory()
				.getAbsolutePath();
		executeWithDefaultParams(imgPath, outputDir);

	}

	/**
	 * 
	 * @param imgPath
	 * @param outputDir
	 */
	public void executeWithDefaultParams(String imgPath, String outputDir) {

		File file = FileUtils.getFile(imgPath);

		if (!file.exists()) {
			throw new CBODException("Dosya bulunamadi:" + imgPath);
		}

		String imgName = file.getName();
		String mapFile = outputDir.concat("/").concat(imgName).concat(".map");

		String segmentedImg = outputDir.concat("/").concat(imgName)
				.concat(".seg.jpg");

		execute("-i", imgPath, "-t", "6", "-o", segmentedImg + " 0.9", "-r3",
				mapFile);

	}
	
	
}

