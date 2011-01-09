package com.barchart.udt.lib;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestLibraryUDT_2 {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLoad() throws Exception {

		String targetFolder = "./target/test-lib-1";

		LibraryUDT_2.load(targetFolder);

		assertTrue(true);

	}

}
