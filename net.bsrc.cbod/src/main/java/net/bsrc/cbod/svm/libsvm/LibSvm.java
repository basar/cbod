package net.bsrc.cbod.svm.libsvm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.bsrc.cbod.core.CBODConstants;
import net.bsrc.cbod.core.exception.CBODException;
import net.bsrc.cbod.core.model.Descriptor;
import net.bsrc.cbod.core.model.EDescriptorType;
import net.bsrc.cbod.core.model.ImageModel;
import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.core.util.ConfigurationUtil;
import net.bsrc.cbod.core.util.ProcessUtil;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: bsr Date: 10/14/13 Time: 8:39 PM
 */
public class LibSvm {

	private final static Logger logger = LoggerFactory.getLogger(LibSvm.class);

	private static LibSvm instance = null;

	public String scaleExecuteCommand;

	public String trainExecuteCommand;

	public String predictExecuteCommand;

	public String svmDirectoryPath;

	private static final String SCALE_EXT = ".scale";
	private static final String MODEL_EXT = ".model";
	private static final String PREDICT_EXT = ".predict";

	private LibSvm() {

	}

	public static LibSvm getInstance() {
		if (instance == null) {
			synchronized (LibSvm.class) {
				if (instance == null) {
					instance = new LibSvm();
					instance.initialize();
				}
			}
		}

		return instance;
	}

	private void initialize() {
		scaleExecuteCommand = ConfigurationUtil
				.getString(CBODConstants.LIB_SVM_SCALE_EXECUTE_COMMAND);
		trainExecuteCommand = ConfigurationUtil
				.getString(CBODConstants.LIB_SVM_TRAIN_EXECUTE_COMMAND);
		predictExecuteCommand = ConfigurationUtil
				.getString(CBODConstants.LIB_SVM_PREDICT_EXECUTE_COMMAND);
		String temp = ConfigurationUtil.getString(CBODConstants.LIB_SVM_DIR);
		svmDirectoryPath = CBODUtil.getDefaultOutputDirectoryPath()
				.concat(temp);
	}

	/**
	 * 
	 * @param fileName
	 *            data file name (not path only name)
	 * @param labelA
	 * @param imageModelListA
	 * @param labelB
	 * @param imageModelListB
	 * @param descType
	 */
	public void createFormattedDataFile(String fileName, int labelA,
			List<ImageModel> imageModelListA, int labelB,
			List<ImageModel> imageModelListB, EDescriptorType descType) {

        List<List<Double>> dataListsA = new ArrayList<List<Double>>();

        for (ImageModel imageModel : imageModelListA) {

            Descriptor descriptor = imageModel.getDescriptor(descType);
            if (descriptor == null) {
                logger.error(
                        "descriptor couldn't be found for image model: {}",
                        imageModel.toString());
                continue;
            }

            dataListsA.add(imageModel.getDescriptor(descType).getDataList());

        }

        List<List<Double>> dataListsB = new ArrayList<List<Double>>();

        for (ImageModel imageModel : imageModelListB) {

            Descriptor descriptor = imageModel.getDescriptor(descType);
            if (descriptor == null) {
                logger.error(
                        "descriptor couldn't be found for image model: {}",
                        imageModel.toString());
                continue;
            }

            dataListsB.add(imageModel.getDescriptor(descType).getDataList());
        }

        createFormattedDataFile(fileName,labelA,dataListsA,labelB,dataListsB);

	}

	/**
	 * 
	 * @param fileName
	 * @param label
	 * @param imageModelList
	 */
	public void createFormattedDataFile(String fileName, int label,
			List<ImageModel> imageModelList, EDescriptorType descType) {

	    List<List<Double>> dataLists = new ArrayList<List<Double>>();
		for (ImageModel imageModel : imageModelList) {

			Descriptor descriptor = imageModel.getDescriptor(descType);
			if (descriptor == null) {
				logger.error(
						"descriptor couldn't be found for image model: {}",
						imageModel.toString());
				continue;
			}

			dataLists.add(imageModel.getDescriptor(descType).getDataList());
		}

		createFormattedDataFile(fileName,label,dataLists);
	}

	public void createFormattedDataFile(String fileName, int labelA,
			List<List<Double>> dataListsA, int labelB,
			List<List<Double>> dataListsB) {

		if (labelA == labelB)
			throw new CBODException(
					"LabelA and LabelB must have different values!");

		if (CollectionUtils.isEmpty(dataListsA)
				|| CollectionUtils.isEmpty(dataListsB))
			throw new CBODException("data lists must not be null or empty");

		File dataFile = FileUtils.getFile(svmDirectoryPath.concat("/").concat(
				fileName));
		List<String> lines = new ArrayList<String>();

		for (List<Double> dataList : dataListsA) {
			String line = formatData(labelA, dataList);
			lines.add(line);
		}

		for (List<Double> dataList : dataListsB) {
			String line = formatData(labelB, dataList);
			lines.add(line);
		}

		try {
			FileUtils.writeLines(dataFile, lines);
		} catch (IOException e) {
			throw new CBODException(e);
		}
	}

