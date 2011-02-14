package com.barchart.udt.net;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.util.HelperUtils;

/**
 * Test for UDT socket input streams and output streams.
 */
public class TestUdtInputAndOutputStream {

	private static final int DEFAULT_BUFFER_SIZE = 8000;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private static final byte[] TEST_BYTES = testBytes();

	private static final boolean USE_UDT = true;

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
			// log.info("ReadStrategy::ABOUT TO READ...");
			bytes[off] = (byte) is.read();
			// log.info("ReadStrategy::JUST READ: " + bytes[off]);
			return 1;
		}
	};

	private ReadStrategy activeReadStrategy;

	/**
	 * This test creates a UDT server socket, connects to it, and sends it data.
	 * That data is then echoed back, and the test makes sure the echoed data
	 * equals the original data. It uses UDT input and output streams
	 * throughout.
	 * 
	 * @throws Exception
	 *             If any unexpected error occurs.
	 */
	@Test
	public void testBulkRead() throws Exception {
		activeReadStrategy = bulkReadStrategy;
		genericInputOutputRest(activeReadStrategy);
	}

	@Test
	public void testSingleRead() throws Exception {
		activeReadStrategy = singleReadStrategy;
		genericInputOutputRest(activeReadStrategy);
	}

	private void genericInputOutputRest(final ReadStrategy readStrategy)
			throws Exception {

		final InetSocketAddress serverAddress = HelperUtils
				.getLocalSocketAddress();

		startThreadedServer(serverAddress);

		final Socket clientSocket = new NetSocketUDT();

		final InetSocketAddress clientAddress = HelperUtils
				.getLocalSocketAddress();

		clientSocket.bind(clientAddress);
		assertTrue("Socket not bound!!", clientSocket.isBound());

		clientSocket.connect(serverAddress);
		assertTrue("Socket not connected!", clientSocket.isConnected());

		final OutputStream os = clientSocket.getOutputStream();

		final InputStream bais = new ByteArrayInputStream(TEST_BYTES);

		copy(bais, os);

		log.info("Copied test bytes from TEST_BYTES through client socket");

		final InputStream is = clientSocket.getInputStream();

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		copy(is, baos, TEST_BYTES.length, readStrategy);

		baos.close();
		bais.close();

		final byte[] bytesCopy = baos.toByteArray();

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
		int count = 0;
		int n = 0;
		while (-1 != (n = is.read(buffer))) {
			os.write(buffer, 0, n);
			count += n;
		}
		// log.trace("Wrote " + count + " bytes.");
		return count;
	}

	private interface ReadStrategy {

		int read(InputStream is, byte[] bytes, int off, int len)
				throws IOException;
	}

	private long copy(final InputStream in, final OutputStream out,
			final int originalByteCount, final ReadStrategy readStrategy)
			throws IOException {
		if (originalByteCount < 0) {
			throw new IllegalArgumentException("Invalid byte count: "
					+ originalByteCount);
		}
		final int bufSize;
		if (originalByteCount < DEFAULT_BUFFER_SIZE) {
			bufSize = originalByteCount;
		} else {
			bufSize = DEFAULT_BUFFER_SIZE;
		}
		final byte buffer[] = new byte[bufSize];
		int len = 0;
		long written = 0;
		long byteCount = originalByteCount;
		try {
			while (byteCount > 0) {
				if (byteCount < bufSize) {
					len = readStrategy.read(in, buffer, 0, (int) byteCount);
					// len = in.read(buffer, 0, (int) byteCount);
				} else {
					len = readStrategy.read(in, buffer, 0, bufSize);
					// len = in.read(buffer, 0, bufSize);
				}
				if (len == -1) {
					log.info("Breaking on length = -1");
					break;
				}
				byteCount -= len;
				// log.info("Decrementing byte count by " + len + " to "
				// + byteCount);
				out.write(buffer, 0, len);
				// log.info("WROTE DATA");
				written += len;
			}
			return written;
		} catch (final IOException e) {
			log.debug("Got IOException during copy after writing " + written
					+ " of " + originalByteCount, e);
			e.printStackTrace();
			throw e;
		} catch (final RuntimeException e) {
			log.debug("Runtime error after writing " + written + " of "
					+ originalByteCount, e);
			e.printStackTrace();
			throw e;
		} finally {
			out.flush();
		}
	}

	private void runTestServer(final InetSocketAddress serverAddress)
			throws Exception {

		final ServerSocket acceptorSocket = new NetServerSocketUDT();

		acceptorSocket.bind(serverAddress);
		assertTrue("Acceptor should be bound", acceptorSocket.isBound());

		final Socket connectorSocket = acceptorSocket.accept();

		echo(connectorSocket);

	}

	private void startThreadedServer(final InetSocketAddress serverAddress) {
		final Runnable runner = new Runnable() {

			@Override
			public void run() {
				// startServer();
				try {
					runTestServer(serverAddress);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		};
		final Thread t = new Thread(runner, "test-thread");
		t.setDaemon(true);
		t.start();
		try {
			Thread.sleep(800);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void echo(final Socket connectorSocket) throws IOException {

		final InputStream is = connectorSocket.getInputStream();
		final OutputStream os = connectorSocket.getOutputStream();

		final Runnable runner = new Runnable() {

			@Override
			public void run() {
				try {
					log.info("About to echo bytes back to client");
					copy(is, os, TEST_BYTES.length, activeReadStrategy);
					log.info("DONE COPYING...CLOSING");
					os.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		};

		final Thread dt = new Thread(runner, "Server-Echo-Thread");
		dt.setDaemon(true);
		dt.start();

	}

}
