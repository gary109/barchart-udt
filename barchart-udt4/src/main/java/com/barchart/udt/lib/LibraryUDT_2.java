package com.barchart.udt.lib;

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

	static final String DOT = ".";
	static final String BAR = "/";
	static final String DASH = "-";
	static final String LIB = "lib";
	static final String JNI = "jni";

	/** The Constant DEFAULT_EXTRACT_FOLDER_NAME. */
	public final static String DEFAULT_EXTRACT_FOLDER_NAME = DOT + BAR + LIB;

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

		final String targetPath = lib.targetPath(targetFolder);

		try {
			/** load CDT testing library; produced by local interactive build */
			final String sourcePath = lib.sourceTestCDT();
			load(sourcePath, targetPath);
			return;
		} catch (Exception e) {
			log.warn("{}", e.getMessage());
		}

		try {
			/** load NAR testing library; produced by local interactive build */
			final String sourcePath = lib.sourceTestNAR();
			load(sourcePath, targetPath);
			return;
		} catch (Exception e) {
			log.warn("{}", e.getMessage());
		}

		try {
			/** load NAR production library; produced by remote hudson build */
			final String sourcePath = lib.sourceRealNAR();
			load(sourcePath, targetPath);
			return;
		} catch (Exception e) {
			log.warn("{}", e.getMessage());
		}

		throw new Exception("load failed");

	}

	/**
	 * testing: custom CDT name convention; produced by CDT interactive build
	 * and moved to the target/test-classes/
	 */
	// example:
	// ./libbarchart-i386-Linux-g++.so
	String sourceTestCDT() {
		final String classifier = aol.resourceName();
		final String name = VersionUDT.BARCHART_ARTIFACT + DASH + classifier;
		final String library = System.mapLibraryName(name);
		final String path = //
		DOT + BAR + library;
		return path;
	}

	/**
	 * testing: maven-nar-plugin name convention; custom location:
	 * target/test-classes; part of java test classpath
	 */
	// example:
	// ./libbarchart-i386-Linux-g++-jni/./lib/i386-Linux-g++/jni/libbarchart-1.0.2-SNAPSHOT.so
	String sourceTestNAR() {
		final String classifier = aol.resourceName();
		final String name = VersionUDT.BARCHART_NAME;
		final String folder = name + DASH + classifier + JNI;
		final String path = //
		DOT + BAR + folder + BAR + sourceRealNAR();
		return path;
	}

	/**
	 * production: maven-nar-plugin name convention; production location; part
	 * of production java classpath
	 */
	// example:
	// ./lib/i386-Linux-g++/jni/libbarchart-1.0.2-SNAPSHOT.so
	String sourceRealNAR() {
		final String name = VersionUDT.BARCHART_NAME;
		final String classifier = aol.resourceName();
		final String library = System.mapLibraryName(name);
		final String path = //
		DOT + BAR + LIB + BAR + classifier + BAR + JNI + BAR + library;
		return path;
	}

	/**
	 * both testing and production: location and naming for extracted library;
	 * note that source artifact in jar (or on classpath) and the extracted
	 * artifact (on file system) will have different names
	 */
	// example:
	// ./lib/libbarchart-1.0.2-SNAPSHOT-i386-Linux-g++.so
	String targetPath(final String targetFolder) {
		final String classifier = aol.resourceName();
		final String name = VersionUDT.BARCHART_NAME + DASH + classifier;
		final String library = System.mapLibraryName(name);
		final String path = //
		targetFolder + BAR + library;
		return path;
	}

	static void load(final String sourcePath, final String targetPath)
			throws Exception {
		LOAD.extractResource(sourcePath, targetPath);
		final String loadPath = (new File(targetPath)).getAbsolutePath();
		System.load(loadPath);
	}

}
