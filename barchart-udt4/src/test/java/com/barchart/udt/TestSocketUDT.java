/**
 * =================================================================================
 *
 * BSD LICENCE (http://en.wikipedia.org/wiki/BSD_licenses)
 *
 * ARTIFACT='barchart-udt4'.VERSION='1.0.0-SNAPSHOT'.TIMESTAMP='2009-09-09_23-19-15'
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

import static org.junit.Assert.*;

import java.net.InetSocketAddress;
import java.net.SocketException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSocketUDT {

	Logger log = LoggerFactory.getLogger(TestSocketUDT.class);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetOption() {

		try {

			SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

			OptionUDT option;

			option = OptionUDT.UDP_RCVBUF;
			int intValue = 123456789;
			socket.setOption(option, intValue);
			assertEquals(intValue, socket.getOption(option));

			log.info("int pass.");

			option = OptionUDT.UDT_SNDSYN;
			boolean booleanValue = true;
			socket.setOption(option, booleanValue);
			assertEquals(booleanValue, socket.getOption(option));

			log.info("boolean pass");

			option = OptionUDT.UDT_MAXBW;
			long longValue = 1234567890123456789L;
			socket.setOption(option, longValue);
			assertEquals(longValue, socket.getOption(option));

			log.info("long pass");

			option = OptionUDT.UDT_LINGER;
			LingerUDT linger1 = new LingerUDT(1);
			socket.setOption(option, linger1);
			assertEquals(linger1, socket.getOption(option));
			LingerUDT linger2 = new LingerUDT(-1);
			socket.setOption(option, linger2);
			assertEquals(new LingerUDT(0), socket.getOption(option));

			log.info("linger pass");

		} catch (SocketException e) {
			fail("SocketException; " + e.getMessage());
		}
	}

	@Test
	public void testSelectEx0() {

		log.info("testSelectEx0");

		try {

			InetSocketAddress localAddress1 = new InetSocketAddress(//
					"localhost", 9001);

			InetSocketAddress localAddress2 = new InetSocketAddress(//
					"localhost", 9002);

			SocketUDT socketServer = new SocketUDT(TypeUDT.DATAGRAM);
			socketServer.setOption(OptionUDT.UDT_RCVSYN, false);
			socketServer.setOption(OptionUDT.UDT_SNDSYN, false);
			socketServer.bind(localAddress1);
			socketServer.listen(1);
			// socketServer.accept();

			SocketUDT socketClient = new SocketUDT(TypeUDT.DATAGRAM);
			socketClient.setOption(OptionUDT.UDT_RCVSYN, false);
			socketClient.setOption(OptionUDT.UDT_SNDSYN, false);
			socketClient.bind(localAddress2);
			socketClient.listen(1);
			// socketClient.accept();

			long timeout = 1 * 1000 * 1000;

			SocketUDT[] selectArray = new SocketUDT[] { socketServer,
					socketClient };

			socketServer.clearError();

			long timeStart = System.currentTimeMillis();

			// SocketUDT.selectExtended(selectArray, timeout);

			long timeFinish = System.currentTimeMillis();

			long timeDiff = timeFinish - timeStart;
			log.info("timeDiff={}", timeDiff);

			// log.info("isSelectedRead={}", socketServer.isSelectedRead());
			// log.info("isSelectedWrite={}", socketServer.isSelectedWrite());
			// log.info("isSelectedException={}", socketServer
			// .isSelectedException());

			log.info("getError={}", socketServer.getError());
			log.info("getErrorCode={}", socketServer.getErrorCode());
			log.info("getgetErrorMessage={}", socketServer.getErrorMessage());

			socketServer.close();
			socketClient.close();

		} catch (Exception e) {
			fail("SocketException; " + e.getMessage());
		}

	}

	@Test(expected = ExceptionUDT.class)
	public void testInvalidClose0() throws ExceptionUDT {

		SocketUDT socket = null;

		try {
			socket = new SocketUDT(TypeUDT.DATAGRAM);
		} catch (ExceptionUDT e) {
			fail("SocketException; " + e.getMessage());
		}

		int socketID = socket.socketID;

		socketID += 10;

		socket.testInvalidClose0(socketID);

	}

	@Test
	public void testIsOpen() {
		try {

			SocketUDT socket = null;

			socket = new SocketUDT(TypeUDT.DATAGRAM);
			assertTrue(socket.isOpen());

			socket.setOption(OptionUDT.Is_Receive_Synchronous, false);
			socket.setOption(OptionUDT.Is_Send_Synchronous, false);
			assertTrue(socket.isOpen());

			InetSocketAddress localSocketAddress = new InetSocketAddress(
					"0.0.0.0", 0);

			socket.bind(localSocketAddress);
			assertTrue(socket.isOpen());

			socket.listen(128);
			assertTrue(socket.isOpen());

			SocketUDT connector = socket.accept();
			assertNull(connector);
			assertTrue(socket.isOpen());

			socket.close();
			assertFalse(socket.isOpen());
			assertTrue(socket.isClosed());

			log.info("isOpen pass");

		} catch (ExceptionUDT e) {
			fail("SocketException; " + e.getMessage());
		}

	}

}
