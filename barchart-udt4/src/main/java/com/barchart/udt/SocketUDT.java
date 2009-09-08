/**
 * =================================================================================
 *
 * BSD LICENCE (http://en.wikipedia.org/wiki/BSD_licenses)
 *
 * ARTIFACT='barchart-udt4'.VERSION='1.0.0-SNAPSHOT'.TIMESTAMP='2009-09-08_14-26-03'
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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* note: do not change field names; used by JNI */
/* note: must synchronize create/destroy - workaround for bug in UDT */
public class SocketUDT {

	private static final Logger log = LoggerFactory.getLogger(SocketUDT.class);

	/**
	 * message time to live
	 */
	public static final int INFINITE_TTL = -1;

	// blocking send/receive infinite call timeout
	public static final int INFINITE_TIMEOUT = -1;

	// bandwidth option
	public static final long UNLIMITED_BW = -1L;

	// number of connections queued in listening mode
	public static final int DEFAULT_ACCEPT_QUEUE_SIZE = 256;

	// maximum number sockets participating in a select() operation
	public static final int DEFAULT_MAX_SELECTOR_SIZE = 1024;

	// maximum number of threads doing connect() operation
	public static final int DEFAULT_CONNECTOR_POOL_SIZE = 16;

	/**
	 * minimum timeout of a select() operation, millis == 10; since UDT ::
	 * common.cpp :: void CTimer::waitForEvent() :: is using 10 ms resolution
	 */
	public static final int DEFAULT_MIN_SELECTOR_TIMEOUT = 10;

	// native library extractor and loader
	static {
		try {
			LibraryUDT.load("./lib/bin");
		} catch (Throwable e) {
			log.error("failed to load native library; terminating", e);
			System.exit(1);
		}
		try {
			initClass0();
		} catch (Throwable e) {
			log.error("failed to init native library; terminating", e);
			System.exit(2);
		}
		log.debug("native library load & init OK");
	}

	// ###################################################

	/**
	 * native descriptor; read by JNI; see udt.h "typedef int UDTSOCKET;"
	 */
	public final int socketID;

	/**
	 * native socket type; SOCK_DGRAM / SOCK_STREAM
	 */
	protected final int socketType;

	/**
	 * native address family; read by JNI
	 */
	// TODO support AF_INET6
	protected final int socketAddressFamily;

	/**
	 * message/stream socket type; read by JNI
	 */
	public final TypeUDT type;

	/**
	 * performance monitor; updated by JNI
	 */
	public final MonitorUDT monitor;

	/**
	 * message send mode; read by JNI on each message send
	 */
	protected volatile int messageTimeTolive;
	protected volatile boolean messageIsOrdered;

	/**
	 * end points; written by JNI after hasXXX returns
	 */
	protected volatile InetSocketAddress localSocketAddress;
	protected volatile InetSocketAddress remoteSocketAddress;

	/**
	 * JNI wrapper exceptin codes
	 */
	public static final int UDT_EXCEPT_INDEX = 2;
	public static final int UDT_WRITE_INDEX = 1;
	public static final int UDT_READ_INDEX = 0;
	public static final int UDT_SIZE_COUNT = 3;

	/**
	 * selectEx result status
	 */
	protected boolean isSelectedRead;
	protected boolean isSelectedWrite;
	protected boolean isSelectedException;

	// ###################################################
	// ### UDT API
	// ###

	/**
	 * http://www.cs.uic.edu/~ygu1/doc/startup.htm
	 */
	protected static native void initClass0() throws ExceptionUDT;

	/**
	 * http://www.cs.uic.edu/~ygu1/doc/cleanup.htm
	 */
	protected static native void stopClass0() throws ExceptionUDT;

	/**
	 * used by api users
	 */
	protected native int initInstance0(int typeCode) throws ExceptionUDT;

	/**
	 * used by accept() internally
	 */
	protected native int initInstance1(int socketUDT) throws ExceptionUDT;

	/**
	 * http://www.cs.uic.edu/~ygu1/doc/accept.htm
	 */
	protected native SocketUDT accept0() throws ExceptionUDT;

	public SocketUDT accept() throws ExceptionUDT {
		return accept0();
	}

