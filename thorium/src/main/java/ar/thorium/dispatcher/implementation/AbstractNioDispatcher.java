package ar.thorium.dispatcher.implementation;

import ar.edu.itba.it.pdc.jabxy.network.dispatcher.Dispatcher;
import ar.edu.itba.it.pdc.jabxy.network.dispatcher.SelectorGuard;
import ar.edu.itba.it.pdc.jabxy.network.handler.EventHandler;
import ar.edu.itba.it.pdc.jabxy.network.handler.HandlerAdapter;
import ar.edu.itba.it.pdc.jabxy.network.queues.InputQueueFactory;
import ar.edu.itba.it.pdc.jabxy.network.queues.OutputQueueFactory;
import ar.edu.itba.it.pdc.jabxy.network.utils.ChannelFacade;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;

public abstract class AbstractNioDispatcher<H extends EventHandler, F extends ChannelFacade, A extends HandlerAdapter<H>> 
				implements Dispatcher<H,F,A>, Runnable {
	// TODO: revisar la posibilidad de cambiar a AtomicReference el Selector
	// para evitar el SelectorGuard
	private final Logger logger = Logger.getLogger(getClass().getName());
	private final Selector selector;
	private final BlockingQueue<Pair<A, Object>> statusChangeQueue;
	private final SelectorGuard guard;
	private volatile boolean dispatching = true;
	protected final Executor executor;
	protected final InputQueueFactory inputQueueFactory;
	protected final OutputQueueFactory outputQueueFactory;
	
	public AbstractNioDispatcher(Executor executor, SelectorGuard guard, InputQueueFactory inputQueueFactory, OutputQueueFactory outputQueueFactory) throws IOException{
		this.inputQueueFactory = inputQueueFactory;
		this.outputQueueFactory = outputQueueFactory;
		this.executor = executor;
		this.guard = guard;
		
		statusChangeQueue = new ArrayBlockingQueue<Pair<A, Object>>(100);
		selector = Selector.open();
		this.guard.setSelector(this.selector);
	}
	
	protected abstract void invokeHandler(A adapter, SelectionKey key);

	@SuppressWarnings("unchecked")
	@Override
	public void dispatch() throws IOException {
		while (dispatching) {
			guard.selectorBarrier();

			selector.select();

			checkStatusChangeQueue();

			Set<SelectionKey> keys = selector.selectedKeys();

			for (SelectionKey key : keys) {
				A adapter = (A) key.attachment();

				invokeHandler(adapter, key);
			}

			keys.clear();
		}
	}

	@Override
	public void shutdown() {
		dispatching = false;

		selector.wakeup();
	}

	@Override
	public void enqueueStatusChange(A adapter, Object handle) {
		boolean interrupted = false;
		Pair<A, Object> pair = new Pair<A, Object>(adapter, handle);
		try {
			while (true) {
				try {
					statusChangeQueue.put(pair);
					selector.wakeup();
					return;
				} catch (InterruptedException e) {
					interrupted = true;
				}
			}
		} finally {
			if (interrupted)
				Thread.currentThread().interrupt();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try {
			dispatch();
		} catch (IOException e) {
			logger.error("Unexpected I/O Exception", e);
		}

		Set<SelectionKey> keys = selector.selectedKeys();

		for (SelectionKey key : keys) {
			A adapter = (A) key.attachment();

			unregisterChannel((F)adapter);
		}

		try {
			selector.close();
		} catch (IOException e) {
			logger.error("Unexpected I/O Exception closing selector", e);
		}
	}

	@SuppressWarnings("unchecked")
	private void checkStatusChangeQueue() {
		Pair<A, Object> pair;

		while ((pair = statusChangeQueue.poll()) != null) {
			A adapter = pair.getObject();
			Object handle = pair.getHandle();
			
			if (adapter.isDead()) {
				unregisterChannel((F)adapter);
			} else {
				adapter.confirmSelection(handle);
			}
		}
	}

	@Override
	public Thread start() {
		Thread thread = new Thread(this);

		thread.start();

		return thread;
	}
	
	public Selector getSelector() {
		return this.selector;
	}
	
	void acquireSelector() {
		guard.acquireSelector();
	}
	
	void releaseSelector() {
		guard.releaseSelector();
	}
	
	private class Pair<T, S> {
		private final T object;
		private final S handle;
		
		public Pair(T object, S handle) {
			this.object = object;
			this.handle = handle;
		}
		
		public T getObject() {
			return this.object;
		}
		
		public S getHandle() {
			return this.handle;
		}
	}
}
