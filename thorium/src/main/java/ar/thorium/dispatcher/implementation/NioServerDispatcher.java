package ar.thorium.dispatcher.implementation;

import ar.thorium.dispatcher.SelectorGuard;
import ar.thorium.handler.HandlerFutureTask;
import ar.thorium.handler.ServerEventHandler;
import ar.thorium.handler.ServerHandlerAdapter;
import ar.thorium.queues.InputQueueFactory;
import ar.thorium.queues.OutputQueueFactory;
import ar.thorium.queues.exceptions.QueueBuildingException;
import ar.thorium.utils.ChannelFacade;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.concurrent.Executor;

public class NioServerDispatcher<T extends ServerEventHandler> extends AbstractNioDispatcher<T , ChannelFacade, ServerHandlerAdapter<T>> {
	// TODO: revisar la posibilidad de cambiar a AtomicReference el Selector
	// para evitar el SelectorGuard
	private final Logger logger = Logger.getLogger(getClass().getName());

	public NioServerDispatcher(Executor executor, SelectorGuard guard, InputQueueFactory inputQueueFactory, OutputQueueFactory outputQueueFactory) throws IOException {
		super(executor, guard, inputQueueFactory, outputQueueFactory);
	}

	@Override
	public ChannelFacade registerChannel(SelectableChannel channel,
			T handler) throws IOException {
		channel.configureBlocking(false);

		ServerHandlerAdapter<T> adapter;
		try {
			adapter = new ServerHandlerAdapter<T>(this, inputQueueFactory.newInputQueue(), outputQueueFactory.newOutputQueue(), handler);
		} catch (QueueBuildingException e) {
			throw new IOException(e);
		}

		adapter.registering();

		acquireSelector();

		try {
			SelectionKey key = channel.register(getSelector(), SelectionKey.OP_READ,
					adapter);

			adapter.setKey(key);
			adapter.registered();

			return adapter;
		} finally {
			releaseSelector();
		}
	}

	@Override
	public void unregisterChannel(ChannelFacade key) {
		if (!(key instanceof ServerHandlerAdapter<?>)) {
			throw new IllegalArgumentException("Not a valid registration token");
		}

		@SuppressWarnings("unchecked")
		ServerHandlerAdapter<T> adapter = (ServerHandlerAdapter<T>) key;
		SelectionKey selectionKey = adapter.key();

		acquireSelector();

		try {
			adapter.unregistering();
			selectionKey.cancel();
		} finally {
			releaseSelector();
		}

		adapter.unregistered();
	}
	
	@Override
	protected void invokeHandler(ServerHandlerAdapter<T> adapter, SelectionKey key) {
		adapter.prepareToRun(key);
		adapter.key().interestOps(0);

		executor.execute(new HandlerFutureTask<T , ChannelFacade, ServerHandlerAdapter<T>>(adapter, this, key));
	}

}
