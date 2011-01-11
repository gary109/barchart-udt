package com.barchart.udt.lib;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.barchart.udt.HelperTestUtilities;

public class TestLibraryUDT_2 {

	@Before
	public void setUp() throws Exception {
		HelperTestUtilities.logOsArch();
		HelperTestUtilities.logClassPath();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLoad() throws Exception {

		String targetFolder = "./target/test-lib-2";

		LibraryUDT_2.load(targetFolder);

		assertTrue(true);

	}

}
