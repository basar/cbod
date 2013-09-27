package net.bsrc.cbod.pascal.xml;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class PascalXMLHelper {

	private static XStream xstream = null;

	private static XStream getXStream() {
		if (xstream == null) {
			synchronized (XStream.class) {
				if (xstream == null) {
					xstream = new XStream() {
						@Override
						protected MapperWrapper wrapMapper(MapperWrapper next) {
							return new MapperWrapper(next) {
								@Override
								public boolean shouldSerializeMember(
										@SuppressWarnings("rawtypes") Class definedIn,
										String fieldName) {
									if (definedIn == Object.class) {
										return false;
									}
									return super.shouldSerializeMember(
											definedIn, fieldName);
								}
							};
						}
					};
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
