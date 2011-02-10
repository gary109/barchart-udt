package com.barchart.udt.net;

import java.net.InetSocketAddress;

import com.barchart.udt.util.HelperUtils;

class StreamClient extends StreamBase {

	StreamClient(final InetSocketAddress remoteAddress) throws Exception {
		super(HelperUtils.getLocalSocketAddress(), remoteAddress);
	}

	void connect() throws Exception {

		socket.bind(localAddress);
		assert socket.isBound();

		socket.connect(remoteAddress);
		assert socket.isConnected();

	}

	void disconnect() throws Exception {

		socket.close();

	}

}
