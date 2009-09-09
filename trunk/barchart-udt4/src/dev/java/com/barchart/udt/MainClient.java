/**
 * =================================================================================
 *
 * BSD LICENCE (http://en.wikipedia.org/wiki/BSD_licenses)
 *
 * ARTIFACT='barchart-udt4'.VERSION='1.0.0-SNAPSHOT'.TIMESTAMP='2009-09-09_16-17-24'
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

public class MainClient {

	private static Logger log = LoggerFactory.getLogger(MainClient.class);

	public static void main(String[] args) {

		log.info("started CLIENT");

		String bindAddress = getProperty("udt.bind.address");
		String remoteAddress = getProperty("udt.remote.address");
		int remotePort = Integer.parseInt(getProperty("udt.remote.port"));

		long maxBandwidth = Integer.parseInt(getProperty("udt.max.bandwidth"));
		int countBatch = Integer.parseInt(getProperty("udt.count.batch"));
		int countSleep = Integer.parseInt(getProperty("udt.count.sleep"));
		int countMonitor = Integer.parseInt(getProperty("udt.count.monitor"));

		try {

			SocketUDT sender = new SocketUDT(TypeUDT.DATAGRAM);

			// bytes/sec
			sender.setOption(OptionUDT.UDT_MAXBW, maxBandwidth);

			InetSocketAddress localSocketAddress = new InetSocketAddress( //
					bindAddress, 0);

			sender.bind(localSocketAddress);
			localSocketAddress = sender.getLocalSocketAddress();
			log.info("bind; localSocketAddress={}", localSocketAddress);

			InetSocketAddress remoteSocketAddress = new InetSocketAddress(//
					remoteAddress, remotePort);

			sender.connect(remoteSocketAddress);
			remoteSocketAddress = sender.getRemoteSocketAddress();
			log.info("connect; remoteSocketAddress={}", remoteSocketAddress);

			StringBuilder text = new StringBuilder(1024);
			OptionUDT.appendSnapshot(sender, text);
			text.append("\t\n");
			log.info("sender options; {}", text);

			long count = 0;

			MonitorUDT monitor = sender.monitor;

			while (true) {

				for (int k = 0; k < countBatch; k++) {

					byte[] array = new byte[SIZE];

					putSequenceNumber(array);

					int result = sender.send(array);

					assert result == SIZE : "wrong size";

				}

				Thread.sleep(countSleep);

				count++;

				if (count % countMonitor == 0) {
					sender.updateMonitor(false);
					text = new StringBuilder(1024);
					monitor.appendSnapshot(text);
					log.info("stats; {}", text);
				}

			}

			// log.info("result={}", result);

		} catch (Throwable e) {
			log.error("unexpected", e);
		}

	}

	private static final int SIZE = 1460;

	final static AtomicLong sequencNumber = new AtomicLong(0);

	static void putSequenceNumber(byte[] array) {

		ByteBuffer buffer = ByteBuffer.wrap(array);

		buffer.putLong(sequencNumber.getAndIncrement());

	}

}
