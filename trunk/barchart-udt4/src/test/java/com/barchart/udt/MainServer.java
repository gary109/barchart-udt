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
package com.barchart.udt;

import static com.barchart.udt.HelperTestUtilities.*;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.MonitorUDT;
import com.barchart.udt.OptionUDT;
import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

public class MainServer {

	private static Logger log = LoggerFactory.getLogger(MainServer.class);

	public static void main(String[] args) {

		log.info("started SERVER");

		String bindAddress = getProperty("udt.bind.address");
		int localPort = Integer.parseInt(getProperty("udt.local.port"));
		int countMonitor = Integer.parseInt(getProperty("udt.count.monitor"));

		try {

			SocketUDT acceptor = new SocketUDT(TypeUDT.DATAGRAM);
			log.info("init; acceptor={}", acceptor.socketID);

			InetSocketAddress localSocketAddress = new InetSocketAddress(
					bindAddress, localPort);

			acceptor.bind(localSocketAddress);
			localSocketAddress = acceptor.getLocalSocketAddress();
			log.info("bind; localSocketAddress={}", localSocketAddress);

			acceptor.listen(10);
			log.info("listen;");

			SocketUDT receiver = acceptor.accept();

			log.info("accept; receiver={}", receiver.socketID);

			assert receiver.socketID != acceptor.socketID;

			long timeStart = System.currentTimeMillis();

			//

			InetSocketAddress remoteSocketAddress = receiver
					.getRemoteSocketAddress();

			log.info("receiver; remoteSocketAddress={}", remoteSocketAddress);

			StringBuilder text = new StringBuilder(1024);
			OptionUDT.appendSnapshot(receiver, text);
			text.append("\t\n");
			log.info("receiver options; {}", text);

			MonitorUDT monitor = receiver.monitor;

			while (true) {

				byte[] array = new byte[SIZE];

				int result = receiver.receive(array);

				assert result == SIZE : "wrong size";

				getSequenceNumber(array);

				if (sequenceNumber % countMonitor == 0) {

					receiver.updateMonitor(false);
					text = new StringBuilder(1024);
					monitor.appendSnapshot(text);
					log.info("stats; {}", text);

					long timeFinish = System.currentTimeMillis();

					long timeDiff = 1 + (timeFinish - timeStart) / 1000;

					long byteCount = sequenceNumber * SIZE;

					long rate = byteCount / timeDiff;

					log.info("receive rate, bytes/second: {}", rate);

				}

			}

			// log.info("result={}", result);

		} catch (Throwable e) {
			log.error("unexpected", e);
		}

	}

	static long sequenceNumber = 0;

	static void getSequenceNumber(byte[] array) {

		ByteBuffer buffer = ByteBuffer.wrap(array);

		long currentNumber = buffer.getLong();

		if (currentNumber == sequenceNumber) {
			sequenceNumber++;
		} else {
			log.error("sequence error; currentNumber={} sequenceNumber={}",//
					currentNumber, sequenceNumber);
			System.exit(1);
		}

	}

	final static AtomicLong sequencNumber = new AtomicLong(0);

	private static final int SIZE = 1460;

}