	protected void checkSocketAddress(InetSocketAddress socketAddress) {
		if (socketAddress == null) {
			throw new IllegalArgumentException("socketAddress can't be null");
		}
		if (socketAddress.isUnresolved()) {
			// can not use; internal InetAddress field is null
			throw new IllegalArgumentException("socketAddress is unresolved : "
					+ socketAddress + " : check your DNS settings");
		}
		//
		// InetAddress inetAddress = socketAddress.getAddress();
		// if (inetAddress.isAnyLocalAddress()) {
		// log.warn("trying to use an ANY(0.0.0.0) address; "
		// + "this may interfere with UDT connections; "
		// + "please use concrete address instead");
		// }
	}

	/**
	 * http://www.cs.uic.edu/~ygu1/doc/bind.htm
	 */
	protected native void bind0(InetSocketAddress localSocketAddress)
			throws ExceptionUDT;

	public void bind(InetSocketAddress localSocketAddress) //
			throws ExceptionUDT, IllegalArgumentException {
		checkSocketAddress(localSocketAddress);
		bind0(localSocketAddress);
	}

	/**
	 * http://www.cs.uic.edu/~ygu1/doc/close.htm
	 */
	protected native void close0() throws ExceptionUDT;

	public void close() throws ExceptionUDT {
		synchronized (SocketUDT.class) {
			if (isOpen()) {
				close0();
				log.debug("closed socketID={}", socketID);
			}
		}
	}

	// NOTE: catch all exceptions; else prevents GC
	// NOTE: do not leak "this" references; else prevents GC
	@Override
	protected void finalize() {
		try {
			close();
			super.finalize();
		} catch (Throwable e) {
			log.error("failed to close", e);
		}
	}

	/**
	 * http://www.cs.uic.edu/~ygu1/doc/connect.htm
	 */
	protected native void connect0(InetSocketAddress remoteSocketAddress)
			throws ExceptionUDT;

	public void connect(InetSocketAddress remoteSocketAddress) //
			throws ExceptionUDT, IllegalArgumentException {
		checkSocketAddress(remoteSocketAddress);
		connect0(remoteSocketAddress);
	}

	/**
	 * http://www.cs.uic.edu/~ygu1/doc/peername.htm
	 */
	protected native boolean hasLoadedRemoteSocketAddress();

	public InetSocketAddress getRemoteSocketAddress() throws ExceptionUDT {
		if (hasLoadedRemoteSocketAddress()) {
			return remoteSocketAddress;
		} else {
			return null;
		}
	}

	/**
	 * http://www.cs.uic.edu/~ygu1/doc/sockname.htm
	 */
	protected native boolean hasLoadedLocalSocketAddress();

	public InetSocketAddress getLocalSocketAddress() throws ExceptionUDT {
		if (hasLoadedLocalSocketAddress()) {
			return localSocketAddress;
		} else {
			return null;
		}
	}

	/**
	 * http://www.cs.uic.edu/~ygu1/doc/opt.htm
	 */
	protected native Object getOption0(int code, Class<?> klaz)
			throws ExceptionUDT;

	public Object getOption(OptionUDT option) throws ExceptionUDT {
		if (option == null) {
			throw new NullPointerException("option == null");
		}
		return getOption0(option.code, option.klaz);
	}

	protected native void setOption0(int code, Class<?> klaz, Object value)
			throws ExceptionUDT;

	public void setOption(OptionUDT option, Object value) throws ExceptionUDT {
		if (option == null || value == null) {
			throw new NullPointerException("option == null || value == null");
		}
		if (value.getClass() == option.klaz) {
			setOption0(option.code, option.klaz, value);
		} else {
			throw new ExceptionUDT(socketID, ErrorUDT.WRAPPER_MESSAGE,
					"option and value types do not match: "
							+ option.klaz.getName() + " vs "
							+ value.getClass().getName());
		}
	}

	/**
	 * http://www.cs.uic.edu/~ygu1/doc/listen.htm
	 */
	protected native void listen0(int queueSize) throws ExceptionUDT;

	protected volatile int listenQueueSize;

	public void listen(int queueSize) throws ExceptionUDT {
		if (queueSize <= 0) {
			throw new IllegalArgumentException("queueSize <= 0");
		}
		listenQueueSize = queueSize;
		listen0(queueSize);
	}

