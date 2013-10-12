package ar.thorium.handler;

import ar.thorium.dispatcher.Dispatcher;
import ar.thorium.queues.InputQueue;
import ar.thorium.queues.OutputQueue;
import ar.thorium.utils.ChannelFacade;

import java.nio.channels.SelectionKey;

public class ClientHandlerAdapter<T extends ClientEventHandler>
        extends AbstractHandlerAdapter<T, ChannelFacade, ClientHandlerAdapter<T>>
        implements ChannelFacade {


    public ClientHandlerAdapter(Dispatcher<T, ChannelFacade, ClientHandlerAdapter<T>> dispatcher, InputQueue inputQueue, OutputQueue outputQueue, T eventHandler) {
        super(dispatcher, inputQueue, outputQueue, eventHandler);
    }

    @Override
    public HandlerAdapter call() throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void prepareToRun(SelectionKey key) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void confirmSelection(Object handle) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void enableWriting() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void enableReading() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getInterestOps() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void modifyInterestOps(int opsToSet, int opsToReset) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
