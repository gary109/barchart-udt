package com.barchart.udt;

import com.barchart.udt.lib.LibraryLoaderDefaultUDT;

public final class ResourceUDT {

	private ResourceUDT() {
		//
	}

	/** */
	public static final String PACKAGE_NAME = //
	SocketUDT.class.getPackage().getName();

	/**
	 * system property which if provided will override
	 * {@link ResourceUDT#DEFAULT_LIBRARY_EXTRACT_LOCATION}
	 */
	public static final String PROPERTY_LIBRARY_EXTRACT_LOCATION = //
	PACKAGE_NAME + ".library.extract.location";

	/** system property which if provided will override default native loader */
	public static final String PROPERTY_LOADER_CLASS_NAME = //
	PACKAGE_NAME + ".loader.class.name";

	/** */
	public static final String DEFAULT_LIBRARY_LOADER_CLASS = //
	LibraryLoaderDefaultUDT.class.getName();

	/**
	 * target destination of native wrapper library *.dll or *.so files that are
	 * extracted from this library jar;
	 */
	public static final String DEFAULT_LIBRARY_EXTRACT_LOCATION = //
	"./lib/bin";

	/***/
	public static void setLibraryExtractLocation(final String location) {
		System.setProperty(PROPERTY_LIBRARY_EXTRACT_LOCATION, location);
	}

	/***/
	public static void setLibraryLoaderClassName(final String className) {
		System.setProperty(PROPERTY_LOADER_CLASS_NAME, className);
	}

}
