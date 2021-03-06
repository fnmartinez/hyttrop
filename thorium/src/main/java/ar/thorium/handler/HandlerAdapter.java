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
        if (logger.isDebugEnabled()) logger.debug(this.toString() + " being invoked");
        try {
            if (!connectionHandled || (readyOps & SelectionKey.OP_CONNECT) == SelectionKey.OP_CONNECT) {
                eventHandler.handleConnection(this);
                connectionHandled = true;
                this.enableWriteSelection();
                if (logger.isDebugEnabled()) logger.debug(this.toString() + " handled connection.");
            } else {
                if((readyOps & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE){ //si voy a escribir en el canal
                    eventHandler.handleWrite(this);
                    drainOutput();
                    if (logger.isDebugEnabled()) logger.debug(this.toString() + " handled write operation.");
                }
                if((readyOps & SelectionKey.OP_READ) == SelectionKey.OP_READ){ //si voy a leer
                    fillInput();
                    Message message;
                    if(!inputQueue.isEmpty() && (message = inputQueue.getMessage()) != null){
                        eventHandler.handleRead(this, message);
                        if (logger.isDebugEnabled()) logger.debug(this.toString() + " handled read operation.");
                    }
                }
            }
            this.enableReadSelection();
        } catch (Exception e) {
            logger.error("An error occurred while calling this handler.", e);
            this.die();
        } finally {
            synchronized (stateChangeLock) {
                this.running = false;
            }
        }

        return this;
    }

    // --------------------------------------------------
    // Private helper methods

    // These four methods manipulate the private copy of the selection
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
    private synchronized void drainOutput() throws IOException {
        if (((readyOps & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE)
                && (!outputQueue.isEmpty())) {
            if (logger.isTraceEnabled()) logger.trace("Queue drained.");
            outputQueue.drainTo((ByteChannel) channel);
        }

        // Write selection is turned on when output data in enqueued,
        // turn it off when the queue becomes empty.
        if (outputQueue.isEmpty()) {
            if (logger.isTraceEnabled()) logger.trace("Queue is empty.");
            disableWriteSelection();

            if (shuttingDown || outputQueue.isClosed()) {
                if (logger.isInfoEnabled()) logger.info("Channel was closed.");
                channel.close();
                eventHandler.stopped(this);
            }
        }
    }

    // Attempt to fill the input queue with as much data as the channel
    // can provide right now. If end-of-stream is reached, stop read
    // selection and stop the input side of the channel.
    private synchronized void fillInput() throws IOException {
        if (shuttingDown)
            return;

        inputQueue.fillFrom((ByteChannel) channel);
        if (inputQueue.isClosed()) {
            disableReadSelection();

            if (channel instanceof SocketChannel) {
                SocketChannel sc = (SocketChannel) channel;

                if (sc.socket().isConnected()) {
                    try {
                        if (outputQueue.isEmpty() && outputQueue.isClosed()) {
                            if (logger.isInfoEnabled()) logger.info("Closing connection: "+ sc.socket().getRemoteSocketAddress());
                            sc.socket().close();
                        } else {
                            sc.socket().shutdownInput();
                        }
                    } catch (SocketException e) {
                        // happens sometimes, ignore
                    }
                }
            }

            if (logger.isDebugEnabled()) logger.debug("End of stream reached.");

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
                if (logger.isDebugEnabled()) logger.debug("Handler ready to run.");
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
        if (logger.isTraceEnabled()) logger.trace(this.toString() + " enabling writing");
        enableWriteSelection();
        issueChange(key);
    }

    public void enableReading() {
        if (logger.isTraceEnabled()) logger.trace(this.toString() + " enabling reading");
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
        try {
            eventHandler.stopping(this);
        } catch (Exception e) {
            logger.error("Sending stopping signal to event handler failed", e);
        }
    }

    public void unregistered() {
        try {
            eventHandler.stopped(this);
        } catch (Exception e) {
            logger.error("Sending stopped signal to event handler failed", e);
        }
        try {
            if (channel.isOpen()) {
                channel.close();
            }
        } catch (IOException e) {
            logger.error("Exception thrown while closing channel after unregistering adapter", e);
        }
    }

    public void die() {
        logger.info(this.toString() + " died.");
        this.inputQueue.close();
        this.outputQueue.close();
        this.dead = true;
    }

    protected int modifyInterestOps(int ops, int opsToSet, int opsToReset) {
        if (logger.isTraceEnabled()) logger.trace("Modifying interests ops: CurrentOps=" + ops + " OpsToSet=" + opsToSet + " OpsToReset=" + opsToReset);
        ops = (ops | opsToSet) & (~opsToReset);
        return ops;
    }

    @SuppressWarnings("unchecked")
    protected void issueChange(SelectionKey key) {
        synchronized (stateChangeLock) {
            if (!running) {
                dispatcher.enqueueStatusChange(this, key);
            }
        }
    }

    @Override
    public String toString() {
        return "HandlerAdapter{" +
                "channel=" + channel +
                '}';
    }
}