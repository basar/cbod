package net.bsrc.cbod.core.util;

import java.util.ArrayList;
import java.util.List;

import net.bsrc.cbod.core.CBODConstants;
import net.bsrc.cbod.core.CBODHog;
import net.bsrc.cbod.core.CBODSift;
import net.bsrc.cbod.core.model.Descriptor;
import net.bsrc.cbod.core.model.EDescriptorType;
import net.bsrc.cbod.core.model.EObjectType;
import net.bsrc.cbod.core.model.ImageModel;
import net.bsrc.cbod.core.persistence.ImageModelService;
import net.bsrc.cbod.mpeg.bil.BilMpeg7Fex;
import net.bsrc.cbod.opencv.OpenCV;

import org.apache.commons.io.FilenameUtils;

import com.googlecode.javacv.cpp.opencv_core.CvMat;

public class DBInitializeUtil {

	private final static boolean SAVE_WHEEL = true;
	private final static boolean SAVE_HEAD_LIGHT = true;
	private final static boolean SAVE_TAIL_LIGHT = true;
	private final static boolean SAVE_NONE_CAR_PART = true;

	public static void saveImageModelstoDB() {

		String cbodDirPath = CBODUtil.getDefaultOutputDirectoryPath();
		ImageModelService service = ImageModelService.getInstance();

		List<ImageModel> saveImageModels = new ArrayList<ImageModel>();

		if (SAVE_WHEEL) {

			// Wheel paths
			String wheelTrainImagePath = cbodDirPath
					.concat("/image_db/wheel/train");
			String wheelTestImagePath = cbodDirPath
					.concat("/image_db/wheel/test");

			// Train
			saveImageModels.addAll(createImageModels(wheelTrainImagePath,
					false, EObjectType.WHEEL));
			// Test
			saveImageModels.addAll(createImageModels(wheelTestImagePath, true,
					EObjectType.WHEEL));

		}

		if (SAVE_TAIL_LIGHT) {

			// Tail light paths
			String tailLightTrainImagePath = cbodDirPath
					.concat("/image_db/tail_light/train");
			String tailLightTestImagePath = cbodDirPath
					.concat("/image_db/tail_light/test");

			// Train
			saveImageModels.addAll(createImageModels(tailLightTrainImagePath,
					false, EObjectType.TAIL_LIGHT));
			// Test
			saveImageModels.addAll(createImageModels(tailLightTestImagePath,
					true, EObjectType.TAIL_LIGHT));

		}

		if (SAVE_HEAD_LIGHT) {
			// Head light paths
			String headLightTrainImagePath = cbodDirPath
					.concat("/image_db/head_light/train");
			String headLightTestImagePath = cbodDirPath
					.concat("/image_db/head_light/test");

			saveImageModels.addAll(createImageModels(headLightTrainImagePath,
					false, EObjectType.HEAD_LIGHT));

			saveImageModels.addAll(createImageModels(headLightTestImagePath,
					true, EObjectType.HEAD_LIGHT));
		}

		if (SAVE_NONE_CAR_PART) {

			String noneCarPartTrainImagePath = cbodDirPath
					.concat("/image_db/none_car_part/train");
			String noneCarPartTestImagePath = cbodDirPath
					.concat("/image_db/none_car_part/test");

			saveImageModels.addAll(createImageModels(noneCarPartTrainImagePath,
					false, EObjectType.NONE_CAR_PART));

			saveImageModels.addAll(createImageModels(noneCarPartTestImagePath,
					true, EObjectType.NONE_CAR_PART));
		}

		// Create sift dictionary
		CvMat siftDictionary = CBODSift.createDictionary(saveImageModels, 125);
		String tempDir = CBODUtil.getCbodTempDirectory() + "/";
		OpenCV.storeCvMatToFile(tempDir.concat("sift_dict.xml"), "sift_dict",
				siftDictionary);

		extractFeatureVectors(saveImageModels, siftDictionary);

		service.saveImageModelList(saveImageModels);

	}

	private static List<ImageModel> createImageModels(
			final String imageFolderPath, final boolean isTestImage,
			final EObjectType objectType) {

		List<ImageModel> resultList = new ArrayList<ImageModel>();

		for (String filePath : CBODUtil.getFileList(imageFolderPath,
				CBODConstants.JPEG_SUFFIX)) {

			ImageModel imgModel = new ImageModel();
			imgModel.setImagePath(filePath);
			imgModel.setImageName(FilenameUtils.getName(filePath));

			imgModel.setTestImage(isTestImage);
			imgModel.setObjectType(objectType);

			resultList.add(imgModel);
		}

		return resultList;

	}

	private static void extractFeatureVectors(List<ImageModel> imageModelList,
			CvMat siftDictionary) {

		// SIFT descriptors
		for (ImageModel imageModel : imageModelList) {
			Descriptor desc = new Descriptor();
			desc.setType(EDescriptorType.SIFT);
			desc.setDataList(CBODSift.extractSIFTDescriptorAsList(imageModel,
					siftDictionary));
			imageModel.getDescriptors().add(desc);
		}

		// HOG descriptors
		for (ImageModel imageModel : imageModelList) {
			Descriptor hogDesc = new Descriptor();
			hogDesc.setType(EDescriptorType.HOG);
			hogDesc.setDataList(CBODHog.extractHogDescriptor(imageModel));
			imageModel.getDescriptors().add(hogDesc);
		}

		BilMpeg7Fex mpegFex = BilMpeg7Fex.getInstance();

		mpegFex.extractColorStructureDescriptors(imageModelList, 256);
		mpegFex.extractScalableColorDescriptors(imageModelList, 256);
		mpegFex.extractColorLayoutDescriptors(imageModelList, 64, 28);
		mpegFex.extractDominantColorDescriptors(imageModelList, 1, 0, 1, 32,
				32, 32);
		mpegFex.extractEdgeHistogramDescriptors(imageModelList);

	}

}
