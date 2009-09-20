/**
 * =================================================================================
 *
 * BSD LICENCE (http://en.wikipedia.org/wiki/BSD_licenses)
 *
 * ARTIFACT='barchart-udt4'.VERSION='1.0.0-SNAPSHOT'.TIMESTAMP='2009-09-09_23-19-15'
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
/**
 * current implementation supports IPv4 only (no IPv6)
 */
public class SocketUDT {

	private static final String PACKAGE_NAME = //
	SocketUDT.class.getPackage().getName();

	private static final Logger log = LoggerFactory.getLogger(SocketUDT.class);

	/**
	 * infinite message time to live;
	 */
	public static final int INFINITE_TTL = -1;

	/**
	 * blocking send/receive infinite call timeout;
	 */
	public static final int INFINITE_TIMEOUT = -1;

	/**
	 * unlimited bandwidth option value;
	 */
	public static final long UNLIMITED_BW = -1L;

	/**
	 * Number of connections queued in listening mode by {@link #accept()}
	 */
	public static final int DEFAULT_ACCEPT_QUEUE_SIZE = 256;

	/**
	 * Maximum number sockets that can participate in a
	 * {@link com.barchart.udt.nio.SelectorUDT#select()} operation
	 */
	public static final int DEFAULT_MAX_SELECTOR_SIZE = 1024;

	/**
	 * Maximum number of threads. That can be doing
	 * {@link com.barchart.udt.nio.ChannelSocketUDT#connect(java.net.SocketAddress)}
	 * operation in non-blocking mode
	 */
	public static final int DEFAULT_CONNECTOR_POOL_SIZE = 16;

	/**
	 * Minimum timeout of a {@link com.barchart.udt.nio.SelectorUDT#select()}
	 * operation. Since UDT :: common.cpp :: void CTimer::waitForEvent() :: is
	 * using 10 milliseconds resolution; (milliseconds);
	 */
	public static final int DEFAULT_MIN_SELECTOR_TIMEOUT = 10;

	// native library extractor and loader

	/**
	 * target destination of native wrapper library *.dll or *.so files that are
	 * extracted from this library jar;
	 */
	public static final String DEFAULT_LIBRARY_EXTRACT_LOCATION = "./lib/bin";

	/**
	 * system property which if provided will override
	 * {@link #DEFAULT_LIBRARY_EXTRACT_LOCATION}
	 */
	public static final String PROPERTY_LIBRARY_EXTRACT_LOCATION = //
	PACKAGE_NAME + ".library.extract.location";

