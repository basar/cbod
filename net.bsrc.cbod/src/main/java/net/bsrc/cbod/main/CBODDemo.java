package net.bsrc.cbod.main;

import net.bsrc.cbod.core.CBODConstants;
import net.bsrc.cbod.core.CBODHog;
import net.bsrc.cbod.core.model.Descriptor;
import net.bsrc.cbod.core.model.EDescriptorType;
import net.bsrc.cbod.core.model.ImageModel;
import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.jseg.JSEG;
import net.bsrc.cbod.jseg.JSEGParameter;
import net.bsrc.cbod.jseg.JSEGParameterFactory;
import net.bsrc.cbod.mpeg.bil.BilMpeg7Fex;
import net.bsrc.cbod.opencv.OpenCV;
import net.bsrc.cbod.svm.libsvm.LibSvm;
import net.bsrc.cbod.svm.libsvm.ScaleParameter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: bsr Date: 24/11/13 Time: 16:53
 */
public class CBODDemo {

	private static final Logger logger = LoggerFactory
			.getLogger(CBODDemo.class);

	public static final int NEGATIVE_LABEL = 0;
	public static final int POSITIVE_LABEL = 1;

	/**
	 * 
	 * @param baseName
	 * @param descriptorType
	 * @param positiveImageModels
	 * @param negativeImageModels
	 * @return
	 */
	public static String[] createScaledTrainFileAndRangeFile(String baseName,
			EDescriptorType descriptorType,
			List<ImageModel> positiveImageModels,
			List<ImageModel> negativeImageModels) {

		String trainFileName = baseName.concat(".")
				.concat(CBODConstants.SVM_TRAIN)
				.concat(CBODConstants.TXT_SUFFIX);

		String rangeFileName = baseName.concat(".")
				.concat(CBODConstants.SVM_RANGE)
				.concat(CBODConstants.TXT_SUFFIX);

		LibSvm libSvm = LibSvm.getInstance();

		libSvm.createFormattedDataFile(trainFileName, NEGATIVE_LABEL,
				negativeImageModels, POSITIVE_LABEL, positiveImageModels,
				descriptorType);

		ScaleParameter scaleParameter = new ScaleParameter();
		scaleParameter.setSaveFileName(rangeFileName);

		String scaleTrainFileName = libSvm.doScale(trainFileName,
				scaleParameter);

		return new String[] { scaleTrainFileName, rangeFileName };

	}

	/**
	 * 
	 * @param trainFile
	 * @return
	 */
	public static String createModelFile(String trainFile) {

		LibSvm libSvm = LibSvm.getInstance();

		return libSvm.doTrain(trainFile, null);

	}

	/**
	 * 
	 * @param modelFile
	 * @param rangeFile
	 * @param descriptorType
	 */
	public static List<ImageModel> doPredict(List<ImageModel> imageSegments,
			String modelFile, String rangeFile, EDescriptorType descriptorType) {

		BilMpeg7Fex mpeg7Fex = BilMpeg7Fex.getInstance();
		LibSvm libSvm = LibSvm.getInstance();

		String testFileName = "TEST".concat(".")
				.concat(CBODConstants.SVM_TRAIN)
				.concat(CBODConstants.TXT_SUFFIX);

		if (descriptorType == EDescriptorType.EHD) {
			mpeg7Fex.extractEdgeHistogramDescriptors(imageSegments);
		}

		if (descriptorType == EDescriptorType.SCD) {
			mpeg7Fex.extractScalableColorDescriptors(imageSegments, 256);
		}

		if (descriptorType == EDescriptorType.CSD) {
			mpeg7Fex.extractColorStructureDescriptors(imageSegments, 256);
		}

		if (descriptorType == EDescriptorType.CLD) {
			mpeg7Fex.extractColorLayoutDescriptors(imageSegments, 64, 28);
		}

		if (descriptorType == EDescriptorType.HOG) {
			// HOG descriptors
			for (ImageModel imageModel : imageSegments) {
				Descriptor hogDesc = new Descriptor();
				hogDesc.setType(EDescriptorType.HOG);
				hogDesc.setDataList(CBODHog.extractHogDescriptor(imageModel));
				imageModel.getDescriptors().add(hogDesc);
			}
		}

		// Test datasi tum veriler positifmis gibi olusturuluyor. (Ignore
		// edilebilir)
		libSvm.createFormattedDataFile(testFileName, POSITIVE_LABEL,
				imageSegments, descriptorType);

		// Test file scale edilmeli!!
		ScaleParameter scaleParameter = new ScaleParameter();
		scaleParameter.setRestoreFileName(rangeFile);
		String scaleTestFileName = libSvm.doScale(testFileName, scaleParameter);

		String predictFilePath = libSvm.doPredict(scaleTestFileName, modelFile,
				null);

		//
		List<ImageModel> candidateModels = new ArrayList<ImageModel>();

		try {
			File predictFile = FileUtils.getFile(libSvm
					.getAbsoluteFilePath(predictFilePath));
			List<String> lines = FileUtils.readLines(predictFile);

			for (int i = 0; i < lines.size(); i++) {

				String line = lines.get(i);
				int label = Integer.parseInt(line);

				if (label == POSITIVE_LABEL) {
					candidateModels.add(imageSegments.get(i));
				}
			}

		} catch (IOException e) {
			logger.error("", e);
			return null;
		}

		return candidateModels;

	}

	public static List<ImageModel> segmentImage(String imagePath,
			JSEGParameter jsegParam) {
		// Image raw name
		String imageRawName = FilenameUtils.getBaseName(imagePath);
		// OS temp dir
		String tempDirectory = FileUtils.getTempDirectoryPath();
		// App temp dir
		String cbodTempDir = CBODUtil.getCbodTempDirectory();

		// Ilk olarak gelen image segmentlerine ayrilmali!
		if (jsegParam == null) {
			jsegParam = new JSEGParameter(imagePath);
		}

		jsegParam.setRegionMapFileName(tempDirectory.concat("/").concat(
				imageRawName + CBODConstants.MAP_SUFFIX));
		jsegParam.setOutputFileImage(cbodTempDir.concat("/")
				.concat(imageRawName)
				.concat(CBODConstants.SEG_SUFFIX + CBODConstants.JPEG_SUFFIX));

		String mapName = jsegParam.getRegionMapFileName();

		JSEG.getInstance().execute(jsegParam);

		List<ImageModel> imageSegments = OpenCV
				.getSegmentedRegionsAsImageModels(imagePath, mapName, true);

		// Orginal resmin segmentleri
		for (int i = 0; i < imageSegments.size(); i++) {

			ImageModel model = imageSegments.get(i);
			String regionName = tempDirectory
					.concat("/")
					.concat(imageRawName)
					.concat("." + i)
					.concat(CBODConstants.SEG_SUFFIX
							+ CBODConstants.JPEG_SUFFIX);

			model.setImagePath(regionName);
			model.setImageName(FilenameUtils.getName(regionName));

			// Her bir segment diske yazilacak
			// Save region file to disk
			OpenCV.writeImage(model.getMat(), regionName);
		}

		return imageSegments;
	}

}
