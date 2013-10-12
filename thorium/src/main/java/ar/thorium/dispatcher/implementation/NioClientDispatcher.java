package ar.thorium.dispatcher.implementation;

import ar.thorium.dispatcher.SelectorGuard;
import ar.thorium.handler.ClientEventHandler;
import ar.thorium.handler.ClientHandlerAdapter;
import ar.thorium.queues.InputQueueFactory;
import ar.thorium.queues.OutputQueueFactory;
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void unregisterChannel(ChannelFacade key) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void invokeHandler(ClientHandlerAdapter<T> adapter, SelectionKey key) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
