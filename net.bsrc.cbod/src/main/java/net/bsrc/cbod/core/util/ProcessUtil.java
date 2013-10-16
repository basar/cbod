package net.bsrc.cbod.core.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * External process operations
 * 
 * @see "http://www.rgagnon.com/javadetails/java-0014.html"
 * @author bsr
 * 
 */
public class ProcessUtil {

	private static final Logger logger = LoggerFactory
			.getLogger(ProcessUtil.class);

	public static void execute(String param) {

		try {

			logger.debug(param);
			Process p = Runtime.getRuntime().exec(param);
			BufferedReader bri = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			BufferedReader bre = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));
			String line;
			while ((line = bri.readLine()) != null) {
				logger.info(line);
			}
			bri.close();
			while ((line = bre.readLine()) != null) {
				logger.info(line);
			}
			bre.close();
			p.waitFor();
			logger.info("Done.");

		} catch (Exception e) {
			logger.error("", e);
		}

	}

}
