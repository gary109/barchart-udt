package com.barchart.udt.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

public class MainLoadSocketUDT {

	static final Logger log = LoggerFactory.getLogger(MainLoadSocketUDT.class);

	public static void main(String[] args) {

		log.info("this example tests if barchart-udt dependency works");

		try {

			SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

			log.info("made socketID={}", socket.socketID);

		} catch (Throwable e) {

			log.error("can not make socket", e);

			System.exit(1);

		}

	}

}
