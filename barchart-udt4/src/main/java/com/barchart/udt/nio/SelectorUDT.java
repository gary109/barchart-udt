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
package com.barchart.udt.nio;

import static java.nio.channels.SelectionKey.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.IllegalSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

public class SelectorUDT extends AbstractSelector {

	private static final Logger log = LoggerFactory
			.getLogger(SelectorUDT.class);

	//

	/**
	 * note that you should not use JDK Selector.open() at all
	 */
	public static Selector open() throws IOException {
		SelectorProviderUDT provider = SelectorProviderUDT.datagramProvider;
		log.warn("using default selection provider; type={}", provider.type);
		return provider.openSelector();
	}

	/**
	 * use this call to instantiate a selector for UDT
	 */
	protected static Selector open(TypeUDT type) throws IOException {
		final SelectorProviderUDT provider;
		switch (type) {
		case DATAGRAM:
			provider = SelectorProviderUDT.datagramProvider;
			break;
		case STREAM:
			provider = SelectorProviderUDT.streamProvider;
			break;
		default:
			log.error("unsupported type={}", type);
			throw new IOException("unsupported type");
		}
		return provider.openSelector();
	}

	//

	@Override
	protected void implCloseSelector() throws IOException {
		wakeup();
		synchronized (this) {
			synchronized (publicRegisteredKeySet) {
				synchronized (publicSelectedKeySet) {

					selectedKeySet.clear();

					for (SelectionKey key : registeredKeySet) {
						try {
							key.channel().close();
						} catch (Throwable e) {
							log.error("unexpected", e);
						}
					}

					registeredKeySet.clear();

				}
			}
		}
	}

