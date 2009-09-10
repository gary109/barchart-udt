package com.barchart.udt.nio;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;

class ConnectorExecutorsUDT {

	private static final RejectedExecutionHandler defaultHandler = new AbortPolicy();

	public static ExecutorService newCachedThreadPool(int corePoolSize,
			int maximumPoolSize, long keepAliveTime, ThreadFactory threadFactory) {

		BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();

		return new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
				keepAliveTime, TimeUnit.SECONDS, workQueue, threadFactory,
				defaultHandler);

	}

}
