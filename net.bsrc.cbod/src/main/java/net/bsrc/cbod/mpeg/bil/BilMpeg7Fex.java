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
import net.bsrc.cbod.core.model.ImageModel;
import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.core.util.ConfigurationUtil;
import net.bsrc.cbod.core.util.ProcessUtil;
import net.bsrc.cbod.mpeg.IMpegFex;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper class for mpeg 7 feature extractor
 * 
 * Thanks to "http://www.cs.bilkent.edu.tr/~bilmdg/bilvideo-7/Software.html"
 * 
 * User: bsr Date: 10/7/13 Time: 9:25 PM
 */
public class BilMpeg7Fex implements IMpegFex, IProcessExecute {

	private static Logger logger = LoggerFactory.getLogger(BilMpeg7Fex.class);

	private static BilMpeg7Fex instance = null;

	private String executeCommand;

	private String mpegFexDirPath;

	private final static String CSD = "CSD";
	private final static String SCD = "SCD";
	private final static String CLD = "CLD";
	private final static String DCD = "DCD";
	private final static String HTD = "HTD";
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

	@Override
	public void extractColorStructureDescriptors(
			List<ImageModel> imageModelList, Integer descriptorSize) {

		extractDescriptors(CSD, imageModelList, descriptorSize);
	}

	@Override
	public void extractScalableColorDescriptors(
			List<ImageModel> imageModelList, Integer descriptorSize) {

		extractDescriptors(SCD, imageModelList, descriptorSize);
	}

	@Override
	public void extractColorLayoutDescriptors(List<ImageModel> imageModelList,
			Integer numberOfYCoeff, Integer numberOfCCoeff) {

		extractDescriptors(CLD, imageModelList, numberOfYCoeff, numberOfCCoeff);

	}

	@Override
	public void extractDominantColorDescriptors(
			List<ImageModel> imageModelList, Integer normalizationFlag,
			Integer varianceFlag, Integer spatialFlag, Integer numBin1,
			Integer numBin2, Integer numBin3) {

		extractDescriptors(DCD, imageModelList, normalizationFlag,
				varianceFlag, spatialFlag, numBin1, numBin2, numBin3);

	}

	@Override
	public void extractHomogeneousTextureDesciptors(
			List<ImageModel> imageModelList, Integer layerFLag) {

		extractDescriptors(HTD, imageModelList, layerFLag);
	}

	/**
	 * 
	 * @param descType
	 * @param imageModelList
	 * @param params
	 * @param <T>
	 */
	private <T> void extractDescriptors(String descType,
			List<ImageModel> imageModelList, T... params) {

		if (imageModelList == null || imageModelList.isEmpty()) {
			throw new CBODException("imageModelList must not be null or empty");
		}

		StringBuilder parameter = new StringBuilder();
		parameter.append(descType).append(" ");

		for (T t : params) {
			if (t != null)
				parameter.append(t).append(" ");
		}

		String inputFilePath = writeImageFullPathsToFile(imageModelList);
		parameter.append(inputFilePath).append(" ");

		String descriptorFile = getDescriptorFile(descType);
		parameter.append(descriptorFile);

		execute(parameter.toString());

		// Fill dominant color descriptors
		for (Map<String, List<Integer>> map : getDescriptors(descriptorFile)) {
			for (ImageModel imgModel : imageModelList) {
				List<Integer> descs = map.get(imgModel.getImageName());
				if (descs != null) {

					if (descType.equals(CSD))
						imgModel.setColorStructureDescriptors(descs);

					if (descType.equals(SCD))
						imgModel.setScalableColorDescriptors(descs);

					if (descType.equals(CLD))
						imgModel.setColorLayoutDescriptors(descs);

					if (descType.equals(DCD))
						imgModel.setDominantColorDesciptors(descs);

					if (descType.equals(HTD))
						imgModel.setHomogeneousTextureDescriptors(descs);

					break;
				}
			}
		}

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

	private String writeImageFullPathsToFile(List<ImageModel> imageModelList) {

		File tmpDir = FileUtils.getTempDirectory();

		File tmpFile = FileUtils.getFile(tmpDir.getAbsolutePath().concat(
				"/" + TMP_FILE_NAME));

		List<String> imageNames = new ArrayList<String>();
		for (ImageModel imgModel : imageModelList) {
			imageNames.add(imgModel.getImageFullPath());
		}

		try {
			FileUtils.writeLines(tmpFile, imageNames);
		} catch (IOException e) {
			throw new CBODException(e);
		}

		return tmpFile.getAbsolutePath();
	}

	@Override
	public void execute(String parameter) {
		StringBuilder sb = new StringBuilder();
		sb.append(executeCommand);
		sb.append(" ");
		sb.append(parameter);
		String tmp = sb.toString();
		logger.info(tmp);
		ProcessUtil.execute(tmp);
	}

	public String getMpegFexDirPath() {
		return mpegFexDirPath;
	}

}
