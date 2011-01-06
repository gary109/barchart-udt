package com.barchart.udt.lib;

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

public class LOAD {

	private final static Logger log = LoggerFactory.getLogger(LOAD.class);

	public static boolean isSameResource(final URLConnection conONE,
			final URLConnection conTWO) throws Exception {

		final long timeONE = conONE.getLastModified();
		final long sizeONE = conONE.getContentLength();

		final long timeTWO = conTWO.getLastModified();
		final long sizeTWO = conTWO.getContentLength();

		return sizeONE == sizeTWO && timeONE == timeTWO;

	}

	public static URLConnection fileConnection(final File file)
			throws Exception {

		final URL url = file.toURI().toURL();

		final URLConnection connection = url.openConnection();

		return connection;

	}

	private final static int EOF = -1;

	private static long timeStamp(URLConnection connIN) {
		// will use time stamp of jar file
		return connIN.getLastModified();
	}

	/** from class path into file system */
	public static void extractResource(final String sourcePath,
			final String targetPath) throws Exception {

		final ClassLoader classLoader = LOAD.class.getClassLoader();

		if (classLoader == null) {
			log.error("resource classLoader not available: {}", sourcePath);
			throw new IllegalArgumentException("resource not found");
		}

		// no root "/" prefix for this call
		final URL sourceUrl = classLoader.getResource(sourcePath);

		if (sourceUrl == null) {
			log.error("resource url not found: {}", sourcePath);
			throw new IllegalArgumentException("resource not found");
		}

		log.debug("sourceURL={} ", sourceUrl);

		final URLConnection sourceConn = sourceUrl.openConnection();

		if (sourceConn == null) {
			log.error("resource connection not available: {}", sourcePath);
			throw new IllegalArgumentException("resource not found");
		}

		final File targetFile = new File(targetPath);
		log.debug("targetFile={} ", targetFile.getAbsolutePath());

		final URLConnection targetConn = fileConnection(targetFile);

		if (isSameResource(sourceConn, targetConn)) {
			log.info("already extracted; sourcePath={}", sourcePath);
			return;
		} else {
			log.debug("making new destination resource for extraction");
			targetFile.delete();
			targetFile.createNewFile();
		}

		final long sourceTime = timeStamp(sourceConn);

		final InputStream sourceStream = new BufferedInputStream(//
				classLoader.getResourceAsStream(sourcePath));

		final OutputStream targetStream = new BufferedOutputStream(//
				new FileOutputStream(targetFile));

		final byte[] array = new byte[64 * 1024];

		int readCount = 0;

		while ((readCount = sourceStream.read(array)) != EOF) {
			targetStream.write(array, 0, readCount);
		}

		sourceStream.close();

		targetStream.close();

		// synchronize target time stamp with source to avoid repeated copy
		targetFile.setLastModified(sourceTime);

		log.info("extracted OK; sourcePath={}", sourcePath);

	}

}
