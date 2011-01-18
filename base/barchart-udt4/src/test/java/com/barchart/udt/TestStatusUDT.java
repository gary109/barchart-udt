package com.barchart.udt;

import static org.junit.Assert.*;

import java.net.InetSocketAddress;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestStatusUDT {

	static final Logger log = LoggerFactory.getLogger(TestStatusUDT.class);

	@Before
	public void setUp() throws Exception {

		log.info("started {}", System.getProperty("os.arch"));

	}

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
	public void testSocketStatus1() throws Exception {

		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);
		assertEquals(StatusUDT.INIT, socket.getStatus());

		final InetSocketAddress localAddress1 = new InetSocketAddress(
				"0.0.0.0", 8001);

		socket.bind(localAddress1);
		assertEquals(StatusUDT.OPENED, socket.getStatus());

		socket.close();
		assertEquals(StatusUDT.CLOSED, socket.getStatus());

		log.info("finished");

	}

	@Test
	public void testSocketStatus2() throws Exception {

		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);
		assertEquals(StatusUDT.INIT, socket.getStatus());

		final InetSocketAddress localAddress1 = new InetSocketAddress(
				"0.0.0.0", 8002);

		socket.bind(localAddress1);
		assertEquals(StatusUDT.OPENED, socket.getStatus());

		socket.listen(1);
		assertEquals(StatusUDT.LISTENING, socket.getStatus());

		socket.close();
		assertEquals(StatusUDT.BROKEN, socket.getStatus());

		log.info("finished");

	}

	@Test
	public void testSocketStatus3() throws Exception {

		final InetSocketAddress clientAddress = new InetSocketAddress(
				"0.0.0.0", 8031);

		final InetSocketAddress serverAddress = new InetSocketAddress(
				"0.0.0.0", 8032);

		//

		final SocketUDT client = new SocketUDT(TypeUDT.DATAGRAM);
		assertEquals(StatusUDT.INIT, client.getStatus());

		final SocketUDT server = new SocketUDT(TypeUDT.DATAGRAM);
		assertEquals(StatusUDT.INIT, server.getStatus());

		//

		client.bind(clientAddress);
		assertEquals(StatusUDT.OPENED, client.getStatus());

		server.bind(serverAddress);
		assertEquals(StatusUDT.OPENED, server.getStatus());

		//

		server.listen(10);
		assertEquals(StatusUDT.LISTENING, server.getStatus());

		//

		// final SocketUDT accept = server.accept();

		//

		final Thread serverThread = new Thread() {
			@Override
			public void run() {
				try {

					// final SocketUDT accept = server.accept();
					// assertEquals(StatusUDT.LISTENING, accept.getStatus());

				} catch (Exception e) {
					fail(e.getMessage());
				}
			}
		};

		serverThread.start();

		serverThread.join();

		// Thread.sleep(10 * 1000);

	}

}
