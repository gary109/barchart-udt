package com.barchart.udt.util;

class AOL {

	final String arch;
	final String os;
	final String linker;

	AOL(String line) {

		String[] entry = line.split("=");
		String[] terms = entry[0].split("\\.");

		arch = terms[0];
		os = terms[1];
		linker = terms[2];

	}

	String propertyKey() {
		return arch + "." + os + "." + linker;
	}

	String folderName() {
		return arch + "-" + os + "-" + linker;
	}

}