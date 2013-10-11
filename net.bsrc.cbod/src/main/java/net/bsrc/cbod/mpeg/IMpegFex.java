package net.bsrc.cbod.mpeg;

import java.util.List;

import net.bsrc.cbod.core.model.ImageModel;

/**
 * User: bsr Date: 10/7/13 Time: 9:48 PM
 */
public interface IMpegFex {

	void extractColorStructureDescriptors(List<ImageModel> listImageModel,
			Integer descriptorSize);

	void extractScalableColorDescriptors(List<ImageModel> imageModelList,
			Integer descriptorSize);

	void extractColorLayoutDescriptors(List<ImageModel> imageModelList,
			Integer numberOfYCoeff, Integer numberOfCCoeff);

	void extractDominantColorDescriptors(List<ImageModel> imageModelList,
			Integer normalizationFlag, Integer varianceFlag,
			Integer spatialFlag, Integer numBin1, Integer numBin2,
			Integer numBin3);

	void extractHomogeneousTextureDesciptors(List<ImageModel> imageModelList,
			Integer layerFLag);

}
