package ar.thorium.handler;

import ar.thorium.dispatcher.Dispatcher;
import ar.thorium.queues.InputQueue;
import ar.thorium.queues.OutputQueue;
import ar.thorium.utils.ChannelFacade;
import ar.thorium.utils.Message;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;

public class HandlerAdapter implements Callable<HandlerAdapter>, ChannelFacade {

    private final Dispatcher dispatcher;
    private final InputQueue inputQueue;
    private final OutputQueue outputQueue;
    private final Object stateChangeLock = new Object();
    private SelectableChannel channel;
    private EventHandler eventHandler;
    private SelectionKey key;
    private volatile boolean running = false;
    private volatile boolean dead = false;
    private boolean shuttingDown = false;
    private int interestOps = 0;
    private int readyOps = 0;
    private boolean connectionHandled;
    private static Logger logger = Logger.getLogger(HandlerAdapter.class);

    public HandlerAdapter(Dispatcher dispatcher, InputQueue inputQueue,
                          OutputQueue outputQueue, EventHandler eventHandler) {
        this.dispatcher = dispatcher;
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        this.outputQueue().setChannelFacade(this);
        this.eventHandler = eventHandler;
        this.connectionHandled = false;
    }

    @Override
    public HandlerAdapter call() throws IOException {
        try {
            if (!connectionHandled || (readyOps & SelectionKey.OP_CONNECT) == SelectionKey.OP_CONNECT) {
                eventHandler.handleConnection(this);
                connectionHandled = true;
                this.enableWriteSelection();
                logger.debug("Handler caller to connect.");
            } else {
                if((readyOps & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE){ //si voy a escribir en el canal
                    eventHandler.handleWrite(this);
                    drainOutput();
                    logger.debug("Handler caller to write.");
                }
                if((readyOps & SelectionKey.OP_READ) == SelectionKey.OP_READ){ //si voy a leer
                    fillInput();
                    Message message;
                    if(!inputQueue.isEmpty() && (message = inputQueue.getMessage()) != null){
                        eventHandler.handleRead(this, message);
                        logger.debug("Handler caller to read.");
                    }
                }
            }
            this.enableReadSelection();
        } catch (Exception e) {
            logger.error("An error occurred while calling this handler.", e);
        } finally {
            synchronized (stateChangeLock) {
                this.running = false;
            }
        }

        return this;
    }

    // --------------------------------------------------
    // Private helper methods

    // These three methods manipulate the private copy of the selection
    // interest flags. Upon completion, this local copy will be copied
    // back to the SelectionKey as the new interest set.
    private void enableWriteSelection() {
        modifyInterestOps(SelectionKey.OP_WRITE, 0);
    }

    private void disableWriteSelection() {
        modifyInterestOps(0, SelectionKey.OP_WRITE);
    }

    private void disableReadSelection() {
        modifyInterestOps(0, SelectionKey.OP_READ);
    }

    private void enableReadSelection() {
        modifyInterestOps(SelectionKey.OP_READ, 0);
    }

    // If there is output queued, and the channel is ready to
    // accept data, send as much as it will take.
    private void drainOutput() throws IOException {
    	
        if (((readyOps & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE)
                && (!outputQueue.isEmpty())) {
            logger.debug("Queue drained.");
            outputQueue.drainTo((ByteChannel) channel);
        }

        // Write selection is turned on when output data in enqueued,
        // turn it off when the queue becomes empty.
        if (outputQueue.isEmpty()) {
            logger.debug("Queue is empty.");
            disableWriteSelection();

            if (shuttingDown || outputQueue.isClosed()) {
                logger.debug("Channel was closed.");
                channel.close();
                eventHandler.stopped(this);
            }
        }
    }

    // Attempt to fill the input queue with as much data as the channel
    // can provide right now. If end-of-stream is reached, stop read
    // selection and shutdown the input side of the channel.
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

            logger.debug("End of stream reached.");

            shuttingDown = true;
            eventHandler.stopping(this);

            // cause drainOutput to run, which will close
            // the socket if/when the output queue is empty
            enableWriteSelection();
        }
    }

    public void setKey(SelectionKey key) {
        this.key = key;
        this.channel = key.channel();
        interestOps = key.interestOps();
    }

    public SelectionKey key() {
        return this.key;
    }

    public void prepareToRun(SelectionKey key) {
        synchronized (stateChangeLock) {
            if (key.equals(this.key)) {
                interestOps = key.interestOps();
                readyOps = key.readyOps();
                running = true;
                logger.debug("Handler prepared to run.");
            } else {
                logger.error("Incorrect key received.");
                throw new IllegalArgumentException("This is not my key");
            }
        }
    }

    public int getInterestOps() {
        return interestOps;
    }

    public void modifyInterestOps(int opsToSet, int opsToReset) {
        this.interestOps = modifyInterestOps(interestOps, opsToSet, opsToReset);
    }

    public int getReadyOps() {
        return readyOps;
    }

    public void confirmSelection(SelectionKey key) {
        if (key != null && key.equals(this.key) && key.isValid()) {
            key.interestOps(interestOps);
        }
    }



    public void enableWriting() {
        enableWriteSelection();
        issueChange(key);
    }

    public void enableReading() {
        enableReadSelection();
        issueChange(key);
    }

    public InputQueue inputQueue() {
        return this.inputQueue;
    }

    public OutputQueue outputQueue() {
        return this.outputQueue;
    }

    public boolean isDead() {
        return dead;
    }

    public void unregistering() {
        eventHandler.stopping(this);
    }

    public void unregistered() {
        eventHandler.stopped(this);
    }

    public void die() {
        logger.debug("Handler died.");
        this.dead = true;
    }

    protected int modifyInterestOps(int ops, int opsToSet, int opsToReset) {
        ops = (ops | opsToSet) & (~opsToReset);
        logger.debug("InterestOpts modified.");
        return ops;
    }

    @SuppressWarnings("unchecked")
    protected void issueChange(SelectionKey key) {
        synchronized (stateChangeLock) {
            if (!running) {
                dispatcher.enqueueStatusChange((HandlerAdapter) this, key);
            }
        }
    }

    public boolean connectionHandled() {
        return connectionHandled;
    }
}