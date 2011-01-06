package com.barchart.udt.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AOL {

	private final static Logger log = LoggerFactory.getLogger(AOL.class);

	final String arch;
	final String os;
	final String linker;

	/** The Constant OS_NAME. */
	public final static String OS_NAME = System.getProperty("os.name");

	/** The Constant OS_ARCH. */
	public final static String OS_ARCH = System.getProperty("os.arch");

	AOL(final String line) {

		String[] entry = line.split("=");
		String[] terms = entry[0].split("\\.");

		arch = terms[0];
		os = terms[1];
		linker = terms[2];

	}

	String propertyName() {
		return arch + "." + os + "." + linker;
	}

	String resourceName() {
		return arch + "-" + os + "-" + linker;
	}

	boolean isMatchJVM() {

		// log.info("aol;    arch={} os={}", arch, os);
		// log.info("jvm;    arch={} os={}", OS_ARCH, OS_NAME);
		// log.info("filter; arch={} os={}", filterArch(), filterName());

		if (arch.equals(filterArch()) && os.equals(filterName())) {
			return true;
		}

		return false;
	}

	static String filterName() {
		if (OS_NAME.equals("Mac OS X")) {
			return "MacOSX";
		}
		return OS_NAME;
	}

	static String filterArch() {
		return OS_ARCH;
	}

};