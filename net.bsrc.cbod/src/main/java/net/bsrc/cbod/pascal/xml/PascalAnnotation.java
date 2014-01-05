package net.bsrc.cbod.pascal.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.bsrc.cbod.pascal.EPascalType;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.apache.commons.lang.ArrayUtils;

@XStreamAlias("annotation")
public class PascalAnnotation {

	private String folder;

	private String filename;

	private PascalSource source;

	private PascalSize size;

	private String segmented;

	@XStreamImplicit(itemFieldName = "object")
	private List<PascalObject> objectList;

	public PascalAnnotation() {

	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public PascalSource getSource() {
		return source;
	}

	public void setSource(PascalSource source) {
		this.source = source;
	}

	public PascalSize getSize() {
		return size;
	}

	public void setSize(PascalSize size) {
		this.size = size;
	}

	public String getSegmented() {
		return segmented;
	}

	public void setSegmented(String segmented) {
		this.segmented = segmented;
	}

	public List<PascalObject> getObjectList() {
		return objectList;
	}

	public List<PascalObject> getObjectList(EPascalType type) {
		EPascalType[] types = new EPascalType[1];
		types[0] = type;
		return getObjectList(types);
	}

	public List<PascalObject> getObjectList(EPascalType... types) {
		List<PascalObject> result = new ArrayList<PascalObject>();
		for (PascalObject po : getObjectList()) {
			boolean control = false;
			for (EPascalType type : types) {
				if (po.getName() != null && po.getName().equals(type.getName())) {
					control = true;
					break;
				}
			}
			if (control)
				result.add(po);
		}
		return result;

	}

	public void setObjectList(List<PascalObject> objectList) {
		this.objectList = objectList;
	}

	@Override
	public String toString() {
		return "PascalAnnotation [folder=" + folder + ", filename=" + filename
				+ ", source=" + source + ", size=" + size + ", segmented="
				+ segmented + ", objectList=" + objectList + "]";
	}

}
