package ar.thorium.handler;

import ar.thorium.dispatcher.Dispatcher;
import ar.thorium.utils.ChannelFacade;
import org.apache.log4j.Logger;

import java.nio.channels.SelectionKey;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class HandlerFutureTask<H extends EventHandler, F extends ChannelFacade, A extends HandlerAdapter<H>>
		extends FutureTask<A> implements Runnable {

	private final HandlerAdapter<H> adapter;
	private Logger logger = Logger.getLogger(HandlerFutureTask.class);
	private Dispatcher<H,F,A> dispatcher;
	private SelectionKey key;
	
	@SuppressWarnings("unchecked")
	public HandlerFutureTask(A adapter, Dispatcher<H,F,A> dispatcher, SelectionKey key) {
		super((Callable<A>) adapter);
		this.adapter = adapter;
		this.dispatcher = dispatcher;
	}

	@SuppressWarnings("unchecked")
	protected void done() {
		dispatcher.enqueueStatusChange((A)adapter, key);

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
