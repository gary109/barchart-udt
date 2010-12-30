package com.barchart.udt;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestStatusUDT {

	@Test
	public void testFromCode() {

		assertEquals(StatusUDT.UNKNOWN, StatusUDT.fromCode(-1));

		assertEquals(StatusUDT.INIT, StatusUDT.fromCode(1));
		assertEquals(StatusUDT.OPENED, StatusUDT.fromCode(2));
		assertEquals(StatusUDT.LISTENING, StatusUDT.fromCode(3));
		assertEquals(StatusUDT.CONNECTED, StatusUDT.fromCode(4));
		assertEquals(StatusUDT.BROKEN, StatusUDT.fromCode(5));
		assertEquals(StatusUDT.CLOSED, StatusUDT.fromCode(6));

	}

	@Test
	public void testSocketStatus() throws ExceptionUDT {

	}

}