	// NOE: register() and select() are blocking each other
	@Override
	protected SelectionKey register(AbstractSelectableChannel channel,
			int interestOps, Object attachment) {

		if (!(channel instanceof ChannelUDT)) {
			// also takes care of null
			log.error("!(channel instanceof ChannelUDT)");
			throw new IllegalSelectorException();
		}

		synchronized (publicRegisteredKeySet) {

			if (publicRegisteredKeySet.size() == maximimSelectorSize) {
				log.error("reached maximimSelectorSize)");
				throw new IllegalSelectorException();
			}

			ChannelUDT channelUDT = (ChannelUDT) channel;

			SocketUDT socketUDT = channelUDT.getSocketUDT();

			SelectionKeyUDT keyUDT = new SelectionKeyUDT(//
					this, channelUDT, attachment, interestOps);

			// the only place with "add"
			registeredKeyMap.put(socketUDT.socketID, keyUDT);
			registeredKeySet.add(keyUDT);

			return keyUDT;

		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<SelectionKey> keys() {
		if (!isOpen()) {
			throw new ClosedSelectorException();
		}
		return (Set<SelectionKey>) publicRegisteredKeySet;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<SelectionKey> selectedKeys() {
		if (!isOpen()) {
			throw new ClosedSelectorException();
		}
		return (Set<SelectionKey>) publicSelectedKeySet;
	}

	@Override
	public int select() throws IOException {
		return select(0);
	}

	@Override
	// per java.nio.Selector contract 0 input means infinite
	public int select(long timeout) throws IOException {
		if (timeout < 0) {
			throw new IllegalArgumentException("Negative timeout");
		}
		long timeoutUDT = (timeout == 0) ? UDT_TIMEOUT_INFINITE : timeout;
		return doSelectInsideLock(timeoutUDT);
	}

	@Override
	public int selectNow() throws IOException {
		return doSelectInsideLock(UDT_TIMEOUT_NONE);
	}

	/**
	 * NOTE: it is recommended to user timed select(timeout) (timeout = 100
	 * millis) to avoid missed wakeup() which rare but are possible in current
	 * implentation (accepted in favor of design simplicity / performance)
	 */
	@Override
	public Selector wakeup() {
		// publisher for volatile
		wakeupStepCount++;
		return this;
	}

	protected volatile int wakeupStepCount;

	protected int wakeupBaseCount;

	protected void saveWakeupBase() {
		wakeupBaseCount = wakeupStepCount;
	}

	protected boolean isWakeupPending() {
		return wakeupBaseCount != wakeupStepCount;
	}

	// #######################################################################

	/**
	 * Private views of the key sets
	 */

	/**
	 * The set of keys with data ready for an operation
	 */
	protected final Set<SelectionKeyUDT> selectedKeySet;
	/**
	 * The set of keys registered with this Selector
	 */
	protected final Set<SelectionKeyUDT> registeredKeySet;

	// Public views of the key sets

	// totally immutable
	protected final Set<? extends SelectionKey> publicRegisteredKeySet;
	// partially immutable: removal allowed, but not addition
	protected final Set<? extends SelectionKey> publicSelectedKeySet;
	// mutable: requests for cancel
	protected final Set<SelectionKey> cancelledKeySet;

	protected final Map<Integer, SelectionKeyUDT> registeredKeyMap;

	/**
	 * used by SocketUDT.select(); performace optimization: used final arrays
	 */
	public final int maximimSelectorSize;
	protected final int[] readArray;
	protected final int[] writeArray;
	protected final int[] exceptArray;
	protected final int[] sizeArray;

	/**
	 * connector thread pool limit
	 */
	public final int maximumConnectorSize;
	protected final ConnectorPoolUDT connectorPool;

	protected SelectorUDT(SelectorProvider provider, //
			int maximumSelectorSize, int maximumConnectorSize) {

		super(provider);

		registeredKeyMap = new HashMap<Integer, SelectionKeyUDT>();

		registeredKeySet = new HashSet<SelectionKeyUDT>();
		selectedKeySet = new HashSet<SelectionKeyUDT>();
		cancelledKeySet = cancelledKeys();

		publicRegisteredKeySet = Collections.unmodifiableSet(registeredKeySet);
		publicSelectedKeySet = HelperNIOUDT.ungrowableSet(selectedKeySet);

		this.maximimSelectorSize = maximumSelectorSize;
		readArray = new int[maximumSelectorSize];
		writeArray = new int[maximumSelectorSize];
		exceptArray = new int[maximumSelectorSize];
		sizeArray = new int[SocketUDT.UDT_SIZE_COUNT];

		this.maximumConnectorSize = maximumConnectorSize;
		connectorPool = new ConnectorPoolUDT(maximumConnectorSize);

	}

	public static int UDT_TIMEOUT_INFINITE = -1;

	public static int UDT_TIMEOUT_NONE = 0;

	protected int doSelectInsideLock(long millisTimeout) throws IOException {
		if (!isOpen()) {
			throw new ClosedSelectorException();
		}
		synchronized (this) {
			synchronized (publicRegisteredKeySet) {
				synchronized (publicSelectedKeySet) {
					return doSelectReally(millisTimeout);
				}
			}
		}
	}

	/**
	 * note: millisTimeout input values contract:
	 * 
	 * -1 : infinite
	 * 
	 * =0 : immediate (but really up to 10 ms UDT resolution slice)
	 * 
	 * >0 : finite
	 */
	/**
	 * note: return value contract:
	 * 
	 * ==0 : means nothing was selected/timeout
	 * 
	 * !=0 : number of pending r/w ops, NOT number of selected keys
	 * */
	protected int doSelectReally(long millisTimeout) throws IOException {

		processCancelled();

		selectedKeySet.clear();

		int updateCount = 0;

		trySelect: try {

			begin(); // java.nio.Selector contract for wakeup()

			// ### pre select

			int readSize = 0;
			int writeSize = 0;

			for (SelectionKeyUDT keyUDT : registeredKeySet) {
				keyUDT.readyOps = 0; // publisher for volatile
				final int interestOps = keyUDT.interestOps;
				final int socketID = keyUDT.socketID;
				final KindUDT channelType = keyUDT.channelUDT.getChannelKind();
				if ((interestOps & (OP_ACCEPT)) != 0) {
					assert channelType == KindUDT.ACCEPTOR;
					readArray[readSize++] = socketID;
				}
				if ((interestOps & (OP_READ)) != 0) {
					assert channelType == KindUDT.CONNECTOR;
					readArray[readSize++] = socketID;
				}
				if ((interestOps & (OP_WRITE)) != 0) {
					assert channelType == KindUDT.CONNECTOR;
					writeArray[writeSize++] = socketID;
				}
				if ((interestOps & (OP_CONNECT)) != 0) {
					assert channelType == KindUDT.CONNECTOR;
					assert (interestOps & (OP_READ | OP_WRITE | OP_ACCEPT)) == 0;
					// UDT does not support select() for connect() operation
					// using thread pool instead
				}
			}

			// set sizes
			sizeArray[SocketUDT.UDT_READ_INDEX] = readSize;
			sizeArray[SocketUDT.UDT_WRITE_INDEX] = writeSize;
			sizeArray[SocketUDT.UDT_EXCEPT_INDEX] = 0;

			// ### into select

			saveWakeupBase();

			if (millisTimeout < 0) {
				/* infinite; do select in slices; check for wakeup; */
				do {
					updateCount = SocketUDT.select(readArray, writeArray,
							exceptArray, sizeArray,
							SocketUDT.DEFAULT_MIN_SELECTOR_TIMEOUT);
					if (updateCount > 0 || isWakeupPending()) {
						break;
					}
				} while (true);
			} else if (millisTimeout > 0) {
				/* finite; do select in slices; check for wakeup; */
				do {
					updateCount = SocketUDT.select(readArray, writeArray,
							exceptArray, sizeArray,
							SocketUDT.DEFAULT_MIN_SELECTOR_TIMEOUT);
					if (updateCount > 0 || isWakeupPending()) {
						break;
					}
					millisTimeout -= SocketUDT.DEFAULT_MIN_SELECTOR_TIMEOUT;
				} while (millisTimeout > 0);
			} else { // millisTimeout == 0
				/* immedeate; one shot select */
				updateCount = SocketUDT.select(readArray, writeArray,
						exceptArray, sizeArray, 0);
			}

			if (updateCount == 0) {
				// timeout, nothing is ready; no need to process post select
				break trySelect;
			}

			// ### post select

			// get sizes
			final int readReturnSize = sizeArray[SocketUDT.UDT_READ_INDEX];
			final int writeReturnSize = sizeArray[SocketUDT.UDT_WRITE_INDEX];
			final int exceptReturnSize = sizeArray[SocketUDT.UDT_EXCEPT_INDEX];

			// add ready to selected
			updateRead(readReturnSize);
			updateWrite(writeReturnSize);
			updateExcept(exceptReturnSize);

		} finally {

			end(); // java.nio.Selector contract for wakeup()

		}

		// using thread pool based connect() processor
		updateCount += updateConnect();

		return updateCount;

	}

	protected int updateConnect() {
		final Queue<SelectionKeyUDT> readyQueue = connectorPool.readyQueue;
		if (readyQueue.isEmpty()) {
			return 0;
		}
		int updateCount = 0;
		SelectionKeyUDT keyUDT;
		while ((keyUDT = readyQueue.poll()) != null) {
			// contract:
			assert keyUDT.channelUDT.getChannelKind() == KindUDT.CONNECTOR;
			assert registeredKeySet.contains(keyUDT);
			assert (keyUDT.interestOps & OP_CONNECT) != 0;
			assert (keyUDT.interestOps & (OP_READ | OP_WRITE | OP_ACCEPT)) == 0;
			//
			keyUDT.readyOps |= OP_CONNECT;
			selectedKeySet.add(keyUDT);
			updateCount++;
		}
		return updateCount;
	}

	protected void updateRead(int readSize) {
		for (int k = 0; k < readSize; k++) {
			final int socketId = readArray[k];
			final SelectionKeyUDT keyUDT = registeredKeyMap.get(socketId);
			assert keyUDT != null;
			switch (keyUDT.channelUDT.getChannelKind()) {
			case ACCEPTOR:
				keyUDT.readyOps |= OP_ACCEPT;
				break;
			case CONNECTOR:
				keyUDT.readyOps |= OP_READ;
				break;
			default:
				assert false : "unexpected default";
				continue;
			}
			selectedKeySet.add(keyUDT);
		}
	}

	protected void updateWrite(int writeSize) {
		for (int k = 0; k < writeSize; k++) {
			final int socketId = writeArray[k];
			final SelectionKeyUDT keyUDT = registeredKeyMap.get(socketId);
			assert keyUDT != null;
			switch (keyUDT.channelUDT.getChannelKind()) {
			case ACCEPTOR:
				assert false : "unexpected ACCEPTOR";
				continue;
			case CONNECTOR:
				keyUDT.readyOps |= OP_WRITE;
				break;
			default:
				assert false : "unexpected default";
				continue;
			}
			selectedKeySet.add(keyUDT);
		}
	}

	protected void updateExcept(int exceptSize) {
		for (int k = 0; k < exceptSize; k++) {
			int socketId = exceptArray[k];
			SelectionKeyUDT keyUDT = registeredKeyMap.get(socketId);
			assert keyUDT != null;
			switch (keyUDT.channelUDT.getChannelKind()) {
			case ACCEPTOR:
			case CONNECTOR:
				// set all ready OPS to throw on any operation
				keyUDT.readyOps |= keyUDT.channel().validOps();
				break;
			default:
				assert false : "unexpected default";
				continue;
			}
			selectedKeySet.add(keyUDT);
		}
	}

	protected void processCancelled() throws IOException {
		/*
		 * Precondition: Synchronized on this, publicRegisteredKeySet,
		 * publicSelectedKeySet
		 */
		synchronized (cancelledKeySet) {
			if (!cancelledKeySet.isEmpty()) {
				for (SelectionKey key : cancelledKeySet) {

					SelectionKeyUDT keyUDT = (SelectionKeyUDT) key;

					// the only place with "remove"
					SelectionKeyUDT removed = registeredKeyMap
							.remove(keyUDT.socketID);
					boolean isRemoved = registeredKeySet.remove(keyUDT);

					assert removed != null;
					assert isRemoved;

				}
				cancelledKeySet.clear();
			}
		}
	}

	void submitConnectRequest(SelectionKeyUDT keyUDT, InetSocketAddress remote)
			throws IOException {

		// TODO think again if lack of sync is OK?
		if (!registeredKeySet.contains(keyUDT)) {
			throw new IOException("connect while not registered");
		}

		if ((keyUDT.interestOps & OP_CONNECT) == 0) {
			throw new IOException("connect while not interested");
		}

		if ((keyUDT.interestOps & (OP_READ | OP_WRITE)) != 0) {
			throw new IOException("OP_CONNECT is not sole interest");
		}

		connectorPool.submitRequest(keyUDT, remote);

	}

}
