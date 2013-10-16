package ar.thorium.dispatcher.implementation;

import ar.thorium.dispatcher.SelectorGuard;
import ar.thorium.handler.*;
import ar.thorium.handler.ClientHandlerAdapter;
import ar.thorium.queues.InputQueueFactory;
import ar.thorium.queues.OutputQueueFactory;
import ar.thorium.queues.exceptions.QueueBuildingException;
import ar.thorium.utils.ChannelFacade;
import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.concurrent.Executor;

public class NioClientDispatcher<T extends ClientEventHandler>
        extends AbstractNioDispatcher<T, ChannelFacade, ClientHandlerAdapter<T>>{

    public NioClientDispatcher(Executor executor, SelectorGuard guard, InputQueueFactory inputQueueFactory, OutputQueueFactory outputQueueFactory) throws IOException {
        super(executor, guard, inputQueueFactory, outputQueueFactory);
    }

    @Override
    public ChannelFacade registerChannel(SelectableChannel channel, T handler) throws IOException {
        channel.configureBlocking(false);

        ClientHandlerAdapter<T> adapter;
        try {
            adapter = new ClientHandlerAdapter<T>(this, inputQueueFactory.newInputQueue(), outputQueueFactory.newOutputQueue(), handler);
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
        if (!(key instanceof ClientHandlerAdapter<?>)) {
            throw new IllegalArgumentException("Not a valid registration token");
        }

        @SuppressWarnings("unchecked")
        ClientHandlerAdapter<T> adapter = (ClientHandlerAdapter<T>) key;
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
    protected void invokeHandler(ClientHandlerAdapter<T> adapter, SelectionKey key) {
        adapter.prepareToRun(key);
        adapter.key().interestOps(0);

        executor.execute(new HandlerFutureTask<T , ChannelFacade, ClientHandlerAdapter<T>>(adapter, this, key));
    }
}
