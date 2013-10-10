package net.bsrc.cbod.core.util;

import net.bsrc.cbod.core.CBODConstants;

import org.apache.commons.configuration.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Project configuration utilities
 * 
 * @author bsr
 * 
 */
public final class ConfigurationUtil {

	private static final Logger logger = LoggerFactory
			.getLogger(ConfigurationUtil.class);

	private static CompositeConfiguration compositeConfig = null;

	/**
	 * To prevent object creation
	 */
	private ConfigurationUtil() {

	}

	private static Configuration getConfiguration() {
		if (compositeConfig == null) {
			synchronized (ConfigurationUtil.class) {
				if (compositeConfig == null) {
					compositeConfig = createCompositeConfiguration();
				}
			}
		}
		return compositeConfig;
	}

	private static CompositeConfiguration createCompositeConfiguration() {

		CompositeConfiguration config = new CompositeConfiguration();

		try {
			config.addConfiguration(new SystemConfiguration());
			//
			String targetEnvValue = config
					.getString(CBODConstants.TARGET_ENV_KEY);

			if (!StringUtils.isEmpty(targetEnvValue)) {
				String targetProject = StringUtils.replace(
						CBODConstants.PROJECT_TARGET_ENV_PROPERTIES, "${"
								+ CBODConstants.TARGET_ENV_KEY + "}",
						targetEnvValue);

				config.addConfiguration(new PropertiesConfiguration(
						CBODConstants.CONFIG_BASE + targetProject));
			}

			config.addConfiguration(new PropertiesConfiguration(
					CBODConstants.CONFIG_BASE
							+ CBODConstants.PROJECT_PROPERTIES));

		} catch (ConfigurationException e) {
			logger.error("Error!!:", e);
		}

		return config;
	}

	public static String getString(String key) {
		return getConfiguration().getString(key);
	}

	public static Object getProperty(String key) {
		return getConfiguration().getProperty(key);
	}

}