	public int listenQueueSize() {
		return listenQueueSize;
	}

	/**
	 * http://www.cs.uic.edu/~ygu1/doc/recv.htm
	 * http://www.cs.uic.edu/~ygu1/doc/recvmsg.htm
	 * 
	 * * return values, if exception is NOT thrown
	 * 
	 * -1 : non-blocking-only - nothing received
	 * 
	 * =0 : timeout expired
	 * 
	 * >0 : normal receive / // /**
	 */
	protected native int receive0(int socketID, int socketType, byte[] array)
			throws ExceptionUDT;

	public int receive(byte[] array) throws ExceptionUDT {
		if (array == null) {
			throw new NullPointerException("array == null");
		}
		return receive0(socketID, socketType, array);
	}

	/**
	 * http://www.cs.uic.edu/~ygu1/doc/recvfile.htm
	 */
	public int receiveFile(ByteBuffer buffer) throws ExceptionUDT {
		throw new ExceptionUDT(//
				socketID, ErrorUDT.WRAPPER_UNIMPLEMENTED, "receiveFile");
	}

	/**
	 * http://www.cs.uic.edu/~ygu1/doc/select.htm
	 * 
	 * timeout in milliseconds
	 * 
	 * return value, when NOT exception
	 * 
	 * =0 : timeout, no ready sockets
	 * 
	 * >0 : total number or reads, writes, exceptions
	 */
	protected static native int select0( //
			int[] readArray, //
			int[] writeArray, //
			int[] exceptArray, //
			int[] sizeArray, //
			long millisTimeout //
	) throws ExceptionUDT;

	/**
	 * timeout in milliseconds
	 */
	// asserts are contracts
	public static int select( //
			int[] readArray, //
			int[] writeArray, //
			int[] exceptArray, //
			int[] sizeArray, //
			long millisTimeout) throws ExceptionUDT {

		assert readArray != null;
		assert writeArray != null;
		assert exceptArray != null;
		assert sizeArray != null;

		assert readArray.length >= sizeArray[UDT_READ_INDEX];
		assert writeArray.length >= sizeArray[UDT_WRITE_INDEX];
		assert exceptArray.length >= readArray.length;
		assert exceptArray.length >= writeArray.length;

		assert millisTimeout >= DEFAULT_MIN_SELECTOR_TIMEOUT
				|| millisTimeout <= 0;

		return select0(readArray, writeArray, exceptArray, sizeArray,
				millisTimeout);

	}

	/**
	 * unimplemented
	 */
	protected static native void selectEx0(//
			int[] registrationArray, //
			int[] readArray, //
			int[] writeArray, //
			int[] exceptionArray, //
			long timeout) throws ExceptionUDT;

	/**
	 * http://www.cs.uic.edu/~ygu1/doc/send.htm
	 * http://www.cs.uic.edu/~ygu1/doc/sendmsg.htm
	 * 
	 * return values, if exception is NOT thrown
	 * 
	 * -1 : non-blocking-only - no buffer space
	 * 
	 * =0 : timeout expired
	 * 
	 * >0 : normal send
	 */
	protected native int send0(int socketID, int socketType, int timeToLive,
			boolean isOrdered, byte[] array) throws ExceptionUDT;

	public int send(byte[] array) throws ExceptionUDT {
		if (array == null) {
			throw new NullPointerException("array == null");
		}
		return send0(socketID, socketType, //
				messageTimeTolive, messageIsOrdered, array);
	}

	/**
	 * default timeToLive value used by sendmsg mode
	 */
	public void setMessageTimeTolLive(int timeToLive) {
		messageTimeTolive = timeToLive;
	}

	/**
	 * default isOrdered value used by sendmsg mode
	 */
	public void setMessageIsOdered(boolean isOrdered) {
		messageIsOrdered = isOrdered;
	}

	/**
	 * http://www.cs.uic.edu/~ygu1/doc/sendfile.htm
	 */
	public int sendFile(ByteBuffer buffer) throws ExceptionUDT {
		throw new ExceptionUDT(//
				socketID, ErrorUDT.WRAPPER_UNIMPLEMENTED, "sendFile");
	}

