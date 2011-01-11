/**
 * =================================================================================
 *
 * BSD LICENCE (http://en.wikipedia.org/wiki/BSD_licenses)
 *
 * ARTIFACT='barchart-udt4';VERSION='1.0.2-SNAPSHOT';TIMESTAMP='2011-01-11_09-30-59';
 *
 * Copyright (C) 2009-2011, Barchart, Inc. (http://www.barchart.com/)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *     * Neither the name of the Barchart, Inc. nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Developers: Andrei Pozolotin;
 *
 * =================================================================================
 */
package com.barchart.udt.lib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
 enum LibraryUDT_2 {

	UNKNOWN("xxx.xxx.xxx"), //

	I386_LINUX_GPP("i386.Linux.g++"), //

	AMD64_LINUX_GPP("amd64.Linux.g++"), //

	I386_MACOSX_GPP("i386.MacOSX.g++"), //

	X86_64_MACOSX_GPP("x86_64.MacOSX.g++"), //

	X86_WINDOWS_MSVC("x86.Windows.msvc"), //
	// X86_WINDOWS_GPP("x86.Windows.g++"), //

	X86_64_WINDOWS_MSVC("x86_64.Windows.msvc"), //
	// X86_64_WINDOWS_GPP("x86_64.Windows.g++"), //

	;

	private final static Logger log = LoggerFactory
			.getLogger(LibraryUDT_2.class);

	static final String DOT = ".";
	static final String SLASH = "/";
	static final String DASH = "-";
	static final String LIB = "lib";
	static final String JNI = "jni";

	/** The Constant DEFAULT_EXTRACT_FOLDER_NAME. */
	public final static String DEFAULT_EXTRACT_FOLDER_NAME = DOT + SLASH + LIB;

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

		RES.makeTargetFolder(targetFolder);

		final LibraryUDT_2 library = detectLibrary();

		final String targetPath = library.targetPath(targetFolder);

		try {
			log.info("source #1: CDT testing library");
			final String sourcePath = library.sourceTestCDT();
			RES.systemLoad(sourcePath, targetPath);
			return;
		} catch (Exception e) {
			log.warn("\n\t {} {}", e.getClass().getSimpleName(), e.getMessage());
		}

		try {
			log.info("source #2: NAR testing library");
			final String sourcePath = library.sourceTestNAR();
			RES.systemLoad(sourcePath, targetPath);
			return;
		} catch (Exception e) {
			log.warn("\n\t {} {}", e.getClass().getSimpleName(), e.getMessage());
		}

		try {
			log.info("source #3: NAR production library");
			final String sourcePath = library.sourceRealNAR();
			RES.systemLoad(sourcePath, targetPath);
			return;
		} catch (Exception e) {
			log.warn("\n\t {} {}", e.getClass().getSimpleName(), e.getMessage());
		}

		throw new Exception("load failed");

	}

	/**
	 * testing: custom CDT name convention; produced by CDT interactive build
	 * and moved to the target/test-classes/ to make it part of test classpath
	 */
	// example:
	// /libbarchart-i386-Linux-g++.so
	String sourceTestCDT() {
		final String classifier = aol.resourceName();
		final String name = VersionUDT.BARCHART_ARTIFACT + DASH + classifier;
		final String library = System.mapLibraryName(name);
		final String path = //
		SLASH + library;
		return path;
	}

	/**
	 * testing: maven-nar-plugin name convention; custom location:
	 * target/test-classes; part of java test classpath
	 */
	// example:
	// /libbarchart-i386-Linux-g++-jni/./lib/i386-Linux-g++/jni/libbarchart-1.0.2-SNAPSHOT.so
	String sourceTestNAR() {
		final String classifier = aol.resourceName();
		final String name = VersionUDT.BARCHART_NAME;
		final String folder = name + DASH + classifier + DASH + JNI;
		final String path = //
		SLASH + folder + SLASH + sourceRealNAR();
		return path;
	}

	/**
	 * production: maven-nar-plugin name convention; production location; part
	 * of production java classpath
	 */
	// example:
	// /lib/i386-Linux-g++/jni/libbarchart-1.0.2-SNAPSHOT.so
	String sourceRealNAR() {
		final String name = VersionUDT.BARCHART_NAME;
		final String classifier = aol.resourceName();
		final String library = System.mapLibraryName(name);
		final String path = //
		SLASH + LIB + SLASH + classifier + SLASH + JNI + SLASH + library;
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
		targetFolder + SLASH + library;
		return path;
	}

}
