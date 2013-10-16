package ar.thorium.handler;

import ar.thorium.dispatcher.Dispatcher;
import ar.thorium.queues.InputQueue;
import ar.thorium.queues.OutputQueue;
import ar.thorium.utils.ChannelFacade;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

public class ClientHandlerAdapter<T extends ClientEventHandler>
        extends AbstractHandlerAdapter<T, ChannelFacade, ClientHandlerAdapter<T>>
        implements ChannelFacade {

    protected int readyOps = 0;
    protected SelectionKey key;
    protected int interestOps = 0;
    protected SelectableChannel channel;

    public ClientHandlerAdapter(Dispatcher<T, ChannelFacade, ClientHandlerAdapter<T>> dispatcher, InputQueue inputQueue, OutputQueue outputQueue, T eventHandler) {
        super(dispatcher, inputQueue, outputQueue, eventHandler);
    }

    @Override
    public HandlerAdapter call() throws Exception {
        try {
            drainOutput();
            fillInput();

            ByteBuffer message;

            // must process all buffered messages because Selector will
            // not fire again for input that's already read and buffered
            if ((readyOps & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
                while ((message = eventHandler.nextMessage(this)) != null) {
                    eventHandler.handleInput(message, this);
                }
            }
        } finally {
            synchronized (stateChangeLock) {
                this.running = false;
            }
        }

        return this;
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

    public void setKey(SelectionKey key) {
        this.key = key;
        this.channel = key.channel();
        interestOps = key.interestOps();
    }

    public SelectionKey key() {
        return this.key;
    }

    private void drainOutput() throws IOException {
        if (((readyOps & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE)
                && (!outputQueue.isEmpty())) {
            outputQueue.drainTo((ByteChannel) channel);
        }

        // Write selection is turned on when output data in enqueued,
        // turn it off when the queue becomes empty.
        if (outputQueue.isEmpty()) {
            disableWriteSelection();

            if (shuttingDown) {
                channel.close();
                eventHandler.stopped(this);
            }
        }
    }

    private void disableWriteSelection() {
        modifyInterestOps(0, SelectionKey.OP_WRITE);
    }
}
