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
			get();
		} catch (ExecutionException e) {
			adapter.die();
			logger.error("Handler died.", e.getCause());
		} catch (InterruptedException e) {
			Thread.interrupted();
			logger.error("Handler interrupted.", e);
		}
	}

}