	/**
	 * http://www.cs.uic.edu/~ygu1/doc/perfmon.htm
	 */
	protected native void updateMonitor0(boolean makeClear) throws ExceptionUDT;

	public void updateMonitor(boolean makeClear) throws ExceptionUDT {
		updateMonitor0(makeClear);
	}

	/**
	 * http://www.cs.uic.edu/~ygu1/doc/t-error.htm
	 */
	protected native int getErrorCode0();

	public int getErrorCode() {
		return getErrorCode0();
	}

	protected native String getErrorMessage0();

	public String getErrorMessage() {
		return getErrorMessage0();
	}

	protected native void clearError0();

	public void clearError() {
		clearError0();
	}

	/**
	 * not yet available in UDT; use a hack to detect open state
	 */
	protected native boolean isOpen0();

	public boolean isOpen() {
		return isOpen0();
	}

	public boolean isClosed() {
		return !isOpen0();
	}

	// ###
	// ### UDT API
	// ###################################################

	public void setDefaultMessageSendMode() {
		setMessageIsOdered(true);
		setMessageTimeTolLive(SocketUDT.INFINITE_TTL);
	}

	// primary socket
	public SocketUDT(TypeUDT type) throws ExceptionUDT {
		synchronized (SocketUDT.class) {
			this.type = type;
			this.monitor = new MonitorUDT();
			this.socketID = initInstance0(type.code);
			this.socketType = type.code;
			this.socketAddressFamily = 2; // ipv4
			setDefaultMessageSendMode();
		}
	}

	// secondary socket; made by accept(), socketID is descriptor
	protected SocketUDT(TypeUDT type, int socketID) throws ExceptionUDT {
		synchronized (SocketUDT.class) {
			this.type = type;
			this.monitor = new MonitorUDT();
			this.socketID = initInstance1(socketID);
			this.socketType = type.code;
			this.socketAddressFamily = 2; // ipv4
			setDefaultMessageSendMode();
		}
	}

