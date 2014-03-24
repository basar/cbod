package net.bsrc.cbod.main;

import java.util.ArrayList;
import java.util.List;

import net.bsrc.cbod.core.CBODConstants;
import net.bsrc.cbod.core.model.EObjectType;
import net.bsrc.cbod.core.model.ImageModel;
import net.bsrc.cbod.core.persistence.ImageModelService;
import net.bsrc.cbod.core.util.CBODUtil;
import net.bsrc.cbod.mpeg.bil.BilMpeg7Fex;

import org.apache.commons.io.FilenameUtils;

public class DBInitializeUtil {

	private final static boolean SAVE_WHEEL = true;
	private final static boolean SAVE_HEAD_LIGHT = true;
	private final static boolean SAVE_TAIL_LIGHT = true;
	private final static boolean SAVE_NONE_CAR_PART = true;

	public static void saveImageModelstoDB() {

		String cbodDirPath = CBODUtil.getDefaultOutputDirectoryPath();
		ImageModelService service = ImageModelService.getInstance();

		if (SAVE_WHEEL) {

			// Wheel paths
			String wheelTrainImagePath = cbodDirPath
					.concat("/image_db/wheel/train");
			String wheelTestImagePath = cbodDirPath
					.concat("/image_db/wheel/test");

			// Train
			service.saveImageModelList(createImageModels(wheelTrainImagePath,
					false, EObjectType.WHEEL));
			// Test
			service.saveImageModelList(createImageModels(wheelTestImagePath,
					true, EObjectType.WHEEL));

		}

		if (SAVE_TAIL_LIGHT) {

			// Tail light paths
			String tailLightTrainImagePath = cbodDirPath
					.concat("/image_db/tail_light/train");
			String tailLightTestImagePath = cbodDirPath
					.concat("/image_db/tail_light/test");

			// Train
			service.saveImageModelList(createImageModels(
					tailLightTrainImagePath, false, EObjectType.TAIL_LIGHT));
			// Test
			service.saveImageModelList(createImageModels(
					tailLightTestImagePath, true, EObjectType.TAIL_LIGHT));

		}

		if (SAVE_HEAD_LIGHT) {
			// Head light paths
			String headLightTrainImagePath = cbodDirPath
					.concat("/image_db/head_light/train");
			String headLightTestImagePath = cbodDirPath
					.concat("/image_db/head_light/test");

			// Train
			service.saveImageModelList(createImageModels(
					headLightTrainImagePath, false, EObjectType.HEAD_LIGHT));
			// Test
			service.saveImageModelList(createImageModels(
					headLightTestImagePath, true, EObjectType.HEAD_LIGHT));
		}
		
		if(SAVE_NONE_CAR_PART){
			
			String noneCarPartTrainImagePath = cbodDirPath
					.concat("/image_db/none_car_part/train");
			String noneCarPartTestImagePath = cbodDirPath
					.concat("/image_db/none_car_part/test");
			
			// Train
			service.saveImageModelList(createImageModels(
					noneCarPartTrainImagePath, false, EObjectType.NONE_CAR_PART));
			// Test
			service.saveImageModelList(createImageModels(
					noneCarPartTestImagePath, true, EObjectType.NONE_CAR_PART));	
		}
		
		

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
			//imgModel.setData(CBODUtil.getFileData(filePath));

			imgModel.setTestImage(isTestImage);
			imgModel.setObjectType(objectType);

			resultList.add(imgModel);
		}

		BilMpeg7Fex mpegFex = BilMpeg7Fex.getInstance();

		mpegFex.extractColorStructureDescriptors(resultList, 256);
		mpegFex.extractScalableColorDescriptors(resultList, 256);
		mpegFex.extractColorLayoutDescriptors(resultList, 64, 28);
		mpegFex.extractDominantColorDescriptors(resultList, 1, 0, 1, 32, 32, 32);
		mpegFex.extractEdgeHistogramDescriptors(resultList);

		return resultList;

	}

}
