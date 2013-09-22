package net.bsrc.cbod.util;

import net.bsrc.cbod.core.CBODConstants;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.lang.StringUtils;

/**
 * Project configuration utilities
 * 
 * @author bsr
 * 
 */
public final class ConfigurationUtil {

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
			//TODO logger yazilacak!
			e.printStackTrace();
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
