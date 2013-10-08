package net.bsrc.cbod.core.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * External process operations
 * 
 * @see "http://www.rgagnon.com/javadetails/java-0014.html"
 * @author bsr
 * 
 */
public class ProcessUtil {

	public static void execute(String param) {

		try {

			Process p = Runtime.getRuntime().exec(param);
			BufferedReader bri = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			BufferedReader bre = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));
			String line;
			while ((line = bri.readLine()) != null) {
				System.out.println(line);
			}
			bri.close();
			while ((line = bre.readLine()) != null) {
				System.out.println(line);
			}
			bre.close();
			p.waitFor();
			System.out.println("Done.");

		} catch (Exception e) {
			// TODO logger yaz
			e.printStackTrace();
		}

	}

}
