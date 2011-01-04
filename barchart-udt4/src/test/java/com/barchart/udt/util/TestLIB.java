package com.barchart.udt.util;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestLIB {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLoad() throws Exception {

		LibraryUDT2.load();

		assertTrue(true);

	}

}
