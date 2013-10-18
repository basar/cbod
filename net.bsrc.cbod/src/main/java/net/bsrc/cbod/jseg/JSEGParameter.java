package net.bsrc.cbod.jseg;

import net.bsrc.cbod.core.CBODConstants;
import net.bsrc.cbod.core.util.CBODUtil;

/**
 * JSEG parameter model
 * 
 * @author bsr
 * @see "http://vision.ece.ucsb.edu/segmentation/jseg/software/"
 */
public class JSEGParameter {

	/**
	 * input image file name <br>
	 * <b>This field must be provided</b>
	 */
	private String inputFileName = null;

	/**
	 * 1: image yuv <br>
	 * 2: image raw　<br>
	 * 3: image raw gray <br>
	 * 4: image pgm <br>
	 * 5: image ppm rgb <br>
	 * 6: image jpg <br>
	 * 9: image gif<br>
	 * <p>
	 * type 1,2,3 must provide image size
	 * <p>
	 * default value is 6 (jpg) <br>
	 * <b>This value must be provided</b>
	 */
	private Integer inputMediaType = 6;

	/**
	 * output image file name <br>
	 * output image format same as input image
	 */
	private String outputFileImage = null;

	/**
	 * dim orginal image to show boundaries. 0 - 1.0
	 * <p>
	 * Default value is 0.9
	 */
	private Double factor = 0.9;

	/**
	 * Output region map file name
	 * <p>
	 * The region map file is a gray-scale image. It labels the image pixels. If
	 * pixel (0,0) belongs to region 1, its value is 1. The label starts at 1
	 * and ends at the total number of regions. Your probably will only see
	 * black when you try to view the map image. That's because most values are
	 * too small. If you equalize the image, you'll see patches of regions. The
	 * equalization changes the original pixel values though.
	 * 
	 * The region map is very useful for many applications, for example, if you
	 * want to process all the pixels in a particular region of interest. On the
	 * other hand, if you want to get the region boundaries, you need to do a
	 * little job by yourself. For example, you can check if each pixel is of
	 * same label as its neighboring pixels. If no, it indicates that pixel is
	 * at the boundary.
	 */
	private String regionMapFileName = null;

	/**
	 * 3: image raw gray <br>
	 * 9: image gif
	 * <p>
	 * default value is 3
	 */
	private Integer regionMapType = 3;

	/**
	 * Color quantization thresh hold, 0-600,
	 * <p>
	 * default automatic.
	 * <p>
	 * Specify values 0-600, leave blank for automatic determination. The higher
	 * the value, the less number of quantized colors in the image. For color
	 * images, try 250. If you are unsatisfied with the result because two
	 * neighboring regions with similar colors are not getting separated, please
	 * try a smaller value say 150.
	 */
	private Integer colorQuantizationThreshold = null;

	/**
	 * Region merge threshold, 0-1.0,
	 * <p>
	 * default value is 0.4.
	 */
	private Double regionMergeThreshold = null;

	/**
	 * Number of scales.
	 * <p>
	 * The algorithm automatically determines the starting scale based on the
	 * image size and reduces the scale to refine the segmentation results. If
	 * you want to segment a small object in a large-sized image, use more
	 * number of scales. If you want to have a coarse segmentation, use 1 scale
	 * only.
	 */
	private Integer numberOfScales = null;

	/**
	 * 
	 * @param inputFileName
	 */
	public JSEGParameter(String inputFileName) {
		this.inputFileName = inputFileName;
		this.outputFileImage = inputFileName.replaceAll(
				CBODConstants.JPEG_SUFFIX, CBODConstants.SEG_SUFFIX
						+ CBODConstants.JPEG_SUFFIX);
		this.regionMapFileName = inputFileName.replaceAll(
				CBODConstants.JPEG_SUFFIX, CBODConstants.MAP_SUFFIX);
	}

	public String getInputFileName() {
		return inputFileName;
	}

	public void setInputFileName(String inputFileName) {
		this.inputFileName = inputFileName;
	}

	public Integer getInputMediaType() {
		return inputMediaType;
	}

	public void setInputMediaType(Integer inputMediaType) {
		this.inputMediaType = inputMediaType;
	}

	public String getOutputFileImage() {
		return outputFileImage;
	}

	public void setOutputFileImage(String outputFileImage) {
		this.outputFileImage = outputFileImage;
	}

	public Double getFactor() {
		return factor;
	}

	public void setFactor(Double factor) {
		this.factor = factor;
	}

	public String getRegionMapFileName() {
		return regionMapFileName;
	}

	public void setRegionMapFileName(String regionMapFileName) {
		this.regionMapFileName = regionMapFileName;
	}

	public Integer getRegionMapType() {
		return regionMapType;
	}

	public void setRegionMapType(Integer regionMapType) {
		this.regionMapType = regionMapType;
	}

	public Integer getColorQuantizationThreshold() {
		return colorQuantizationThreshold;
	}

	public void setColorQuantizationThreshold(Integer colorQuantizationThreshold) {
		this.colorQuantizationThreshold = colorQuantizationThreshold;
	}

	public Double getRegionMergeThreshold() {
		return regionMergeThreshold;
	}

	public void setRegionMergeThreshold(Double regionMergeThreshold) {
		this.regionMergeThreshold = regionMergeThreshold;
	}

	public Integer getNumberOfScales() {
		return numberOfScales;
	}

	public void setNumberOfScales(Integer numberOfScales) {
		this.numberOfScales = numberOfScales;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();

		CBODUtil.appendParam(sb, "-i", inputFileName);
		CBODUtil.appendParam(sb, "-t", inputMediaType);
		CBODUtil.appendParam(sb, "-o", outputFileImage, factor);
		CBODUtil.appendParam(sb, "-r", regionMapType, regionMapFileName);
		CBODUtil.appendParam(sb, "-q", colorQuantizationThreshold);
		CBODUtil.appendParam(sb, "-m", regionMergeThreshold);
		CBODUtil.appendParam(sb, "-l", numberOfScales);

		return sb.toString();
	}
}
