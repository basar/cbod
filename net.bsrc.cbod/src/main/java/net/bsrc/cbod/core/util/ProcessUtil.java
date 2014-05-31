package net.bsrc.cbod.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
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

	public static void execute(String param, File outputFile) {

		try {

			logger.debug(param);
			Process p = Runtime.getRuntime().exec(param);
			BufferedReader bri = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			BufferedReader bre = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));

			String line;
			List<String> lines = new ArrayList<String>();

			while ((line = bri.readLine()) != null) {
				if (outputFile == null) {
					logger.info(line);
				} else {
					lines.add(line);
				}
			}
			bri.close();

			while ((line = bre.readLine()) != null) {
				logger.info(line);
			}
			bre.close();

			p.waitFor();
			p.destroy();

			logger.info("Process finished");

			if (outputFile != null)
				FileUtils.writeLines(outputFile, lines);

		} catch (Exception e) {
			logger.error("", e);
		}

	}

}
