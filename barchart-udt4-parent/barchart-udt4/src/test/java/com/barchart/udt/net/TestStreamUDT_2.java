package com.barchart.udt.net;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.StatusUDT;
import com.barchart.udt.TypeUDT;
import com.barchart.udt.util.HelperUtils;

/**
 * Test for UDT socket input streams and output streams.
 */
public class TestStreamUDT_2 {

	private static final int DEFAULT_BUFFER_SIZE = 8000;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private static final byte[] TEST_BYTES = testBytes();

	private final ReadStrategy bulkReadStrategy = new ReadStrategy() {
		@Override
		public int read(final InputStream is, final byte[] bytes,
				final int off, final int len) throws IOException {

			return is.read(bytes, off, len);

		}
	};

	private final ReadStrategy singleReadStrategy = new ReadStrategy() {
		@Override
		public int read(final InputStream is, final byte[] bytes,
				final int off, final int len) throws IOException {

			log.info("ReadStrategy::ABOUT TO READ...");

			bytes[off] = (byte) is.read();

			log.info("ReadStrategy::JUST READ: " + bytes[off]);

			return 1;

		}
	};

	/**
	 * This test creates a UDT server socket, connects to it, and sends it data.
	 * That data is then echoed back, and the test makes sure the echoed data
	 * equals the original data. It uses UDT input and output streams
	 * throughout.
	 * 
	 * @throws Exception
	 *             If any unexpected error occurs.
	 */
	// @Test
	public void testBulkRead() throws Exception {

		genericInputOutputTest(bulkReadStrategy);

	}

	@Test
	public void testSingleRead() throws Exception {

		genericInputOutputTest(singleReadStrategy);

	}

	private void genericInputOutputTest(final ReadStrategy readStrategy)
			throws Exception {

		Thread.currentThread().setName("### main");

		final InetSocketAddress serverAddress = HelperUtils
				.getLocalSocketAddress();

		startThreadedServer(serverAddress, readStrategy);

		//

		final SocketUDT clientSocket = new SocketUDT(TypeUDT.DATAGRAM);

		final InetSocketAddress clientAddress = HelperUtils
				.getLocalSocketAddress();

		clientSocket.bind(clientAddress);
		assertTrue("Socket not bound!!", clientSocket.isBound());

		clientSocket.connect(serverAddress);
		assertTrue("Socket not connected!", clientSocket.isConnected());

		final InputStream dataIn = new ByteArrayInputStream(TEST_BYTES);

		final OutputStream socketOut = new OutputStreamUDT_2(clientSocket);

		copy(dataIn, socketOut);

		dataIn.close();

		log.info("\n\t### Copied test bytes from TEST_BYTES through client socket");

		final InputStream socketIn = new InputStreamUDT_2(clientSocket);

		final ByteArrayOutputStream dataOut = new ByteArrayOutputStream();

		copy(socketIn, dataOut, TEST_BYTES.length, readStrategy);

		dataOut.close();

		final byte[] bytesCopy = dataOut.toByteArray();

		assertEquals("byte array lengths aren't equal", TEST_BYTES.length,
				bytesCopy.length);

		assertTrue(Arrays.equals(TEST_BYTES, bytesCopy));

		clientSocket.close();

	}

	private static byte[] testBytes() {
		final byte[] data = new byte[20000];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) (i % 127);
		}
		return data;
	}

	private int copy(final InputStream is, final OutputStream os)
			throws IOException {

		if (is == null) {
			throw new NullPointerException("null input stream.");
		}

		if (os == null) {
			throw new NullPointerException("null output stream.");
		}

		final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

		int countTotal = 0;
		int countRead = 0;

		while (-1 != (countRead = is.read(buffer))) {
			os.write(buffer, 0, countRead);
			countTotal += countRead;
		}

		log.info("Wrote " + countTotal + " bytes.");

		return countTotal;

	}

	private interface ReadStrategy {

		int read(InputStream is, byte[] bytes, int off, int len)
				throws IOException;
	}

	private long copy(final InputStream is, final OutputStream os,
			final int copyTotal, final ReadStrategy readStrategy)
			throws IOException {

		if (copyTotal < 0) {
			throw new IllegalArgumentException("Invalid byte count: "
					+ copyTotal);
		}

		final int arraySize;

		if (copyTotal < DEFAULT_BUFFER_SIZE) {
			arraySize = copyTotal;
		} else {
			arraySize = DEFAULT_BUFFER_SIZE;
		}

		final byte array[] = new byte[arraySize];

		int readCount = 0;
		int writeCount = 0;

		int pendingCount = copyTotal;

		try {

			while (pendingCount > 0) {

				log.info("IN LOOP; pendingCount={}", pendingCount);

				if (pendingCount < arraySize) {
					readCount = readStrategy.read(is, array, 0, pendingCount);
					// len = in.read(buffer, 0, (int) byteCount);
				} else {
					readCount = readStrategy.read(is, array, 0, arraySize);
					// len = in.read(buffer, 0, bufSize);
				}

				assert readCount > 0;

				assert readCount <= arraySize;

				pendingCount -= readCount;

				log.info("Decrementing; readCount=" + readCount
						+ " pendingCount=" + pendingCount);

				os.write(array, 0, readCount);

				log.info("WROTE DATA");

				writeCount += readCount;

			}

			return writeCount;

		} catch (final IOException e) {
			log.debug("Got IOException during copy after writing " + writeCount
					+ " of " + copyTotal, e);
			e.printStackTrace();
			throw e;
		} catch (final RuntimeException e) {
			log.debug("Runtime error after writing " + writeCount + " of "
					+ copyTotal, e);
			e.printStackTrace();
			throw e;
		} finally {
			os.flush();
		}

	}

	private void runTestServer(final InetSocketAddress serverAddress,
			final ReadStrategy readStrategy) throws Exception {

		final SocketUDT acceptorSocket = new SocketUDT(TypeUDT.DATAGRAM);

		acceptorSocket.bind(serverAddress);
		assertTrue("Acceptor should be bound", acceptorSocket.isBound());

		acceptorSocket.listen(1);
		assertEquals("Acceptor should be listenin", acceptorSocket.getStatus(),
				StatusUDT.LISTENING);

		final SocketUDT connectorSocket = acceptorSocket.accept();

		echo(connectorSocket, readStrategy);

	}

	private void startThreadedServer(final InetSocketAddress serverAddress,
			final ReadStrategy readStrategy) {

		final Runnable runner = new Runnable() {
			@Override
			public void run() {
				// startServer();
				try {
					runTestServer(serverAddress, readStrategy);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		};

		final Thread t = new Thread(runner, "### test-thread");
		t.setDaemon(true);
		t.start();

		try {
			Thread.sleep(800);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private void echo(final SocketUDT socketUDT,
			final ReadStrategy activeReadStrategy) {

		final InputStream is = new InputStreamUDT_2(socketUDT);

		final OutputStream os = new OutputStreamUDT_2(socketUDT);

		final Runnable runner = new Runnable() {
			@Override
			public void run() {
				try {

					log.info("About to echo bytes back to client");

					copy(is, os, TEST_BYTES.length, activeReadStrategy);

					log.info("DONE COPYING...SLEEPING");

					os.close();

				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		};

		final Thread dt = new Thread(runner, "### Server-Echo-Thread");
		dt.setDaemon(true);
		dt.start();

	}

}
