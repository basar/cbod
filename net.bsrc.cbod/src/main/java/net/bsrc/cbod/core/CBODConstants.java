package net.bsrc.cbod.core;

public final class CBODConstants {

	public static final String CONFIG_BASE = "META-INF/config/";
	public static final String PROJECT_PROPERTIES = "project.properties";
	public static final String TARGET_ENV_KEY = "target.env";
	public static final String PROJECT_TARGET_ENV_PROPERTIES = "project.${"
			+ TARGET_ENV_KEY + "}.properties";
	
	/**
	 * To prevent object creation
	 */
	private CBODConstants(){
		
	}
	
}
