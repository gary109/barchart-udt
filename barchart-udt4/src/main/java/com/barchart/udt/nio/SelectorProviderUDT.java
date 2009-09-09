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
package com.barchart.udt.nio;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

public class SelectorProviderUDT extends SelectorProvider {

	/**
	 * 2 system wide provider instances, one for each type
	 */
	public static final SelectorProviderUDT datagramProvider = new SelectorProviderUDT(
			TypeUDT.DATAGRAM);

	public static final SelectorProviderUDT streamProvider = new SelectorProviderUDT(
			TypeUDT.STREAM);

	// 
	protected final TypeUDT type;

	// almost singleton
	protected SelectorProviderUDT(TypeUDT type) {
		this.type = type;
	}

	protected volatile int acceptQueueSize = SocketUDT.DEFAULT_ACCEPT_QUEUE_SIZE;

	public int getAccetpQueueSize() {
		return acceptQueueSize;
	}

	public void setAccetpQueueSize(int queueSize) {
		acceptQueueSize = queueSize;
	}

	protected volatile int maxSelectorSize = SocketUDT.DEFAULT_MAX_SELECTOR_SIZE;

	public int getMaxSelectorSize() {
		return maxSelectorSize;
	}

	public void setMaxSelectorSize(int selectorSize) {
		maxSelectorSize = selectorSize;
	}

	protected volatile int maxConnectorSize = SocketUDT.DEFAULT_CONNECTOR_POOL_SIZE;

	public int getMaxConnectorSize() {
		return maxConnectorSize;
	}

	public void setMaxConnectorSize(int connectorSize) {
		maxConnectorSize = connectorSize;
	}

	//

	@Override
	public DatagramChannel openDatagramChannel() throws IOException {
		throw new RuntimeException("feature not available");
	}

	@Override
	public Pipe openPipe() throws IOException {
		throw new RuntimeException("feature not available");
	}

	@Override
	public AbstractSelector openSelector() throws IOException {
		return new SelectorUDT(this, maxSelectorSize, maxConnectorSize);
	}

	@Override
	public ServerSocketChannel openServerSocketChannel() throws IOException {
		SocketUDT serverSocketUDT = new SocketUDT(type);
		return new ChannelServerSocketUDT(this, serverSocketUDT);
	}

	@Override
	public SocketChannel openSocketChannel() throws IOException {
		SocketUDT socketUDT = new SocketUDT(type);
		return new ChannelSocketUDT(this, socketUDT);
	}

}