	static {
		try {
			final String location = System.getProperty(
					PROPERTY_LIBRARY_EXTRACT_LOCATION,
					DEFAULT_LIBRARY_EXTRACT_LOCATION);
			LibraryUDT.load(location);
		} catch (Throwable e) {
			log.error("failed to LOAD native library; terminating", e);
			System.exit(1);
		}
		try {
			initClass0();
		} catch (Throwable e) {
			log.error("failed to INIT native library; terminating", e);
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
	 * performance monitor; updated by {@link #updateMonitor(boolean)} in JNI
	 * 
	 * @see #updateMonitor(boolean)
	 */
	public final MonitorUDT monitor;

	/**
	 * message send mode parameters; used by JNI on each message send
	 */
	protected volatile int messageTimeTolive;
	protected volatile boolean messageIsOrdered;

	/**
	 * local end point; loaded by JNI by {@link #hasLoadedLocalSocketAddress()}
	 */
	protected volatile InetSocketAddress localSocketAddress;

	/**
	 * remote end point; loaded by JNI by
	 * {@link #hasLoadedRemoteSocketAddress()}
	 */
	protected volatile InetSocketAddress remoteSocketAddress;

	/**
	 * UDT::select() sizeArray indexes
	 */
	public static final int UDT_READ_INDEX = 0;
	public static final int UDT_WRITE_INDEX = 1;
	public static final int UDT_EXCEPT_INDEX = 2;
	public static final int UDT_SIZE_COUNT = 3;

	/**
	 * UDT::selectEx() result status
	 */
	protected boolean isSelectedRead;
	protected boolean isSelectedWrite;
	protected boolean isSelectedException;

	// ###################################################
	// ### UDT API
	// ###

	/**
	 * Call this after loading native library.
	 * 
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/startup.htm">UDT::startup()</a>
	 */
	protected static native void initClass0() throws ExceptionUDT;

	/**
	 * Call this before unloading native library.
	 * 
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/cleanup.htm.htm">UDT::cleanup()</a>
	 */
	protected static native void stopClass0() throws ExceptionUDT;

	/**
	 * used by default constructor
	 */
	protected native int initInstance0(int typeCode) throws ExceptionUDT;

	/**
	 * used by accept() internally
	 */
	protected native int initInstance1(int socketUDT) throws ExceptionUDT;

	/**
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/accept.htm">UDT::accept()</a>
	 */
	protected native SocketUDT accept0() throws ExceptionUDT;

	/**
	 * @return null : no incoming connections (non-blocking mode only)<br>
	 *         non null : newly accepted connection (both blocking and
	 *         non-blocking)<br>
	 */
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
	}

	/**
	 * @see <a href="http://www.cs.uic.edu/~ygu1/doc/bind.htm">UDT::bind()</a>
	 */
	protected native void bind0(InetSocketAddress localSocketAddress)
			throws ExceptionUDT;

	public void bind(InetSocketAddress localSocketAddress) //
			throws ExceptionUDT, IllegalArgumentException {
		checkSocketAddress(localSocketAddress);
		bind0(localSocketAddress);
	}

	/**
	 * @see <a href="http://www.cs.uic.edu/~ygu1/doc/close.htm">UDT::close()</a>
	 */
	protected native void close0() throws ExceptionUDT;

	/**
	 * @see #close0()
	 */
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
	 * @see <a http://www.cs.uic.edu/~ygu1/doc/connect.htm">UDT::connect()</a>
	 */
	protected native void connect0(InetSocketAddress remoteSocketAddress)
			throws ExceptionUDT;

	/**
	 * Note: this is always a blocking call.
	 * 
	 * @see #connect0(InetSocketAddress)
	 */
	public void connect(InetSocketAddress remoteSocketAddress) //
			throws ExceptionUDT, IllegalArgumentException {
		checkSocketAddress(remoteSocketAddress);
		connect0(remoteSocketAddress);
	}

	/**
	 * Load {@link #remoteSocketAddress} value.
	 * 
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/peername.htm">UDT::peername()</a>
	 */
	protected native boolean hasLoadedRemoteSocketAddress();

	/**
	 * @return not connected: null<br>
	 *         connected: remote UDT peer socket address<br>
	 * @see #hasLoadedRemoteSocketAddress()
	 */
	public InetSocketAddress getRemoteSocketAddress() throws ExceptionUDT {
		if (hasLoadedRemoteSocketAddress()) {
			return remoteSocketAddress;
		} else {
			return null;
		}
	}

	/**
	 * load {@link #localSocketAddress} value
	 * 
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/sockname.htm">UDT::sockname()</a>
	 */
	protected native boolean hasLoadedLocalSocketAddress();

	/**
	 * @return not bound: null<br>
	 *         bound: local UDT socket address<br>
	 * @see #hasLoadedLocalSocketAddress()
	 */
	public InetSocketAddress getLocalSocketAddress() throws ExceptionUDT {
		if (hasLoadedLocalSocketAddress()) {
			return localSocketAddress;
		} else {
			return null;
		}
	}

	/**
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/opt.htm">UDT::getsockopt()</a>
	 */
	protected native Object getOption0(int code, Class<?> klaz)
			throws ExceptionUDT;

	/**
	 * @see #getOption0(int, Class)
	 */
	public Object getOption(OptionUDT option) throws ExceptionUDT {
		if (option == null) {
			throw new NullPointerException("option == null");
		}
		return getOption0(option.code, option.klaz);
	}

	/**
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/opt.htm">UDT::setsockopt()</a>
	 */
	protected native void setOption0(int code, Class<?> klaz, Object value)
			throws ExceptionUDT;

	/**
	 * @see #setOption0(int, Class, Object)
	 */
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
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/listen.htm">UDT::listen()</a>
	 */
	protected native void listen0(int queueSize) throws ExceptionUDT;

	protected volatile int listenQueueSize;

	/**
	 * @see #listen0(int)
	 */
	public void listen(int queueSize) throws ExceptionUDT {
		if (queueSize <= 0) {
			throw new IllegalArgumentException("queueSize <= 0");
		}
		listenQueueSize = queueSize;
		listen0(queueSize);
	}

	/**
	 * @see #listen0(int)
	 */
	public int listenQueueSize() {
		return listenQueueSize;
	}

	/**
	 * receive into a complete array
	 * 
	 * @see <a href="http://www.cs.uic.edu/~ygu1/doc/recv.htm">UDT::recv()</a>
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/recv.htm">UDT::recvmsg()</a>
	 */
	protected native int receive0(int socketID, int socketType, //
			byte[] array) throws ExceptionUDT;

	/**
	 * receive into a portion of an array
	 * 
	 * @see <a href="http://www.cs.uic.edu/~ygu1/doc/recv.htm">UDT::recv()</a>
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/recv.htm">UDT::recvmsg()</a>
	 */
	protected native int receive1(int socketID, int socketType, //
			byte[] array, int position, int limit) throws ExceptionUDT;

	/**
	 * receive into a {@link java.nio.channels.DirectByteBuffer}
	 * 
	 * @see <a href="http://www.cs.uic.edu/~ygu1/doc/recv.htm">UDT::recv()</a>
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/recv.htm">UDT::recvmsg()</a>
	 */
	protected native int receive2(int socketID, int socketType, //
			ByteBuffer buffer, int position, int limit) throws ExceptionUDT;

	/**
	 * receive into byte[] array upto array.length bytes
	 * 
	 * @return <code>-1</code> : nothing received (non-blocking only)<br>
	 *         <code>=0</code> : timeout expired (blocking only)<br>
	 *         <code>>0</code> : normal receive, byte count<br>
	 */
	public int receive(byte[] array) throws ExceptionUDT {
		checkArray(array);
		return receive0(socketID, socketType, array);
	}

	/**
	 * receive into byte[] array upto size=limit-position bytes
	 * 
	 * @return <code>-1</code> : nothing received (non-blocking only)<br>
	 *         <code>=0</code> : timeout expired (blocking only)<br>
	 *         <code>>0</code> : normal receive, byte count<br>
	 */
	public int receive(byte[] array, int position, int limit)
			throws ExceptionUDT {
		checkArray(array);
		return receive1(socketID, socketType, array, position, limit);
	}

	/**
	 * receive into {@link java.nio.channels.DirectByteBuffer}; upto
	 * {@link java.nio.ByteBuffer#remaining()} bytes
	 * 
	 * @return <code>-1</code> : nothing received (non-blocking only)<br>
	 *         <code>=0</code> : timeout expired (blocking only)<br>
	 *         <code>>0</code> : normal receive, byte count<br>
	 */
	public int receive(ByteBuffer buffer) throws ExceptionUDT {
		checkBuffer(buffer);
		final int position = buffer.position();
		final int limit = buffer.limit();
		final int remaining = buffer.remaining();
		final int sizeReceived = receive2(socketID, socketType, //
				buffer, position, limit);
		if (sizeReceived <= 0) {
			return sizeReceived;
		}
		if (sizeReceived <= remaining) {
			buffer.position(position + sizeReceived);
			return sizeReceived;
		} else { // should not happen
			log.error("sizeReceived > remaining");
			return 0;
		}
	}

	/**
	 * WRAPPER_UNIMPLEMENTED
	 * 
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/recvfile.htm">UDT::recvfile()</a>
	 */
	public int receiveFile(ByteBuffer buffer) throws ExceptionUDT {
		throw new ExceptionUDT(//
				socketID, ErrorUDT.WRAPPER_UNIMPLEMENTED, "receiveFile");
	}

	/**
	 * @see com.barchart.udt.nio.SelectorUDT#select()
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/select.htm">UDT::select()</a>
	 */
	protected static native int select0( //
			int[] readArray, //
			int[] writeArray, //
			int[] exceptArray, //
			int[] sizeArray, //
			long millisTimeout //
	) throws ExceptionUDT;

	/**
	 * Timeout is in milliseconds.
	 * 
	 * @return <code><0</code> : should not happen<br>
	 *         <code>=0</code> : timeout, no ready sockets<br>
	 *         <code>>0</code> : total number or reads, writes, exceptions<br>
	 * @see #select0(int[], int[], int[], int[], long)
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

	// #############################

	protected void checkBuffer(ByteBuffer buffer) {
		if (buffer == null) {
			throw new IllegalArgumentException("buffer == null");
		}
		if (!buffer.isDirect()) {
			throw new IllegalArgumentException("must use DirectByteBuffer");
		}
	}

	protected void checkArray(byte[] array) {
		if (array == null) {
			throw new IllegalArgumentException("array == null");
		}
	}

	//

	/**
	 * send from a complete array;
	 * 
	 * wrapper for <em>UDT::send()</em>, <em>UDT::sendmsg()</em>
	 * 
	 * @see <a href="http://www.cs.uic.edu/~ygu1/doc/send.htm">UDT::send()</a>
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/sendmsg.htm">UDT::sendmsg()</a>
	 */
	protected native int send0(int socketID, int socketType, //
			int timeToLive, boolean isOrdered, //
			byte[] array) throws ExceptionUDT;

	/**
	 * send from a portion of an array;
	 * 
	 * wrapper for <em>UDT::send()</em>, <em>UDT::sendmsg()</em>
	 * 
	 * @see <a href="http://www.cs.uic.edu/~ygu1/doc/send.htm">UDT::send()</a>
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/sendmsg.htm">UDT::sendmsg()</a>
	 */
	protected native int send1(int socketID, int socketType, //
			int timeToLive, boolean isOrdered, //
			byte[] array, int arayPosition, int arrayLimit) throws ExceptionUDT;

	/**
	 * send from {@link java.nio.DirectByteBuffer};
	 * 
	 * wrapper for <em>UDT::send()</em>, <em>UDT::sendmsg()</em>
	 * 
	 * @see <a href="http://www.cs.uic.edu/~ygu1/doc/send.htm">UDT::send()</a>
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/sendmsg.htm">UDT::sendmsg()</a>
	 */
	protected native int send2(int socketID, int socketType, //
			int timeToLive, boolean isOrdered, //
			ByteBuffer buffer, int bufferPosition, int bufferLimit)
			throws ExceptionUDT;

	/**
	 * send from byte[] array upto <code>size=array.length</code> bytes
	 * 
	 * @param array
	 *            array to send
	 * @return <code>-1</code> : no buffer space (non-blocking only) <br>
	 *         <code>=0</code> : timeout expired (blocking only) <br>
	 *         <code>>0</code> : normal send, actual sent byte count <br>
	 * @see #send0(int, int, int, boolean, byte[])
	 */
	public int send(byte[] array) throws ExceptionUDT {
		checkArray(array);
		return send0(socketID, socketType, //
				messageTimeTolive, messageIsOrdered, //
				array);
	}

	/**
	 * send from byte[] array upto <code>size=limit-position</code> bytes
	 * 
	 * @param array
	 *            array to send
	 * @param position
	 *            start of array portion to send
	 * @param limit
	 *            finish of array portion to send
	 * @return <code>-1</code> : no buffer space (non-blocking only) <br>
	 *         <code>=0</code> : timeout expired (blocking only) <br>
	 *         <code>>0</code> : normal send, actual sent byte count <br>
	 * @see #send1(int, int, int, boolean, byte[], int, int)
	 */
	public int send(byte[] array, int position, int limit) throws ExceptionUDT {
		checkArray(array);
		return send1(socketID, socketType, //
				messageTimeTolive, messageIsOrdered, //
				array, position, limit);
	}

	/**
	 * send from {@link java.nio.DirectByteBuffer}, upto
	 * {@link java.nio.ByteBuffer#remaining()} bytes
	 * 
	 * @param buffer
	 *            buffer to send
	 * @return <code>-1</code> : no buffer space (non-blocking only)<br>
	 *         <code>=0</code> : timeout expired (blocking only)<br>
	 *         <code>>0</code> : normal send, actual sent byte count<br>
	 * @see #send2(int, int, int, boolean, ByteBuffer, int, int)
	 */
	public int send(ByteBuffer buffer) throws ExceptionUDT {
		checkBuffer(buffer);
		final int position = buffer.position();
		final int limit = buffer.limit();
		final int remaining = buffer.remaining();
		final int sizeSent = send2(socketID, socketType, //
				messageTimeTolive, messageIsOrdered, //
				buffer, position, limit);
		if (sizeSent <= 0) {
			return sizeSent;
		}
		if (sizeSent <= remaining) {
			buffer.position(position + sizeSent);
			return sizeSent;
		} else { // should not happen
			log.error("sizeSent > remaining");
			return 0;
		}
	}

	/**
	 * default timeToLive value used by sendmsg mode
	 * 
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/sendmsg.htm">UDT::sendmsg()</a>
	 */
	public void setMessageTimeTolLive(int timeToLive) {
		// publisher to volatile
		messageTimeTolive = timeToLive;
	}

	/**
	 * default isOrdered value used by sendmsg mode
	 * 
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/sendmsg.htm">UDT::sendmsg()</a>
	 */
	public void setMessageIsOdered(boolean isOrdered) {
		// publisher to volatile
		messageIsOrdered = isOrdered;
	}

	/**
	 * WRAPPER_UNIMPLEMENTED
	 * 
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/sendfile.htm">UDT::sendfile()</a>
	 */
	public int sendFile(ByteBuffer buffer) throws ExceptionUDT {
		throw new ExceptionUDT(//
				socketID, ErrorUDT.WRAPPER_UNIMPLEMENTED, "sendFile");
	}

	/**
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/perfmon.htm">perfmon.htm</a>
	 */
	protected native void updateMonitor0(boolean makeClear) throws ExceptionUDT;

	/**
	 * load updated statistics values into {@link #monitor} object
	 * 
	 * @param makeClear
	 *            if true, reset all statistics with this call
	 * @see #updateMonitor0(boolean)
	 */
	public void updateMonitor(boolean makeClear) throws ExceptionUDT {
		updateMonitor0(makeClear);
	}

	/**
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/t-error.htm">t-error.htm</a>
	 */
	protected native int getErrorCode0();

	/**
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/t-error.htm">t-error.htm</a>
	 */
	public int getErrorCode() {
		return getErrorCode0();
	}

	/**
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/t-error.htm">t-error.htm</a>
	 */
	protected native String getErrorMessage0();

	/**
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/t-error.htm">t-error.htm</a>
	 */
	public String getErrorMessage() {
		return getErrorMessage0();
	}

	/**
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/t-error.htm">t-error.htm</a>
	 */
	protected native void clearError0();

	/**
	 * @see <a
	 *      href="http://www.cs.uic.edu/~ygu1/doc/t-error.htm">t-error.htm</a>
	 */
	public void clearError() {
		clearError0();
	}

	/**
	 * not yet available in UDT; use a native hack to detect open state
	 */
	protected native boolean isOpen0();

	/**
	 * test if socket is open
	 * 
	 * @see #isOpen0()
	 */
	public boolean isOpen() {
		return isOpen0();
	}

	/**
	 * test if socket is closed
	 * 
	 * @see #isOpen0()
	 */
	public boolean isClosed() {
		return !isOpen0();
	}

	// ###
	// ### UDT API
	// ###################################################

	/**
	 * Apply default settings for message mode.
	 * <p>
	 * IsOdered = true;<br>
	 * TimeTolLive = INFINITE_TTL;<br>
	 */
	public void setDefaultMessageSendMode() {
		setMessageIsOdered(true);
		setMessageTimeTolLive(INFINITE_TTL);
	}

	/**
	 * Primary socket. Default constructor; will apply
	 * {@link #setDefaultMessageSendMode()}
	 * 
	 * @param type
	 *            UDT socket type
	 */
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

	/**
	 * Secondary socket; made by {@link #accept0()}, will apply
	 * {@link #setDefaultMessageSendMode()}
	 * 
	 * @param socketID
	 *            UDT socket descriptor;
	 */
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

	/**
	 * @return true if {@link #bind(InetSocketAddress)} was successful
	 */
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

	/**
	 * @return true if {@link #connect(InetSocketAddress)} was successful
	 */
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

	/**
	 * @param block
	 *            true : set both send and receive to blocking mode; false : set
	 *            both send and receive to non-blocking mode
	 */
	public void configureBlocking(boolean block) throws ExceptionUDT {
		if (block) {
			setOption(OptionUDT.Is_Receive_Synchronous, Boolean.TRUE);
			setOption(OptionUDT.Is_Send_Synchronous, Boolean.TRUE);
		} else {
			setOption(OptionUDT.Is_Receive_Synchronous, Boolean.FALSE);
			setOption(OptionUDT.Is_Send_Synchronous, Boolean.FALSE);
		}
	}

	/**
	 * Protocol level parameter.
	 */
	public int getSendBufferSize() throws ExceptionUDT {
		return (Integer) getOption(OptionUDT.Protocol_Send_Buffer_Size);
	}

	/**
	 * Protocol level parameter.
	 */
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
			// map from UDT value convention to java value convention
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

	/**
	 * Protocol level parameter.
	 */
	public void setSendBufferSize(int size) throws ExceptionUDT {
		setOption(OptionUDT.Protocol_Send_Buffer_Size, size);
	}

	/**
	 * Protocol level parameter.
	 */
	public void setReceiveBufferSize(int size) throws ExceptionUDT {
		setOption(OptionUDT.Protocol_Receive_Buffer_Size, size);
	}

	public void setReuseAddress(boolean on) throws ExceptionUDT {
		setOption(OptionUDT.Is_Address_Reuse_Enabled, on);
	}

	protected static final LingerUDT LINGER_ZERO = new LingerUDT(0);

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

	/**
	 * @return null : not connected<br>
	 */
	public InetAddress getRemoteInetAddress() {
		try {
			InetSocketAddress remote = getRemoteSocketAddress();
			if (remote == null) {
				return null;
			} else {
				return remote.getAddress();
			}
		} catch (ExceptionUDT e) {
			log.debug("unexpected", e);
			return null;
		}
	}

	/**
	 * @return 0 : not connected<br>
	 */
	public int getRemoteInetPort() {
		try {
			InetSocketAddress remote = getRemoteSocketAddress();
			if (remote == null) {
				return 0;
			} else {
				return remote.getPort();
			}
		} catch (ExceptionUDT e) {
			log.debug("unexpected", e);
			return 0;
		}
	}

	/**
	 * @return null : not bound<br>
	 */
	public InetAddress getLocalInetAddress() {
		try {
			InetSocketAddress local = getLocalSocketAddress();
			if (local == null) {
				return null;
			} else {
				return local.getAddress();
			}
		} catch (ExceptionUDT e) {
			log.debug("unexpected", e);
			return null;
		}
	}

	/**
	 * @return 0 : not bound<br>
	 */
	public int getLocalInetPort() {
		try {
			InetSocketAddress local = getLocalSocketAddress();
			if (local == null) {
				return 0;
			} else {
				return local.getPort();
			}
		} catch (ExceptionUDT e) {
			log.debug("unexpected", e);
			return 0;
		}
	}

	//

	/**
	 * Note: uses {@link #socketID} as hash code.
	 */
	@Override
	public int hashCode() {
		return socketID;
	}

	/**
	 * Note: equality is based on {@link #socketID}.
	 */
	@Override
	public boolean equals(Object otherSocketUDT) {
		if (otherSocketUDT instanceof SocketUDT) {
			SocketUDT other = (SocketUDT) otherSocketUDT;
			return other.socketID == this.socketID;
		}
		return false;
	}

	// #############################
	// ### used for development only
	// ###

	native void testEmptyCall0();

	native void testIterateArray0(Object[] array);

	native void testIterateSet0(Set<Object> set);

	native int[] testMakeArray0(int size);

	native void testGetSetArray0(int[] array, boolean isReturn);

	native void testInvalidClose0(int socketID) throws ExceptionUDT;

	native void testCrashJVM0();

	native void testDirectBufferAccess0(ByteBuffer buffer);

	native void testFillArray0(byte[] array);

	native void testFillBuffer0(ByteBuffer buffer);

	// ###
	// ### used for development only
	// #############################

}
