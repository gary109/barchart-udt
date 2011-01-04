package com.barchart.udt.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum LIB {

	UNKNOWN("xxx.xxx.xxx"), //

	I386_LINUX_GPP("i386.Linux.g++"), //

	AMD64_LINUX_GPP("amd64.Linux.g++"), //

	I386_MACOSX_GPP("i386.MacOSX.g++"), //

	AMD64_MACOSXGPP("amd64.MacOSX.g++"), //

	X86_WINDOWS_GPP("x86.Windows.g++"), //

	X86_32_WINDOWS_GPP("x86_32.Windows.g++"), //

	;

	private final static Logger log = LoggerFactory.getLogger(LIB.class);

	/** The Constant DEFAULT_EXTRACT_FOLDER_NAME. */
	public final static String DEFAULT_EXTRACT_FOLDER_NAME = "./lib";

	private final AOL aol;

	LIB(final String aolKey) {
		this.aol = new AOL(aolKey);
	}

	static LIB detect() {
		for (final LIB lib : values()) {
			if (lib.aol.isMatchJVM()) {
				return lib;
			}
		}
		return UNKNOWN;
	}

	public static void load() throws Exception {
		load(null);
	}

	public static void load(/* non-final */String targetFolder)
			throws Exception {

		if (targetFolder == null || targetFolder.length() == 0) {
			targetFolder = DEFAULT_EXTRACT_FOLDER_NAME;
			log.warn("using default targetFolder={}", targetFolder);
		}

		try {
			loadTest(targetFolder);
			return;
		} catch (Exception e) {
			log.warn("", e);
		}

		try {
			loadReal(targetFolder);
			return;
		} catch (Exception e) {
			log.error("", e);
		}

		throw new Exception("load failed");

	}

	// testing
	String sourceCDT() {
		String name = aol.resourceName();
		String library = System.mapLibraryName(name);
		String path = "./" + library;
		return path;
	}

	// production
	String sourceNAR() {
		String name = Version.BARCHART_NAME;
		String folder = aol.resourceName();
		String library = System.mapLibraryName(name);
		String path = "./lib/" + folder + "/jni/" + library;
		return path;
	}

	String targetLIB(final String targetFolder) {
		String name = Version.BARCHART_NAME + "-" + aol.resourceName();
		String library = System.mapLibraryName(name);
		String path = targetFolder + "/" + library;
		return path;
	}

	public static void loadTest(final String targetFolder) throws Exception {
		LIB lib = detect();
		String sourcePath = lib.sourceCDT();
		String targetPath = lib.targetLIB(targetFolder);
		LOAD.extractResource(sourcePath, targetPath);
		System.load(targetPath);
	}

	public static void loadReal(final String targetFolder) throws Exception {
		LIB lib = detect();
		String sourcePath = lib.sourceNAR();
		String targetPath = lib.targetLIB(targetFolder);
		LOAD.extractResource(sourcePath, targetPath);
		System.load(targetPath);
	}

}
