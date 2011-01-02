package com.barchart.udt.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NAR {

	/** The log. */
	private final static Logger log = LoggerFactory.getLogger(NAR.class);

	/** The Constant OS_NAME. */
	public final static String OS_NAME = System.getProperty("os.name");

	/** The Constant OS_ARCH. */
	public final static String OS_ARCH = System.getProperty("os.arch");

	static String readFileAsString(String filePath) throws Exception {

		StringBuffer fileData = new StringBuffer(1024 * 4);

		InputStream stream = NAR.class.getResourceAsStream(filePath);

		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));

		char[] buf = new char[1024];

		int numRead = 0;

		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}

		reader.close();

		return fileData.toString();

	}

	static String filterOsName() {
		if (OS_NAME.contains("mac")) {
			return "MacOSX";
		}
		return OS_NAME;
	}

	static String filterOsArch() {
		return OS_ARCH;
	}

	static final String NAR_AOL = "/nar-aol.properties";

	public static void main(String... args) throws Exception {

		log.info("started");

		String filePath = NAR_AOL;

		String narAol = readFileAsString(filePath);

		log.info("\n{}", narAol);

		String[] lines = narAol.split("\n");

		log.info("lines.length={}", lines.length);

		Set<String> set = new HashSet<String>();

		for (String line : lines) {

			line = line.trim();

			if (line.startsWith("#") || line.length() < 1) {
				continue;
			}

			String[] entry = line.split("=");
			String[] terms = entry[0].split("\\.");
			for (String term : terms) {
				// log.info("term={}", term);
			}

			if (terms[2].contains("linker")) {
				continue;
			}

			String name = terms[0] + "-" + terms[1] + "-" + terms[2];

			set.add(name);

		}

		for (String line : set.toArray(new String[] {})) {

			String find = filterOsArch() + "-" + filterOsName();

			if (line.contains(find)) {
				log.info("{}", line);
			}

		}

	}

}
