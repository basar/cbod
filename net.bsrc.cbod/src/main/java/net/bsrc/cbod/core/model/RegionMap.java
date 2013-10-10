package net.bsrc.cbod.core.model;

import java.util.Set;

import org.opencv.core.Mat;
/**
 * 
 * @author bsr
 *
 */
public class RegionMap {

	private Mat map;

	private Set<Integer> labels;

	public RegionMap(Mat map, Set<Integer> labels) {
		this.map = map;
		this.labels = labels;
	}

	public Mat getMap() {
		return map;
	}

	public void setMap(Mat map) {
		this.map = map;
	}

	public Set<Integer> getLabels() {
		return labels;
	}

	public void setLabels(Set<Integer> labels) {
		this.labels = labels;
	}

}
