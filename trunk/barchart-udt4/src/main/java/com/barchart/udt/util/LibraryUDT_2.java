package com.barchart.udt.util;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum LibraryUDT_2 {

	UNKNOWN("xxx.xxx.xxx"), //

	I386_LINUX_GPP("i386.Linux.g++"), //

	AMD64_LINUX_GPP("amd64.Linux.g++"), //

	I386_MACOSX_GPP("i386.MacOSX.g++"), //

	X86_64_MACOSX_GPP("x86_64.MacOSX.g++"), //

	X86_WINDOWS_GPP("x86.Windows.g++"), //

	X86_64_WINDOWS_GPP("x86_64.Windows.g++"), //

	;

	private final static Logger log = LoggerFactory
			.getLogger(LibraryUDT_2.class);

	static final String BAR = "/";;

	/** The Constant DEFAULT_EXTRACT_FOLDER_NAME. */
	public final static String DEFAULT_EXTRACT_FOLDER_NAME = "." + BAR + "lib";

	private final AOL aol;

	LibraryUDT_2(final String aolKey) {
		this.aol = new AOL(aolKey);
	}

	static LibraryUDT_2 detectLibrary() {
		for (final LibraryUDT_2 lib : values()) {
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

		final LibraryUDT_2 lib = detectLibrary();

		try {
			// load CDT testing library
			final String sourcePath = lib.sourceTestCDT();
			loadPath(lib, sourcePath, targetFolder);
			return;
		} catch (Exception e) {
			log.warn("{}", e.getMessage());
		}

		try {
			// load NAR testing library
			final String sourcePath = lib.sourceTestNAR();
			loadPath(lib, sourcePath, targetFolder);
			return;
		} catch (Exception e) {
			log.warn("{}", e.getMessage());
		}

		try {
			// load NAR production library
			final String sourcePath = lib.sourceRealNAR();
			loadPath(lib, sourcePath, targetFolder);
			return;
		} catch (Exception e) {
			log.warn("{}", e.getMessage());
		}

		throw new Exception("load failed");

	}

	/** testing: custom name convention */
	String sourceTestCDT() {
		String name = VersionUDT.BARCHART_ARTIFACT + "-" + aol.resourceName();
		String library = System.mapLibraryName(name);
		String path = "." + BAR + library;
		return path;
	}

	/**
	 * testing: maven-nar-plugin name convention; custom location:
	 * target/test-classes; parth of java test classpath
	 */
	String sourceTestNAR() {
		String name = VersionUDT.BARCHART_NAME;
		String classifier = aol.resourceName();
		String folder = name + "-" + classifier + "-jni";
		String path = "." + BAR + folder + BAR + sourceRealNAR();
		return path;
	}

	/**
	 * production: maven-nar-plugin name convention; production location; part
	 * of production java classpath
	 */
	String sourceRealNAR() {
		String name = VersionUDT.BARCHART_NAME;
		String classifier = aol.resourceName();
		String library = System.mapLibraryName(name);
		String path = "." + BAR + "lib" + BAR + classifier + BAR + "jni" + BAR
				+ library;
		return path;
	}

	String targetLIB(final String targetFolder) {
		String name = VersionUDT.BARCHART_NAME + "-" + aol.resourceName();
		String library = System.mapLibraryName(name);
		String path = targetFolder + BAR + library;
		return path;
	}

	static void loadPath(final LibraryUDT_2 lib, final String sourcePath,
			final String targetFolder) throws Exception {
		final String targetPath = lib.targetLIB(targetFolder);
		LOAD.extractResource(sourcePath, targetPath);
		final String loadPath = (new File(targetPath)).getAbsolutePath();
		System.load(loadPath);
	}

}
