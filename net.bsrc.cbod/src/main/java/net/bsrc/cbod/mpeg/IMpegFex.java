package net.bsrc.cbod.mpeg;

import java.util.List;

import net.bsrc.cbod.core.model.ImageModel;

/**
 * User: bsr Date: 10/7/13 Time: 9:48 PM
 */
public interface IMpegFex {

	void extractColorStructureDescriptors(List<ImageModel> listImageModel,
			Integer descriptorSize);

}
