package ar.thorium.dispatcher.implementation;

import ar.thorium.dispatcher.Dispatcher;
import ar.thorium.dispatcher.SelectorGuard;
import ar.thorium.handler.EventHandler;
import ar.thorium.handler.HandlerAdapter;
import ar.thorium.queues.InputQueueFactory;
import ar.thorium.queues.OutputQueueFactory;
import ar.thorium.queues.exceptions.QueueBuildingException;
import ar.thorium.utils.ChannelFacade;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;

public class NioDispatcher implements Dispatcher, Runnable {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private final Selector selector;
    private final BlockingQueue<Pair<HandlerAdapter, Object>> statusChangeQueue;
    private final SelectorGuard guard;
    private volatile boolean dispatching = true;
    protected final Executor executor;
    protected final InputQueueFactory inputQueueFactory;
    protected final OutputQueueFactory outputQueueFactory;


    public NioDispatcher(Executor executor, SelectorGuard guard, InputQueueFactory inputQueueFactory, OutputQueueFactory outputQueueFactory) throws IOException {
        this.inputQueueFactory = inputQueueFactory;
        this.outputQueueFactory = outputQueueFactory;
        this.executor = executor;
        this.guard = guard;

        statusChangeQueue = new ArrayBlockingQueue<Pair<HandlerAdapter, Object>>(100);
        selector = Selector.open();
        this.guard.setSelector(this.selector);
    }

    @Override
    public void dispatch() throws IOException {
        while (dispatching) {
            guard.selectorBarrier();

            selector.select();

            checkStatusChangeQueue();

            Set<SelectionKey> keys = selector.selectedKeys();

            for (SelectionKey key : keys) {
                HandlerAdapter adapter = (HandlerAdapter) key.attachment();

                invokeHandler(adapter, key);
            }

            keys.clear();
        }    }


    @Override
    public void shutdown() {
        dispatching = false;

        selector.wakeup();
    }

    @Override
    public ChannelFacade registerChannel(SelectableChannel channel, EventHandler handler) throws IOException {
        channel.configureBlocking(false);

        HandlerAdapter adapter;
        try {
            adapter = new HandlerAdapter(this, inputQueueFactory.newInputQueue(), outputQueueFactory.newOutputQueue(), handler);
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
        if (!(key instanceof HandlerAdapter)) {
            throw new IllegalArgumentException("Not a valid registration token");
        }

        @SuppressWarnings("unchecked")
        HandlerAdapter adapter = (HandlerAdapter) key;
        SelectionKey selectionKey = adapter.key();

        acquireSelector();

        try {
            adapter.unregistering();
            selectionKey.cancel();
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
    public void enqueueStatusChange(HandlerAdapter adapter, Object handle) {
        boolean interrupted = false;
        Pair<HandlerAdapter, Object> pair = new Pair<HandlerAdapter, Object>(adapter, handle);
        try {
            while (true) {
                try {
                    statusChangeQueue.put(pair);
                    selector.wakeup();
                    return;
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted)
                Thread.currentThread().interrupt();
        }    }

    @Override
    public Thread start() {
        Thread thread = new Thread(this);

        thread.start();

        return thread;
    }

    private void invokeHandler(HandlerAdapter adapter, SelectionKey key) {
    }

    private void checkStatusChangeQueue() {
        Pair<HandlerAdapter, Object> pair;

        while ((pair = statusChangeQueue.poll()) != null) {
            HandlerAdapter adapter = pair.getObject();
            Object handle = pair.getHandle();

            if (adapter.isDead()) {
                unregisterChannel((ChannelFacade)adapter);
            } else {
                adapter.confirmSelection(handle);
            }
        }    }

    @Override
    public void run() {
        try {
            dispatch();
        } catch (IOException e) {
            logger.error("Unexpected I/O Exception", e);
        }

        Set<SelectionKey> keys = selector.selectedKeys();

        for (SelectionKey key : keys) {
            HandlerAdapter adapter = (HandlerAdapter) key.attachment();

            unregisterChannel((ChannelFacade)adapter);
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
    }
}
