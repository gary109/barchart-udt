package com.barchart.udt;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.nio.SelectorProviderUDT;

/**
 * Sets up a simple UDT client and server to test sending messages between them.
 */
public class SimpleUdtTest {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Test
	public void testUdtClientServer() throws Exception {

		// This will hold the message received on the server to make sure
		// we're getting the right one across multiple threads.
		final AtomicReference<String> ref = new AtomicReference<String>();

		// final InetSocketAddress serverAddress =
		// new InetSocketAddress("127.0.0.1", 12356);
		final InetSocketAddress serverAddress = HelperTestUtilities
				.getLocalSocketAddress();

		startThreadedServer(serverAddress, ref);
		final SelectorProvider provider = SelectorProviderUDT.DATAGRAM;
		final SocketChannel clientChannel = provider.openSocketChannel();
		clientChannel.configureBlocking(true);
		final Socket sock = clientChannel.socket();

		// final InetSocketAddress clientAddress = new InetSocketAddress(
		// "127.0.0.1", 10011);
		final InetSocketAddress clientAddress = HelperTestUtilities
				.getLocalSocketAddress();

		sock.bind(clientAddress);
		assertTrue("Socket not bound!!", sock.isBound());

		clientChannel.connect(serverAddress);
		assertTrue("Socket not connected!", sock.isConnected());

		final SocketChannel ch = sock.getChannel();
		final String msg = "HELLO";
		ch.write(ByteBuffer.wrap(msg.getBytes()));

		synchronized (ref) {
			final String str = ref.get();
			if (str == null || !str.equals(msg)) {
				ref.wait(4000);
			}
		}
		assertEquals(msg.length(), ref.get().length());
		assertEquals("Did not get the expected message on the server!!", msg,
				ref.get());
		log.info("Server received: {}", ref.get());

		final byte[] received = new byte[100];
		final ByteBuffer bb = ByteBuffer.wrap(received);
		ch.read(bb);
		assertEquals(msg, new String(received).trim());
	}

	private void startThreadedServer(final InetSocketAddress serverAddress,
			final AtomicReference<String> ref) {
		final Runnable runner = new Runnable() {

			@Override
			public void run() {
				try {
					startUdtServer(serverAddress, ref);
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		};
		final Thread t = new Thread(runner, "test-thread");
		t.setDaemon(true);
		t.start();

		// We need to wait for a second to make sure the server thread starts.
		try {
			Thread.sleep(800);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void startUdtServer(final InetSocketAddress serverAddress,
			final AtomicReference<String> ref) throws IOException {
		final SelectorProvider provider = SelectorProviderUDT.DATAGRAM;
		final ServerSocketChannel acceptorChannel = provider
				.openServerSocketChannel();
		final ServerSocket acceptorSocket = acceptorChannel.socket();
		acceptorSocket.bind(serverAddress);

		assert acceptorSocket.isBound();
		final SocketChannel connectorChannel = acceptorChannel.accept();
		assert connectorChannel.isConnected();
		echo(connectorChannel, ref);
	}

	private void echo(final SocketChannel sc, final AtomicReference<String> ref) {
		final Runnable runner = new Runnable() {

			@Override
			public void run() {
				try {
					// Just read in the data and echo it back.
					final byte[] data = new byte[8192];
					final ByteBuffer dst = ByteBuffer.wrap(data);
					final int read = sc.read(dst);
					final String str = new String(data, 0, read);
					ref.set(str);
					sc.write(ByteBuffer.wrap(str.getBytes()));
					synchronized (ref) {
						ref.notifyAll();
					}
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		};
		final Thread dt = new Thread(runner, "test-thread");
		dt.setDaemon(true);
		dt.start();
	}

}
