package com.barchart.udt.util;

class AOL {

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

		if (arch.equals(filterOsArch()) && os.equals(filterOsName())) {
			return true;
		}

		return false;
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

}