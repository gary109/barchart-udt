package com.barchart.udt.util;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URLConnection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLOAD {

	static final Logger log = LoggerFactory.getLogger(TestLOAD.class);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFileConnection() throws Exception {

		String curDir = System.getProperty("user.dir");
		log.info("curDir: {}", curDir);

		String path = "src/test/resources/test-connection.txt";

		File file = new File(path);
		log.info("exists : {}", file.exists());

		log.info("getAbsolutePath : {}", file.getAbsolutePath());

		URLConnection conn = LOAD.fileConnection(file);

		assertTrue(conn.getLastModified() > 0);
		log.info("getLastModified : {}", conn.getLastModified());

		assertTrue(conn.getContentLength() > 0);
		log.info("getContentLength : {}", conn.getContentLength());

	}

}
