package com.barchart.udt.net;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.StatusUDT;
import com.barchart.udt.TypeUDT;
import com.barchart.udt.util.HelperUtils;

class StreamServer extends StreamBase {

	final ExecutorService executor;

	final Runnable taskAccept = new Runnable() {
		@Override
		public void run() {

			try {

				socket.bind(localAddress);
				assert socket.isBound();

				socket.listen(1);
				assert socket.getStatus() == StatusUDT.LISTENING;

				SocketUDT connectorSocket = socket.accept();

				Runnable taskService = new StreamService(connectorSocket);

				executor.submit(taskService);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	};

	StreamServer(final InetSocketAddress acceptorAddress) throws Exception {

		super(new SocketUDT(TypeUDT.DATAGRAM), HelperUtils
				.getLocalSocketAddress(), acceptorAddress);

		this.executor = Executors.newCachedThreadPool();

	}

	void showtime() {
		executor.submit(taskAccept);
	}

	void shutdown() {
		executor.shutdown();
	}

}
