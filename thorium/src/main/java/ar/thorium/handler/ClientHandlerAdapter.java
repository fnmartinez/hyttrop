package ar.thorium.handler;

import ar.thorium.dispatcher.Dispatcher;
import ar.thorium.queues.InputQueue;
import ar.thorium.queues.OutputQueue;
import ar.thorium.utils.ChannelFacade;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ClientHandlerAdapter<T extends ClientEventHandler>
        extends AbstractHandlerAdapter<T, ChannelFacade, ClientHandlerAdapter<T>>
        implements ChannelFacade {

    protected int readyOps = 0;
    protected SelectionKey key;
    protected int interestOps = 0;
    protected SelectableChannel channel;
    private boolean shuttingDown = false;

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

    private void enableReadSelection() {
        modifyInterestOps(SelectionKey.OP_READ, 0);
    }

    private void enableWriteSelection() {
        modifyInterestOps(SelectionKey.OP_WRITE, 0);
    }

    private void disableReadSelection() {
        modifyInterestOps(0, SelectionKey.OP_READ);
    }

    private void disableWriteSelection() {
        modifyInterestOps(0, SelectionKey.OP_WRITE);
    }

    @Override
    public void prepareToRun(SelectionKey key) {
        synchronized (stateChangeLock) {
            if (key.equals(this.key)) {
                interestOps = key.interestOps();
                readyOps = key.readyOps();
                running = true;
            } else {
                throw new IllegalArgumentException("This is not my key");
            }
        }
    }

    @Override
    public void confirmSelection(Object handle) {
        if (key != null && key.equals(this.key) && key.isValid()) {
            key.interestOps(interestOps);
        }
    }

    @Override
    public void enableWriting() {
        // TODO Auto-generated method stub
        enableWriteSelection();
        issueChange(key);
    }

    @Override
    public void enableReading() {
        enableReadSelection();
        issueChange(key);
    }

    @Override
    public int getInterestOps() {
        return interestOps;
    }

    @Override
    public void modifyInterestOps(int opsToSet, int opsToReset) {
        this.interestOps = modifyInterestOps(interestOps, opsToSet, opsToReset);
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

    private void fillInput() throws IOException {
        if (shuttingDown)
            return;

        int rc = inputQueue.fillFrom((ByteChannel) channel);

        if (rc == -1) {
            disableReadSelection();

            if (channel instanceof SocketChannel) {
                SocketChannel sc = (SocketChannel) channel;

                if (sc.socket().isConnected()) {
                    try {
                        sc.socket().shutdownInput();
                    } catch (SocketException e) {
                        // happens sometimes, ignore
                    }
                }
            }

            shuttingDown = true;
            eventHandler.stopping(this);

            // cause drainOutput to run, which will close
            // the socket if/when the output queue is empty
            enableWriteSelection();
        }
    }
}
