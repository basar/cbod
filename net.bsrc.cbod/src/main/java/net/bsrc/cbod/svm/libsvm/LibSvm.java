package net.bsrc.cbod.svm.libsvm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.bsrc.cbod.core.exception.CBODException;
import net.bsrc.cbod.core.model.EDescriptorType;
import net.bsrc.cbod.core.model.ImageModel;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: bsr Date: 10/14/13 Time: 8:39 PM
 */
public class LibSvm {

	private final static Logger logger = LoggerFactory.getLogger(LibSvm.class);

	private static LibSvm instance = null;

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

	}

	public void createTrainDataFile(String filePath, int labelA,
			List<ImageModel> imageModelListA, int labelB,
			List<ImageModel> imageModelListB, EDescriptorType descType) {


		File file = FileUtils.getFile(filePath);
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
			FileUtils.writeLines(file, lines);
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
