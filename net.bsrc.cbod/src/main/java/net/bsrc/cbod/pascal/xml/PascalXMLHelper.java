package net.bsrc.cbod.pascal.xml;

import com.thoughtworks.xstream.XStream;

public class PascalXMLHelper {

	private static XStream xstream = null;

	private static XStream getXStream() {
		if (xstream == null) {
			synchronized (XStream.class) {
				if (xstream == null) {
					xstream = new XStream();
					xstream.processAnnotations(PascalAnnotation.class);
				}
			}
		}
		return xstream;
	}

	public static PascalAnnotation fromXML(String xml) {
		XStream xStream = getXStream();
		return (PascalAnnotation) xStream.fromXML(xml);
	}

}
