package net.bsrc.cbod.jseg;

import net.bsrc.cbod.core.CBODConstants;
import net.bsrc.cbod.core.IProcessExecute;
import net.bsrc.cbod.core.util.ConfigurationUtil;
import net.bsrc.cbod.core.util.ProcessUtil;

/**
 * JSEG image operations
 * 
 * @author bsr
 * 
 */
public class JSEG implements IProcessExecute {

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
		ProcessUtil.execute(sb.toString());
	}

}
