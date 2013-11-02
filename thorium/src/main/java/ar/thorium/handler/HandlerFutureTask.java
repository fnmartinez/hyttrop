package ar.thorium.handler;

import ar.thorium.dispatcher.Dispatcher;
import org.apache.log4j.Logger;

import java.nio.channels.SelectionKey;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class HandlerFutureTask extends FutureTask<HandlerAdapter> implements Runnable {

	private final HandlerAdapter adapter;
	private final Logger logger = Logger.getLogger(HandlerFutureTask.class);
	private final Dispatcher dispatcher;
	private SelectionKey key;
	
	@SuppressWarnings("unchecked")
	public HandlerFutureTask(HandlerAdapter adapter, Dispatcher dispatcher, SelectionKey key) {
		super(adapter);
		this.adapter = adapter;
		this.dispatcher = dispatcher;
        this.key = key;
	}

	@SuppressWarnings("unchecked")
	protected void done() {
		dispatcher.enqueueStatusChange(adapter, key);

		try {
			// Get result returned by call(), or cause
			// deferred exception to be thrown. We know
			// the result will be the adapter instance
			// stored above, so we ignore it.
			get();

			// Extension point: You may choose to extend the
			// InputHandler and HandlerAdapter classes to add
			// methods for handling these exceptions. This
			// method is still running in the worker thread.
		} catch (ExecutionException e) {
			adapter.die();
			logger.error("Handler died", e.getCause());
		} catch (InterruptedException e) {
			Thread.interrupted();
			logger.error("Handler interrupted", e);
		}
	}

}
