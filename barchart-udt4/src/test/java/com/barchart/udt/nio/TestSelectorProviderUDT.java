/**
 * =================================================================================
 *
 * BSD LICENCE (http://en.wikipedia.org/wiki/BSD_licenses)
 *
 * ARTIFACT='barchart-udt4'.VERSION='1.0.0-SNAPSHOT'.TIMESTAMP='2009-09-07_23-05-40'
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
package com.barchart.udt.nio;

import static org.junit.Assert.*;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.SocketUDT;

public class TestSelectorProviderUDT {

	static final Logger log = LoggerFactory
			.getLogger(TestSelectorProviderUDT.class);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	// @Test
	public void testOpenSelector() {
		log.info("Not yet implemented");
	}

	// @Test
	public void testSelect_A() {

		log.info("testSelect_A");

		try {

			SelectorProviderUDT provider = SelectorProviderUDT.datagramProvider;

			Selector selector = provider.openSelector();

			ChannelSocketUDT channelClient = (ChannelSocketUDT) provider
					.openSocketChannel();

			ChannelServerSocketUDT channelServer = (ChannelServerSocketUDT) provider
					.openServerSocketChannel();

			channelClient.configureBlocking(false);
			channelServer.configureBlocking(false);

			InetSocketAddress localAddress1 = new InetSocketAddress(//
					"localhost", 9011);

			InetSocketAddress localAddress2 = new InetSocketAddress(//
					"localhost", 9012);

			ServerSocket socketServer = channelServer.socket();
			socketServer.bind(localAddress1);
			// socketServer.accept();

			Socket socketClient = channelClient.socket();
			socketClient.bind(localAddress2);
			// socketClient.accept();

			SelectionKeyUDT key1 = (SelectionKeyUDT) channelServer.register(
					selector, SelectionKey.OP_ACCEPT);
			SelectionKeyUDT key2 = (SelectionKeyUDT) channelClient.register(
					selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

			Set<SelectionKeyUDT> registeredKeySet = new HashSet<SelectionKeyUDT>();
			registeredKeySet.add(key1);
			registeredKeySet.add(key2);

			Set<SelectionKeyUDT> selectedKeySet = new HashSet<SelectionKeyUDT>();

			log.info("registeredKeySet={}", registeredKeySet);
			log.info("selectedKeySet={}", selectedKeySet);

			log.info("");

			// socketServer.clearError();

			long timeStart = System.currentTimeMillis();

			int millisTimeout = 1 * 1000;

			int[] readArray = new int[1024];
			int[] writeArray = new int[1024];
			int[] exceptArray = new int[1024];
			int[] sizeArray = new int[3];

			readArray[0] = channelServer.serverSocketUDT.socketID;
			readArray[1] = channelClient.socketUDT.socketID;

			sizeArray[0] = 2;

			log.info("readArray={}", readArray);

			SocketUDT.select(readArray, writeArray, exceptArray, sizeArray,
					millisTimeout);
			log.info("readArray={}", readArray);

			long timeFinish = System.currentTimeMillis();

			long timeDiff = timeFinish - timeStart;
			log.info("timeDiff={}", timeDiff);

			socketServer.close();
			socketClient.close();

		} catch (Exception e) {
			fail("SocketException; " + e.getMessage());
		}

	}

	@Test
	public void testSelect_B() {

		log.info("testSelect_B");

		try {

			SelectorProviderUDT provider = SelectorProviderUDT.datagramProvider;

			Selector selector = provider.openSelector();

			SocketChannel channelClient = (ChannelSocketUDT) provider
					.openSocketChannel();

			ServerSocketChannel channelServer = (ChannelServerSocketUDT) provider
					.openServerSocketChannel();

			channelClient.configureBlocking(false);
			channelServer.configureBlocking(false);

			InetSocketAddress localServer = new InetSocketAddress(//
					"localhost", 9021);

			InetSocketAddress localClient = new InetSocketAddress(//
					"localhost", 9022);

			ServerSocket socketServer = channelServer.socket();
			socketServer.bind(localServer);
			// socketServer.accept();

			Socket socketClient = channelClient.socket();
			socketClient.bind(localClient);
			// socketClient.accept();

			socketClient.connect(localServer);
			log.info("connect");

			SelectionKeyUDT key1 = (SelectionKeyUDT) channelServer.register(
					selector, SelectionKey.OP_ACCEPT);
			SelectionKeyUDT key2 = (SelectionKeyUDT) channelClient.register(
					selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

			int millisTimeout = 1 * 1000;

			long timeStart = System.currentTimeMillis();
			int readyCount = selector.select(millisTimeout);
			long timeFinish = System.currentTimeMillis();
			log.info("readyCount={}", readyCount);

			long timeDiff = timeFinish - timeStart;
			log.info("timeDiff={}", timeDiff);

			Set<SelectionKey> selectedKeySet = selector.selectedKeys();
			logSet(selectedKeySet);

			for (SelectionKey key : selectedKeySet) {
				if (key.isWritable()) {
					log.info("isWritable");
					SocketChannel channel = (SocketChannel) key.channel();
					ByteBuffer buffer = ByteBuffer.allocate(2048);
					int writeCount = channel.write(buffer);
					log.info("writeCount={}", writeCount);
				}
				if (key.isReadable()) {
					log.info("isReadable");
					SocketChannel channel = (SocketChannel) key.channel();
					ByteBuffer buffer = ByteBuffer.allocate(2048);
					int readCount = channel.read(buffer);
					log.info("readCount={}", readCount);
				}
			}

			socketServer.close();
			socketClient.close();

		} catch (Exception e) {
			log.error("Exception;", e);
			fail("Exception; " + e.getMessage());
		}

	}

	static void logSet(Set<?> set) {
		for (Object item : set) {
			log.info("{}", item);
		}
	}

}
