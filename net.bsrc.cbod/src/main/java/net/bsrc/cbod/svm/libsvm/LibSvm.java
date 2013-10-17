package net.bsrc.cbod.svm.libsvm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.bsrc.cbod.core.CBODConstants;
import net.bsrc.cbod.core.exception.CBODException;
import net.bsrc.cbod.core.model.EDescriptorType;
import net.bsrc.cbod.core.model.ImageModel;
import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.core.util.ConfigurationUtil;
import net.bsrc.cbod.core.util.ProcessUtil;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: bsr Date: 10/14/13 Time: 8:39 PM
 */
public class LibSvm {

	private final static Logger logger = LoggerFactory.getLogger(LibSvm.class);

	private static LibSvm instance = null;

	public String scaleExecuteCommand;

	public String svmDirectoryPath;

	private static final String SCALE_EXT = ".scale";

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
		String temp = ConfigurationUtil.getString(CBODConstants.LIB_SVM_DIR);
		svmDirectoryPath = CBODUtil.getDefaultOutputDirectoryPath()
				.concat(temp);
	}



    public void doTrain(){



    }


	/**
	 * 
	 * @param dataFileName
	 *            svm data file
	 * @param scaleParameter
	 * @return scaled file name
	 */
	public String doScale(String dataFileName,
			ScaleParameter scaleParameter) {

		String outputFileName = svmDirectoryPath.concat("/")
				.concat(FilenameUtils.removeExtension(dataFileName))
				.concat(SCALE_EXT).concat(CBODConstants.TXT_SUFFIX);

		StringBuilder sb = new StringBuilder();
		sb.append(scaleExecuteCommand).append(" ");

		String saveFile = scaleParameter.getSaveFileName();
		String restoreFile = scaleParameter.getRestoreFileName();

		if (!StringUtils.isEmpty(saveFile) && !StringUtils.isEmpty(restoreFile))
			throw new CBODException(
					"Savefile and restore file must not be empty at the same time");

		if (!StringUtils.isEmpty(saveFile)) {
			scaleParameter.setSaveFileName(svmDirectoryPath.concat("/")
					.concat(saveFile));
		}

		if (!StringUtils.isEmpty(restoreFile)) {
			scaleParameter.setRestoreFileName(svmDirectoryPath.concat("/")
					.concat(restoreFile));
		}

		sb.append(scaleParameter.toString()).append(" ");

		sb.append(svmDirectoryPath.concat("/").concat(dataFileName))
				.append(" ");

		ProcessUtil.execute(sb.toString(), new File(outputFileName));

		String scaledFileName = FilenameUtils.getName(outputFileName);

		return scaledFileName;
	}

	/**
	 * 
	 * @param dataFileName
	 *            data file name (not path only name)
	 * @param labelA
	 * @param imageModelListA
	 * @param labelB
	 * @param imageModelListB
	 * @param descType
	 */
	public void createFormattedDataFile(String dataFileName, int labelA,
			List<ImageModel> imageModelListA, int labelB,
			List<ImageModel> imageModelListB, EDescriptorType descType) {

		if (labelA == labelB)
			throw new CBODException(
					"LabelA and LabelB must have different values!");

		if (CollectionUtils.isEmpty(imageModelListA)
				|| CollectionUtils.isEmpty(imageModelListB))
			throw new CBODException(
					"Imagemodel lists must not be null or empty");

		File dataFile = FileUtils.getFile(svmDirectoryPath.concat("/").concat(
				dataFileName));
		List<String> lines = new ArrayList<String>();

		for (ImageModel imageModel : imageModelListA) {

			String line = formatData(labelA, imageModel.getDescriptor(descType)
					.getDataList());
			lines.add(line);

		}

		for (ImageModel imageModel : imageModelListB) {

			String line = formatData(labelB, imageModel.getDescriptor(descType)
					.getDataList());
			lines.add(line);
		}

		try {
			FileUtils.writeLines(dataFile, lines);
		} catch (IOException e) {
			throw new CBODException(e);
		}

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

}
