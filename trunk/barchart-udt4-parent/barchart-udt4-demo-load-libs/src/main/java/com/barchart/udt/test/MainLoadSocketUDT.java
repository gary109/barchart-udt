package com.barchart.udt.test;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

public class MainLoadSocketUDT {

	static final Logger log = LoggerFactory.getLogger(MainLoadSocketUDT.class);

	public static void main(String[] args) {

		log.info("this example tests if barchart-udt maven dependency works");

		logClassPath();

		try {

			final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

			log.info("made socketID={}", socket.getSocketId());

			log.info("socket status={}", socket.getStatus());

			log.info("socket isOpen={}", socket.isOpen());

			log.info("socket isBlocking={}", socket.isBlocking());

			log.info("socket options{}", socket.toStringOptions());

		} catch (Throwable e) {

			log.error("can not make socket", e);

		}

	}

	static void logClassPath() {

		String classPath = System.getProperty("java.class.path");

		String[] entries = classPath.split(File.pathSeparator);

		StringBuilder text = new StringBuilder(1024);

		for (String item : entries) {
			text.append("\n\t");
			text.append(item);
		}

		log.info("{}", text);
	}

}
