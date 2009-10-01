/**
 * =================================================================================
 *
 * BSD LICENCE (http://en.wikipedia.org/wiki/BSD_licenses)
 *
 * ARTIFACT='barchart-udt4'.VERSION='1.0.0-SNAPSHOT'.TIMESTAMP='2009-09-20_18-55-32'
 *
 * Copyright (C) 2009, Barchart, Inc. (http://www.barchart.com/)
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
package com.barchart.udt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * CONTRACT:
 * <p>
 * 1) expecting to find native libraries under these names in the root of class
 * path or jar file which contains this class
 * <p>
 * 2) *.dll/*.so element is extracted, copied AND loaded
 * <p>
 * 3) other array elements are extracted and copied ONLY
 * <p>
 * .
 */
public enum LibraryUDT {

	/** The UNKNOWN. */
	UNKNOWN(new String[] { "UNKNOWN" }), //

	/** The WINDOWS_32 library entry. */
	WINDOWS_32(new String[] { "mingwm10.dll", "SocketUDT-windows-x86-32.dll",
			"LICENCE_BARCHART.txt" }), // 

	/** The WINDOWS_64 library entry. */
	WINDOWS_64(new String[] { "SocketUDT-windows-x86-64.dll",
			"LICENCE_BARCHART.txt" }), //

	/** The LINUX_32 library entry. */
	LINUX_32(new String[] { "libSocketUDT-linux-x86-32.so",
			"LICENCE_BARCHART.txt" }), // 

	/** The LINUX_64 library entry. */
	LINUX_64(new String[] { "libSocketUDT-linux-x86-64.so",
			"LICENCE_BARCHART.txt" }), //

	;

	/** The file name array. */
	public final String[] fileNameArray;

	/**
	 * Instantiates a new library udt.
	 * 
	 * @param fileNameArray
	 *            the file name array
	 */
	private LibraryUDT(String[] fileNameArray) {
		this.fileNameArray = fileNameArray;
	}

	/** The log. */
	private final static Logger log = LoggerFactory.getLogger(LibraryUDT.class);

	/** The Constant OS_NAME. */
	public final static String OS_NAME = System.getProperty("os.name")
			.toLowerCase();

	/** The Constant OS_ARCH. */
	public final static String OS_ARCH = System.getProperty("os.arch")
			.toLowerCase();

	/**
	 * Detect on which architecture we are running.
	 * 
	 * @return the library udt
	 */
	public static LibraryUDT detect() {
		if (OS_NAME.contains("windows")) {
			if (OS_ARCH.contains("x86")) {
				LibraryUDT library = WINDOWS_32;
				log.debug("detected: library={}", library);
				return library;
			}
			if (OS_ARCH.contains("amd64")) {
				LibraryUDT library = WINDOWS_64;
				log.debug("detected: library={}", library);
				return library;
			}
		}
		if (OS_NAME.contains("linux")) {
			if (OS_ARCH.contains("i386") || OS_ARCH.contains("i586")
					|| OS_ARCH.contains("i686")) {
				LibraryUDT library = LINUX_32;
				log.debug("detected: library={}", library);
				return library;
			}
			if (OS_ARCH.contains("amd64") || OS_ARCH.contains("x86_64")) {
				LibraryUDT library = LINUX_64;
				log.debug("detected: library={}", library);
				return library;
			}
		}
		log.error("unsupported OS_NAME={} OS_ARCH={}", OS_NAME, OS_ARCH);
		return UNKNOWN;
	}

	/** The Constant DEFAULT_EXTRACT_FOLDER_NAME. */
	public final static String DEFAULT_EXTRACT_FOLDER_NAME = "./lib";

