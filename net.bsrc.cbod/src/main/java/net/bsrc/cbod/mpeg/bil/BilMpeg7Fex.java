package net.bsrc.cbod.mpeg.bil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bsrc.cbod.core.CBODConstants;
import net.bsrc.cbod.core.IProcessExecute;
import net.bsrc.cbod.core.exception.CBODException;
import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.core.util.ConfigurationUtil;
import net.bsrc.cbod.core.util.ProcessUtil;
import net.bsrc.cbod.mpeg.IMpegFex;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

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

	private String mpegFexDirPath;

	private final static String CSD = "CSD";
	private final static String EHD = "EHD";
	private final static String TMP_FILE_NAME = "fex.txt";

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
		mpegFexDirPath = CBODUtil.getDefaultOutputDirectoryPath().concat(temp);
		// Create mpeg fex directory if not exist
		CBODUtil.createDirectory(mpegFexDirPath);
	}

	private String writeImageNamesToFile(List<String> imgNames) {

		File tmpDir = FileUtils.getTempDirectory();

		File tmpFile = FileUtils.getFile(tmpDir.getAbsolutePath().concat(
				"/" + TMP_FILE_NAME));

		try {
			FileUtils.writeLines(tmpFile, imgNames);
		} catch (IOException e) {
			throw new CBODException(e);
		}

		return tmpFile.getAbsolutePath();
	}

	private String getDescriptorFile(String desciptorName) {

		File descriptorFile = FileUtils.getFile(mpegFexDirPath.concat("/")
				.concat(desciptorName).concat(CBODConstants.TXT_SUFFIX));
		try {
			FileUtils.touch(descriptorFile);
		} catch (IOException e) {
			throw new CBODException(e);
		}

		return descriptorFile.getAbsolutePath();
	}

	private List<Map<String, List<Integer>>> getDescriptors(String fileName) {

		List<Map<String, List<Integer>>> resultMap = new ArrayList<Map<String, List<Integer>>>();

		File file = FileUtils.getFile(fileName);

		try {

			List<String> lines = FileUtils.readLines(file);

			for (String line : lines) {

				String imageName = null;
				List<Integer> descDataList = new ArrayList<Integer>();

				String[] arr = StringUtils.split(line, " ");

				for (int i = 0; i < arr.length; i++) {
					// First element image name
					if (i == 0) {
						imageName = arr[i];
						continue;
					}
					// other elements are the descriptors
					descDataList.add(Integer.valueOf(arr[i]));
				}

				Map<String, List<Integer>> map = new HashMap<String, List<Integer>>();
				map.put(imageName, descDataList);

				resultMap.add(map);
			}

		} catch (IOException e) {
			throw new CBODException(e);
		}

		return resultMap;
	}

	@Override
	public List<Map<String, List<Integer>>> extractColorStructureDescriptors(
			List<String> imgNames, Integer descriptorSize) {

		StringBuilder parameter = new StringBuilder();
		parameter.append(CSD).append(" ");

		if (descriptorSize != null)
			parameter.append(descriptorSize).append(" ");

		String inputFilePath = writeImageNamesToFile(imgNames);
		parameter.append(inputFilePath).append(" ");

		String descriptorFile = getDescriptorFile(CSD);
		parameter.append(descriptorFile);

		execute(parameter.toString());

		return getDescriptors(descriptorFile);
	}

	@Override
	public void execute(String parameter) {
		StringBuilder sb = new StringBuilder();
		sb.append(executeCommand);
		sb.append(" ");
		sb.append(parameter);
		ProcessUtil.execute(sb.toString());
	}

	public String getMpegFexDirPath() {
		return mpegFexDirPath;
	}
}
