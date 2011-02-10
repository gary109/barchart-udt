package com.barchart.udt.net;

import com.barchart.udt.SocketUDT;

class StreamService extends StreamBase implements Runnable {

	StreamService(final SocketUDT socket) throws Exception {

		super(socket, socket.getLocalSocketAddress(), socket
				.getRemoteSocketAddress());

	}

	@Override
	public void run() {

		while (true) {

			try {

				int value = streamIn.read();

				streamOut.write(value);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

}
