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
import net.bsrc.cbod.core.model.Descriptor;
import net.bsrc.cbod.core.model.EDescriptorType;
import net.bsrc.cbod.core.model.ImageModel;
import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.core.util.ConfigurationUtil;
import net.bsrc.cbod.core.util.ProcessUtil;
import net.bsrc.cbod.mpeg.IMpegFex;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
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

		extractDescriptors(EDescriptorType.CSD, imageModelList, descriptorSize);
	}

	@Override
	public void extractScalableColorDescriptors(
			List<ImageModel> imageModelList, Integer descriptorSize) {

		extractDescriptors(EDescriptorType.SCD, imageModelList, descriptorSize);
	}

	@Override
	public void extractColorLayoutDescriptors(List<ImageModel> imageModelList,
			Integer numberOfYCoeff, Integer numberOfCCoeff) {

		extractDescriptors(EDescriptorType.CLD, imageModelList, numberOfYCoeff,
				numberOfCCoeff);

	}

	@Override
	public void extractDominantColorDescriptors(
			List<ImageModel> imageModelList, Integer normalizationFlag,
			Integer varianceFlag, Integer spatialFlag, Integer numBin1,
			Integer numBin2, Integer numBin3) {

		extractDescriptors(EDescriptorType.DCD, imageModelList,
				normalizationFlag, varianceFlag, spatialFlag, numBin1, numBin2,
				numBin3);

	}

	@Override
	public void extractHomogeneousTextureDesciptors(
			List<ImageModel> imageModelList, Integer layerFLag) {

		extractDescriptors(EDescriptorType.HTD, imageModelList, layerFLag);
	}

	@Override
	public void extractEdgeHistogramDescriptors(List<ImageModel> imageModelList) {

		extractDescriptors(EDescriptorType.EHD, imageModelList, new Object[] {});
	}

	/**
	 * 
	 * @param descType
	 * @param imageModelList
	 * @param params
	 * @param <T>
	 */
	private <T> void extractDescriptors(EDescriptorType descType,
			List<ImageModel> imageModelList, T... params) {

		Validate.notEmpty(imageModelList, "Image model list must not be null");

		StringBuilder parameter = new StringBuilder();
		parameter.append(descType).append(" ");

		if (params != null && params.length > 0) {
			for (T t : params) {
				if (t != null)
					parameter.append(t).append(" ");
			}
		}

		String inputFilePath = writeImageFullPathsToFile(imageModelList);
		parameter.append(inputFilePath).append(" ");

		String descriptorFile = getDescriptorFile(descType.getName());
		parameter.append(descriptorFile);

		execute(parameter.toString());

		List<Map<String, List<Double>>> descriptors = getDescriptors(descriptorFile);
		// Fill descriptors
		for (Map<String, List<Double>> map : descriptors) {
			for (ImageModel imgModel : imageModelList) {

				if (imgModel.getImageName() == null)
					throw new CBODException(
							"Image model name cannot be null!:{}"
									+ imgModel.toString());
				List<Double> descs = map.get(imgModel.getImageName());

				if (descs != null) {

					Descriptor descriptor = new Descriptor(descType);
					descriptor.setDataList(descs);
					imgModel.getDescriptors().add(descriptor);
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

	private List<Map<String, List<Double>>> getDescriptors(String fileName) {

		List<Map<String, List<Double>>> resultMap = new ArrayList<Map<String, List<Double>>>();

		File file = FileUtils.getFile(fileName);

		try {

			List<String> lines = FileUtils.readLines(file);

			for (String line : lines) {

				String imageName = null;
				List<Double> descDataList = new ArrayList<Double>();

				String[] arr = StringUtils.split(line, " ");

				for (int i = 0; i < arr.length; i++) {
					// First element image name
					if (i == 0) {
						imageName = arr[i];
						continue;
					}
					// other elements are the descriptors
					descDataList.add(Double.valueOf(arr[i]));
				}

				Map<String, List<Double>> map = new HashMap<String, List<Double>>();
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
			imageNames.add(imgModel.getImagePath());
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
		ProcessUtil.execute(tmp, null);
	}

	public String getMpegFexDirPath() {
		return mpegFexDirPath;
	}

}