	/**
	 * can specify optional libraries unpack location folder.
	 * 
	 * @param folderName
	 *            the folder name
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public static void load(String folderName) throws Exception {

		if (folderName == null || folderName.length() == 0) {
			folderName = DEFAULT_EXTRACT_FOLDER_NAME;
			log.warn("using default folderName={}", folderName);
		}

		//

		File targetFolder = new File(folderName);

		if (targetFolder.exists()) {
			if (targetFolder.isDirectory()) {
				log.debug("found folder={}", targetFolder);
			} else {
				log.error("not a directory; folder={}", targetFolder);
				throw new IllegalArgumentException(
						"destination exists, but as file and not a folder");
			}
		} else {
			targetFolder.mkdirs();
			log.debug("made folder={}", targetFolder);
		}

		//

		LibraryUDT library = detect();

		if (library == UNKNOWN) {
			throw new UnsupportedOperationException(
					"this platform is not supported");
		}

		if (library.fileNameArray.length == 0) {
			log
					.error("invalid library file name array for library={}",
							library);
			throw new IllegalArgumentException("invalid name array");
		}

		for (String fileName : library.fileNameArray) {

			log.debug("using: targetFolder={} fileName={}", //
					targetFolder, fileName);

			extractFile(targetFolder, fileName);

			if (isLibraryExtension(fileName)) {
				systemLoad(targetFolder, fileName);
			}

		}

	}

	private static boolean isLibraryExtension(String fileName) {
		fileName = fileName.toLowerCase();
		if (fileName.endsWith(".dll")) {
			return true;
		}
		if (fileName.endsWith(".so")) {
			return true;
		}
		return false;
	}

	private static void systemLoad(File targetFolder, String fileName) {

		String absolutePath = //
		targetFolder.getAbsolutePath() + File.separator + fileName;

		try {
			System.load(absolutePath);
		} catch (Exception e) {
			log.error("load failed : path={} message={}", //
					absolutePath, e.getMessage());
			throw new UnsupportedOperationException(
					"native library load failed");
		}

	}

	/** The Constant EOF. */
	private final static int EOF = -1;

	// note: expecting resources in the the root of class path / jar file
	/**
	 * Extract.
	 * 
	 * @param folder
	 *            the folder
	 * @param fileName
	 *            the file name
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private static void extractFile(File folder, String fileName)
			throws Exception {

		ClassLoader classLoader = LibraryUDT.class.getClassLoader();

		if (classLoader == null) {
			log.error("resource classLoader not available: {}", fileName);
			throw new IllegalArgumentException("resource not found");
		}

		// no root "/" prefix needed for this call
		URL urlIN = classLoader.getResource(fileName);

		if (urlIN == null) {
			log.error("resource url not found: {}", fileName);
			throw new IllegalArgumentException("resource not found");
		}

		log.debug("urlIN={} ", urlIN);

		URLConnection connIN = urlIN.openConnection();

		if (connIN == null) {
			log.error("resource connection not available: {}", fileName);
			throw new IllegalArgumentException("resource not found");
		}

		File fileOUT = new File(folder, fileName);
		log.debug("fileOUT={} ", fileOUT.getAbsolutePath());

		if (isSameFile(connIN, fileOUT)) {
			log.debug("already extracted");
			return;
		} else {
			log.debug("making new destination resource for extraction");
			fileOUT.delete();
			fileOUT.createNewFile();
			// continue
		}

		final long timeStamp = timeStamp(connIN);

		InputStream streamIN = new BufferedInputStream(//
				classLoader.getResourceAsStream(fileName));

		OutputStream streamOUT = new BufferedOutputStream(//
				new FileOutputStream(fileOUT));

		byte[] array = new byte[64 * 1024];

		int readCount = 0;

		while ((readCount = streamIN.read(array)) != EOF) {
			streamOUT.write(array, 0, readCount);
		}

		streamIN.close();

		streamOUT.close();

		// synchronize target time stamp with source to avoid repeated copy
		fileOUT.setLastModified(timeStamp);

		log.debug("resource extracted OK");

	}

	/**
	 * Time stamp.
	 * 
	 * @param connIN
	 *            the conn in
	 * 
	 * @return the long
	 */
	private static long timeStamp(URLConnection connIN) {
		// will use time stamp of jar file
		return connIN.getLastModified();
	}

	/**
	 * Checks if is same file.
	 * 
	 * @param connIN
	 *            the conn in
	 * @param fileOUT
	 *            the file out
	 * 
	 * @return true, if is same file
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private static boolean isSameFile(URLConnection connIN, File fileOUT)
			throws Exception {

		long stampIN = connIN.getLastModified();
		long lengthIN = connIN.getContentLength();

		long stampOut = fileOUT.lastModified();
		long lengthOut = fileOUT.length();

		return lengthIN == lengthOut && stampIN == stampOut;

	}

}
