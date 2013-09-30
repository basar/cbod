package net.bsrc.cbod.core;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Set;

import net.bsrc.cbod.opencv.OpenCV;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class RegionMapFactory {

	public static RegionMap getRegionMap(String imgPath, String mapPath) {

		File mapFile = FileUtils.getFile(mapPath);

		byte[] arr = null;

		try {
			arr = IOUtils.toByteArray(new FileInputStream(mapFile));
		} catch (Exception e) {
			e.printStackTrace();
		}

		Mat orgImg = OpenCV.getImageMat(imgPath);

		final Mat map = new Mat(orgImg.size(), CvType.CV_8UC1);
		final Set<Integer> labels = new HashSet<Integer>();

		int count = 0;

		for (int i = 0; i < map.rows(); i++) {

			for (int j = 0; j < map.cols(); j++) {

				final byte temp = arr[count++];
				// signed byte (8s) usigned integer a cevriliyor
				int label = temp & 0xFF;
				if (!labels.contains(label))
					labels.add(label);
				map.put(i, j, temp);

			}
		}

		return new RegionMap(map, labels);
	}

}
