package net.bsrc.cbod.mpeg;

import java.util.List;
import java.util.Map;

/**
 * User: bsr Date: 10/7/13 Time: 9:48 PM
 */
public interface IMpegFex {

	List<Map<String, List<Integer>>> extractColorStructureDescriptors(
			List<String> imgNames, Integer descriptorSize);

}
