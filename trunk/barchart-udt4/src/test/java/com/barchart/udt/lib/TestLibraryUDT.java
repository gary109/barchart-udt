package com.barchart.udt.lib;

import static com.barchart.udt.lib.LibraryUDT.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.barchart.udt.HelperTestUtilities;

public class TestLibraryUDT {

	static {

		HelperTestUtilities.logOsArch();
		HelperTestUtilities.logClassPath();
		HelperTestUtilities.logLibraryPath();

	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLoadString() throws Exception {

		String targetFolder = "./target/test-lib-3";

		LibraryUDT.load(targetFolder);

		assertTrue(true);

	}

	@Test
	public void testPath() {

		assertEquals(I386_LINUX_GPP.sourceLibRealNAR(),//
				"/lib/i386-Linux-g++/jni/" + coreName());

		assertEquals(I386_LINUX_GPP.targetResPath("./lib", coreName()),
				"./lib/i386-Linux-g++/" + coreName());

		//

		assertEquals(X86_WINDOWS_GPP.sourceDepTestNAR("nar.dll"),
				"/aol/x86-Windows-g++/lib/nar.dll");

	}

}
