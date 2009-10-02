package com.barchart.udt;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMonitorUDT {

	private static final Logger log = LoggerFactory
			.getLogger(TestMonitorUDT.class);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testToString() {

		try {

			SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

			MonitorUDT montitor = socket.monitor;

			log.info("montitor={}", montitor);

		} catch (Exception e) {
			fail(e.getMessage());
		}

	}

}
