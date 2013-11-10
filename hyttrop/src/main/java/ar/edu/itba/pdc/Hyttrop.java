package ar.edu.itba.pdc;

import ar.edu.itba.pdc.handler.HttpEventHandler;
import ar.edu.itba.pdc.utils.HttpEventHandlerFactory;
import ar.edu.itba.pdc.utils.HttpMessageValidator;
import ar.thorium.acceptor.implementations.BasicSocketAcceptor;
import ar.thorium.dispatcher.SelectorGuard;
import ar.thorium.dispatcher.implementation.NioDispatcher;
import ar.thorium.dispatcher.implementation.ReadWriteBlockingGuard;
import ar.thorium.handler.EventHandlerFactory;
import ar.thorium.queues.InputQueueFactory;
import ar.thorium.queues.OutputQueueFactory;
import ar.thorium.utils.SimpleBufferFactory;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Hyttrop {

    private static Logger logger = Logger.getLogger(HttpMessageValidator.class);


    public static void main(String[] args) throws IOException {
        Executor executor = Executors.newCachedThreadPool();
        SelectorGuard guard = new ReadWriteBlockingGuard();
        OutputQueueFactory outputQueueFactory = OutputQueueFactory.newInstance();
        EventHandlerFactory<HttpEventHandler> factory = new HttpEventHandlerFactory();
        InputQueueFactory inputQueueFactory = InputQueueFactory.newInstance(new HttpMessageValidator(), new SimpleBufferFactory(1024));
        NioDispatcher dispatcher = new NioDispatcher (executor, guard,  inputQueueFactory, outputQueueFactory);//executor, guard, input y output
        BasicSocketAcceptor acceptor = new BasicSocketAcceptor (8080, factory, dispatcher);
        ((HttpEventHandlerFactory)factory).setDispatcher(dispatcher);

        acceptor.newThread();
    }
}