	public boolean isBound() {
		if (isClosed()) {
			return false;
		}
		try {
			return getLocalSocketAddress() != null;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isConnected() {
		if (isClosed()) {
			return false;
		}
		try {
			return getRemoteSocketAddress() != null;
		} catch (Exception e) {
			return false;
		}
	}

	public ErrorUDT getError() {
		int code = getErrorCode();
		return ErrorUDT.of(code);
	}

	//

	public void configureBlocking(boolean block) throws ExceptionUDT {
		if (block) {
			setOption(OptionUDT.Is_Receive_Synchronous, Boolean.TRUE);
			setOption(OptionUDT.Is_Send_Synchronous, Boolean.TRUE);
		} else {
			setOption(OptionUDT.Is_Receive_Synchronous, Boolean.FALSE);
			setOption(OptionUDT.Is_Send_Synchronous, Boolean.FALSE);
		}
	}

	public int getSendBufferSize() throws ExceptionUDT {
		return (Integer) getOption(OptionUDT.Protocol_Send_Buffer_Size);
	}

	public int getReceiveBufferSize() throws ExceptionUDT {
		return (Integer) getOption(OptionUDT.Protocol_Receive_Buffer_Size);
	}

	public boolean getReuseAddress() throws ExceptionUDT {
		return (Boolean) getOption(OptionUDT.Is_Address_Reuse_Enabled);
	}

	public int getSoLinger() throws ExceptionUDT {
		return ((LingerUDT) getOption(OptionUDT.Time_To_Linger_On_Close))
				.intValue();
	}

	/**
	 * returns milliseconds; zero return means "infinite"; negative means
	 * invalid
	 */
	public int getSoTimeout() throws ExceptionUDT {
		int sendTimeout = (Integer) getOption(OptionUDT.Send_Timeout);
		int receiveTimeout = (Integer) getOption(OptionUDT.Receive_Timeout);
		final int millisTimeout;
		if (sendTimeout != receiveTimeout) {
			log.error("sendTimeout != receiveTimeout");
			millisTimeout = Math.max(sendTimeout, receiveTimeout);
		} else {
			// map form UDT value convention to java value convention
			if (sendTimeout < 0) {
				// UDT infinite
				millisTimeout = 0;
			} else if (sendTimeout > 0) {
				// UDT finite
				millisTimeout = sendTimeout;
			} else { // ==0
				log.error("UDT reported unexpected zero timeout");
				millisTimeout = -1;
			}
		}
		return millisTimeout;
	}

	public void setSendBufferSize(int size) throws ExceptionUDT {
		setOption(OptionUDT.Protocol_Send_Buffer_Size, size);
	}

	public void setReceiveBufferSize(int size) throws ExceptionUDT {
		setOption(OptionUDT.Protocol_Receive_Buffer_Size, size);
	}

	public void setReuseAddress(boolean on) throws ExceptionUDT {
		setOption(OptionUDT.Is_Address_Reuse_Enabled, on);
	}

	static final LingerUDT LINGER_ZERO = new LingerUDT(0);

	public void setSoLinger(boolean on, int linger) throws ExceptionUDT {
		if (on) {
			if (linger <= 0) {
				throw new IllegalArgumentException("linger <= 0");
			}
			setOption(OptionUDT.Time_To_Linger_On_Close, new LingerUDT(linger));
		} else {
			setOption(OptionUDT.Time_To_Linger_On_Close, LINGER_ZERO);
		}
	}

	/**
	 * call timeout (milliseconds); Set a timeout on blocking Socket operations:
	 * ServerSocket.accept(); SocketInputStream.read();
	 * DatagramSocket.receive(); Enable/disable SO_TIMEOUT with the specified
	 * timeout, in milliseconds. A timeout of zero is interpreted as an infinite
	 * timeout.
	 */
	public void setSoTimeout(int millisTimeout) throws ExceptionUDT {
		if (millisTimeout < 0) {
			throw new IllegalArgumentException("timeout < 0");
		}
		if (millisTimeout == 0) {
			// UDT uses different value for "infinite"
			millisTimeout = INFINITE_TIMEOUT;
		}
		setOption(OptionUDT.Send_Timeout, millisTimeout);
		setOption(OptionUDT.Receive_Timeout, millisTimeout);
	}

	public InetAddress getRemoteInetAddress() {
		try {
			InetSocketAddress remote = getRemoteSocketAddress();
			if (remote == null) {
				return null;
			} else {
				return remote.getAddress();
			}
		} catch (ExceptionUDT e) {
			return null;
		}
	}

	public int getRemoteInetPort() {
		try {
			InetSocketAddress remote = getRemoteSocketAddress();
			if (remote == null) {
				return 0;
			} else {
				return remote.getPort();
			}
		} catch (ExceptionUDT e) {
			return 0;
		}
	}

	public InetAddress getLocalInetAddress() {
		try {
			InetSocketAddress local = getLocalSocketAddress();
			if (local == null) {
				return null;
			} else {
				return local.getAddress();
			}
		} catch (ExceptionUDT e) {
			return null;
		}
	}

	public int getLocalInetPort() {
		try {
			InetSocketAddress local = getLocalSocketAddress();
			if (local == null) {
				return 0;
			} else {
				return local.getPort();
			}
		} catch (ExceptionUDT e) {
			return 0;
		}
	}

	//

	@Override
	public int hashCode() {
		return socketID;
	}

	@Override
	public boolean equals(Object otherSocketUDT) {
		if (otherSocketUDT instanceof SocketUDT) {
			SocketUDT other = (SocketUDT) otherSocketUDT;
			return other.socketID == this.socketID;
		}
		return false;
	}

	// #############################
	// used for development only

	protected native void testEmptyCall0();

	protected native void testIterateArray0(Object[] array);

	protected native void testIterateSet0(Set<Object> set);

	protected native int[] testMakeArray0(int size);

	protected native void testGetSetArray0(int[] array, boolean isReturn);

	// used for development only
	// #############################

}

// TODO memory mapped buffers
// public int sendMessage(ByteBuffer buffer, int timeToLive, boolean
// isOrdered)
// throws ExceptionUDT {
// int position = buffer.position();
// int limit = buffer.limit();
// assert (position <= limit);
// int remaining = (position <= limit ? limit - position : 0);
// long offset = ((DirectBuffer) buffer).address() + position;
// int written = sendMessage1(offset, remaining, timeToLive, isOrdered);
// if (written > 0) {
// buffer.position(position + written);
// }
// return written;
// }
