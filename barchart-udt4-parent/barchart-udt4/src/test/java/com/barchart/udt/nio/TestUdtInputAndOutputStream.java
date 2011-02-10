package com.barchart.udt.nio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Arrays;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.nio.InputStreamUDT;
import com.barchart.udt.nio.OutputStreamUDT;
import com.barchart.udt.nio.SelectorProviderUDT;

/**
 * Test for UDT socket input streams and output streams.
 */
public class TestUdtInputAndOutputStream {
	
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
			log.info("ReadStrategy::JUST READ: "+bytes[off]);
			return 1;
		}
	};
	
	private ReadStrategy activeReadStrategy;

	/**
	 * This test creates a UDT server socket, connects to it, and sends it 
	 * data. That data is then echoed back, and the test makes sure the echoed
	 * data equals the original data. It uses UDT input and output streams
	 * throughout.
	 * 
	 * @throws Exception If any unexpected error occurs.
	 */
	@Test 
	public void testBulkRead() throws Exception {
		activeReadStrategy = bulkReadStrategy;
		genericInputOutputRest(activeReadStrategy, 47921);
	}
	
	//@Test TODO: FIX THIS!! 
	public void testSingleRead() throws Exception {
		activeReadStrategy = singleReadStrategy;
		genericInputOutputRest(activeReadStrategy, 6822);
	}
	
	private void genericInputOutputRest(final ReadStrategy readStrategy, 
		final int serverPort) 
		throws Exception {
		final InetSocketAddress serverAddress = 
			new InetSocketAddress("127.0.0.1", serverPort);
		startThreadedServer(serverAddress);
		final SelectorProvider provider = newSelectorProvider();
		final SocketChannel clientChannel = provider.openSocketChannel();
		clientChannel.configureBlocking(true);
		final Socket sock = clientChannel.socket();
		final InetSocketAddress clientAddress = 
			new InetSocketAddress("127.0.0.1", 10000);
		sock.bind(clientAddress);
		assertTrue("Socket not bound!!", sock.isBound());

		clientChannel.connect(serverAddress);
		assertTrue("Socket not connected!", sock.isConnected());
		
		final OutputStream os = toOutputStream(clientChannel);
		
		final InputStream bais = new ByteArrayInputStream(TEST_BYTES);
		copy(bais, os);
		log.info("Copied test bytes from TEST_BYTES through client socket");
		
		final InputStream is = toInputStream(clientChannel);
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		copy(is, baos, TEST_BYTES.length, readStrategy);
		baos.close();
		bais.close();
		
		final byte[] bytesCopy = baos.toByteArray();
		assertEquals("byte array lengths aren't equal", 
			TEST_BYTES.length, bytesCopy.length);
		assertTrue(Arrays.equals(TEST_BYTES, bytesCopy));
		clientChannel.close();
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
		log.trace("Wrote " + count + " bytes.");
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
		}
		else {
			bufSize = DEFAULT_BUFFER_SIZE;
		}
		final byte buffer[] = new byte[bufSize];
		int len = 0;
		long written = 0;
		long byteCount = originalByteCount;
		try {
			while (byteCount > 0) {
				log.info("IN LOOP");
				if (byteCount < bufSize) {
					len = readStrategy.read(in, buffer, 0, (int) byteCount);
					//len = in.read(buffer, 0, (int) byteCount);
				} else {
					len = readStrategy.read(in, buffer, 0, bufSize);
					//len = in.read(buffer, 0, bufSize);
				}
				if (len == -1) {
					log.info("Breaking on length = -1");
					break;
				}
				byteCount -= len;
				log.info("Decrementing byte count by "+len+" to "+byteCount);
				out.write(buffer, 0, len);
				log.info("WROTE DATA");
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

	private OutputStream toOutputStream(final SocketChannel clientChannel) {
		return new OutputStreamUDT(clientChannel, clientChannel.socket());
		//return clientChannel.socket().getOutputStream();
	}
	
	private InputStream toInputStream(final SocketChannel sc) {
		return new InputStreamUDT(sc, sc.socket());
		//return sc.socket().getInputStream();
	}
		
	private SelectorProvider newSelectorProvider() {
		return SelectorProviderUDT.DATAGRAM;
		//return SelectorProvider.provider();
	}


	private void runTestServer(final InetSocketAddress serverAddress) 
		throws Exception {
		final SelectorProvider provider = newSelectorProvider();
		final ServerSocketChannel acceptorChannel = 
			provider.openServerSocketChannel();
		final ServerSocket acceptorSocket = acceptorChannel.socket();
		acceptorSocket.bind(serverAddress);
		
		assertTrue("Acceptor should be bound", acceptorSocket.isBound());
		final SocketChannel connectorChannel = acceptorChannel.accept();
		
		echo(connectorChannel);
	}

	private void startThreadedServer(final InetSocketAddress serverAddress) {
		final Runnable runner = new Runnable() {
			
			public void run() {
				//startServer();
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
	
	private void echo(final SocketChannel sc) {
		final InputStream is = toInputStream(sc);
		final Runnable runner = new Runnable() {

			public void run() {
				try {
					final OutputStream os = toOutputStream(sc);
					log.info("About to echo bytes back to client");
					copy(is, os, TEST_BYTES.length, activeReadStrategy);
					log.info("DONE COPYING...SLEEPING");
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
