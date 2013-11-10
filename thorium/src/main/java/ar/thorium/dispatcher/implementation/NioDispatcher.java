package ar.thorium.dispatcher.implementation;

import ar.thorium.dispatcher.Dispatcher;
import ar.thorium.dispatcher.SelectorGuard;
import ar.thorium.handler.EventHandler;
import ar.thorium.handler.HandlerAdapter;
import ar.thorium.handler.HandlerFutureTask;
import ar.thorium.queues.InputQueueFactory;
import ar.thorium.queues.OutputQueueFactory;
import ar.thorium.queues.exceptions.QueueBuildingException;
import ar.thorium.utils.ChannelFacade;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;

public class NioDispatcher implements Dispatcher, Runnable {

    private final Selector selector;
    private final BlockingQueue<Pair<HandlerAdapter, SelectionKey>> statusChangeQueue;
    private final SelectorGuard guard;
    private final Executor executor;
    private final InputQueueFactory inputQueueFactory;
    private final OutputQueueFactory outputQueueFactory;
    private volatile boolean dispatching = true;
    private static Logger logger = Logger.getLogger(NioDispatcher.class);

    public NioDispatcher(Executor executor, SelectorGuard guard, InputQueueFactory inputQueueFactory, OutputQueueFactory outputQueueFactory) throws IOException {
        this.inputQueueFactory = inputQueueFactory;
        this.outputQueueFactory = outputQueueFactory;
        this.executor = executor;
        this.guard = guard;

        statusChangeQueue = new ArrayBlockingQueue<Pair<HandlerAdapter, SelectionKey>>(100);
        selector = Selector.open();
        this.guard.setSelector(this.selector);
    }

    @Override
    public void dispatch() throws IOException {
        logger.info("Beginning to dispatch");
        while (dispatching) {
            guard.selectorBarrier();

            selector.select();

            checkStatusChangeQueue();

            Set<SelectionKey> keys = selector.selectedKeys();
            if (logger.isTraceEnabled()) logger.trace("Selected " + keys.size() + " keys");
            for (SelectionKey key : keys) {
                HandlerAdapter adapter = (HandlerAdapter) key.attachment();
                if (logger.isDebugEnabled()) logger.debug("Dispatching connection " + adapter);
                invokeHandler(adapter, key);
                if (logger.isDebugEnabled()) logger.debug("Connection dispatched." + adapter);
            }

            keys.clear();
        }
    }

    @Override
    public void shutdown() {
        logger.info("Dispatcher " + this.toString() + " shutting down");
        dispatching = false;
        selector.wakeup();
    }

    @Override
    public ChannelFacade registerChannel(SelectableChannel channel, EventHandler handler) throws IOException {
        if (logger.isTraceEnabled()) logger.trace("NioDispatcher::registerChannel; channel=" + channel + " handler=" + handler);
        channel.configureBlocking(false);

        HandlerAdapter adapter;
        try {
            adapter = new HandlerAdapter(this, inputQueueFactory.newInputQueue(), outputQueueFactory.newOutputQueue(), handler);
        } catch (QueueBuildingException e) {
            logger.error("Adapter could not be created.", e);
            throw new IOException(e);
        }
        acquireSelector();

        try {
            SelectionKey key = channel.register(getSelector(), SelectionKey.OP_READ | SelectionKey.OP_WRITE,
                    adapter);
            adapter.setKey(key);
            return adapter;
        }catch(Exception e){
            logger.error("Channel could not be registered.", e);
            e.printStackTrace();
            return null;
        } finally {
            releaseSelector();
        }
    }

    @Override
    public void unregisterChannel(ChannelFacade key) {
        if (!(key instanceof HandlerAdapter)) {
            logger.error("The specified key is incorrect.");
            throw new IllegalArgumentException("Not a valid registration token.");
        }

        @SuppressWarnings("unchecked")
        HandlerAdapter adapter = (HandlerAdapter) key;
        SelectionKey selectionKey = adapter.key();

        acquireSelector();

        try {
            adapter.unregistering();
            selectionKey.cancel();
            logger.info("Channel unregistered.");
        } finally {
            releaseSelector();
        }

        adapter.unregistered();    }

    private void releaseSelector() {
        guard.releaseSelector();
    }

    private void acquireSelector() {
        guard.acquireSelector();
    }

    @Override
    public void enqueueStatusChange(HandlerAdapter adapter, SelectionKey handle) {
        boolean interrupted = false;
        Pair<HandlerAdapter, SelectionKey> pair = new Pair<HandlerAdapter, SelectionKey>(adapter, handle);
        try {
            while (true) {
                try {
                    if (statusChangeQueue.contains(pair)) {

                    }
                    if (logger.isDebugEnabled()) logger.debug("Enqueing status change for adapter " + adapter);
                    statusChangeQueue.put(pair);
                    selector.wakeup();
                    return;
                } catch (InterruptedException e) {
                    interrupted = true;
                    logger.fatal("Status change operation throwed an error.", e);
                }
            }
        } finally {
            if (interrupted)
                Thread.currentThread().interrupt();
        }
    }

    @Override
    public Thread start() {
        Thread thread = new Thread(this);

        thread.start();
        logger.info("Dispatcher started.");
        return thread;
    }

    private void invokeHandler(HandlerAdapter adapter, SelectionKey key) {
        try {
        adapter.prepareToRun(key);
        adapter.key().interestOps(0);
        } catch (CancelledKeyException cke) {
            logger.error("Key has been cancelled", cke);
            adapter.die();
            return;
        }
        executor.execute(new HandlerFutureTask(adapter, this, key));
    }

    private void checkStatusChangeQueue() {
        Pair<HandlerAdapter, SelectionKey> pair;

        while ((pair = statusChangeQueue.poll()) != null) {
            HandlerAdapter adapter = pair.getObject();
            SelectionKey handle = pair.getHandle();

            if (adapter.isDead()) {
                unregisterChannel(adapter);
            } else {
                adapter.confirmSelection(handle);
            }
        }
    }

    @Override
    public void run() {
        try {
            dispatch();
        } catch (IOException e) {
            logger.fatal("Unexpected I/O Exception", e);
        }

        logger.info("Clossing dispatcher");
        Set<SelectionKey> keys = selector.selectedKeys();

        logger.info("Unregistering channels");
        for (SelectionKey key : keys) {
            HandlerAdapter adapter = (HandlerAdapter) key.attachment();

            unregisterChannel(adapter);
        }

        try {
            selector.close();
        } catch (IOException e) {
            logger.error("Unexpected I/O Exception closing selector", e);
        }
    }

    public Selector getSelector() {
        return selector;
    }

    private class Pair<T, S> {
        private final T object;
        private final S handle;

        public Pair(T object, S handle) {
            this.object = object;
            this.handle = handle;
        }

        public T getObject() {
            return this.object;
        }

        public S getHandle() {
            return this.handle;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pair)) return false;

            Pair pair = (Pair) o;

            if (handle != null ? !handle.equals(pair.handle) : pair.handle != null) return false;
            if (object != null ? !object.equals(pair.object) : pair.object != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = object != null ? object.hashCode() : 0;
            result = 31 * result + (handle != null ? handle.hashCode() : 0);
            return result;
        }
    }
}