	public void createFormattedDataFile(String fileName, int label,
			List<List<Double>> dataLists) {

		File dataFile = FileUtils.getFile(svmDirectoryPath.concat("/").concat(
				fileName));
		List<String> lines = new ArrayList<String>();

		for (List<Double> dataList : dataLists) {
			String line = formatData(label, dataList);
			lines.add(line);
		}

		try {
			FileUtils.writeLines(dataFile, lines);
		} catch (IOException e) {
			throw new CBODException(e);
		}

	}

	/**
	 * 
	 * @param trainingFileName
	 *            svm data file
	 * @param scaleParameter
	 * @return scaled file name
	 */
	public String doScale(String trainingFileName, ScaleParameter scaleParameter) {

		String outputFileName = svmDirectoryPath.concat("/")
				.concat(FilenameUtils.removeExtension(trainingFileName))
				.concat(SCALE_EXT).concat(CBODConstants.TXT_SUFFIX);

		StringBuilder sb = new StringBuilder();
		sb.append(scaleExecuteCommand).append(" ");

		String saveFile = scaleParameter.getSaveFileName();
		String restoreFile = scaleParameter.getRestoreFileName();

		if (!StringUtils.isEmpty(saveFile) && !StringUtils.isEmpty(restoreFile))
			throw new CBODException(
					"Savefile and restore file must not be empty at the same time");

		if (!StringUtils.isEmpty(saveFile)) {
			scaleParameter.setSaveFileName(svmDirectoryPath.concat("/").concat(
					saveFile));
		}

		if (!StringUtils.isEmpty(restoreFile)) {
			scaleParameter.setRestoreFileName(svmDirectoryPath.concat("/")
					.concat(restoreFile));
		}

		sb.append(scaleParameter.toString()).append(" ");

		sb.append(svmDirectoryPath.concat("/").concat(trainingFileName))
				.append(" ");

		ProcessUtil.execute(sb.toString(), new File(outputFileName));

		String scaledFileName = FilenameUtils.getName(outputFileName);

		return scaledFileName;
	}

	/**
	 * 
	 * @param trainingFileName
	 * @return model file name
	 */
	public String doTrain(String trainingFileName, TrainParameter trainParameter) {

		String modelFilePath = svmDirectoryPath.concat("/")
				.concat(FilenameUtils.removeExtension(trainingFileName))
				.concat(MODEL_EXT).concat(CBODConstants.TXT_SUFFIX);

		StringBuilder sb = new StringBuilder();
		sb.append(trainExecuteCommand).append(" ");

		if (trainParameter != null) {
			sb.append(trainParameter.toString()).append(" ");
		}

		sb.append(svmDirectoryPath.concat("/").concat(trainingFileName))
				.append(" ");
		sb.append(modelFilePath);

		ProcessUtil.execute(sb.toString(), null);

		String modelFileName = FilenameUtils.getName(modelFilePath);

		return modelFileName;
	}

	/**
	 * 
	 * @param testFileName
	 * @param modelFileName
	 * @param predictParameter
	 * @return
	 */
	public String doPredict(String testFileName, String modelFileName,
			PredictParameter predictParameter) {

		String predictFilePath = svmDirectoryPath.concat("/")
				.concat(FilenameUtils.removeExtension(modelFileName))
				.concat(PREDICT_EXT).concat(CBODConstants.TXT_SUFFIX);

		StringBuilder sb = new StringBuilder();
		sb.append(predictExecuteCommand).append(" ");

		if (predictParameter != null) {
			sb.append(predictParameter.toString()).append(" ");
		}

		sb.append(svmDirectoryPath.concat("/").concat(testFileName))
				.append(" ");
		sb.append(svmDirectoryPath.concat("/").concat(modelFileName)).append(
				" ");
		sb.append(predictFilePath);

		ProcessUtil.execute(sb.toString(), null);

		String predictFileName = FilenameUtils.getName(predictFilePath);

		return predictFileName;
	}

	private <T> String formatData(int label, List<T> data) {

		StringBuilder sb = new StringBuilder();

		sb.append(label);
		sb.append(" ");

		int attributeNo = 1;

		for (int i = 0; i < data.size(); i++) {
			T t = data.get(i);
			sb.append(attributeNo++);
			sb.append(":");
			sb.append(t.toString());
			if (i < data.size() - 1) {
				sb.append(" ");
			}
		}

		return sb.toString();
	}

	public String getAbsoluteFilePath(String fileName) {

		Validate.notEmpty(fileName, "'fileName' must not be empty");

		StringBuilder sb = new StringBuilder();
		sb.append(svmDirectoryPath);
		if (!fileName.startsWith("/")) {
			sb.append("/");
		}
		sb.append(fileName);
		return sb.toString();
	}

}
