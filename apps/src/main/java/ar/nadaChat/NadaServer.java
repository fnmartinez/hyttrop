package ar.nadaChat;

import ar.thorium.acceptor.implementations.BasicSocketAcceptor;
import ar.thorium.dispatcher.SelectorGuard;
import ar.thorium.dispatcher.implementation.NioDispatcher;
import ar.thorium.dispatcher.implementation.ReadWriteBlockingGuard;
import ar.thorium.handler.EventHandlerFactory;
import ar.thorium.queues.InputQueueFactory;
import ar.thorium.queues.OutputQueueFactory;
import ar.thorium.utils.SimpleBufferFactory;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NadaServer
{
    private NadaServer()
    {
        // cannot instantiate
    }

    public static void main (String [] args)
            throws IOException
    {
        Executor executor = Executors.newCachedThreadPool();
        SelectorGuard guard = new ReadWriteBlockingGuard();
        OutputQueueFactory outputQueueFactory = OutputQueueFactory.newInstance();
        EventHandlerFactory<NadaHandler> factory = new NadaProtocol();
        InputQueueFactory inputQueueFactory = InputQueueFactory.newInstance(new NadaValidator(), new SimpleBufferFactory(1024));
        NioDispatcher dispatcher = new NioDispatcher (executor, guard,  inputQueueFactory, outputQueueFactory); //executor, guard, input y output
        BasicSocketAcceptor acceptor = new BasicSocketAcceptor (1234, factory, dispatcher);

        acceptor.newThread();
    }
}

