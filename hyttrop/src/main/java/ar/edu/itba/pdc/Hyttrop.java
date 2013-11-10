package ar.edu.itba.pdc;

import ar.edu.itba.pdc.administration.AdminHandlerFactory;
import ar.edu.itba.pdc.administration.AdminMessageValidator;
import ar.edu.itba.pdc.utils.ConfigurationHelper;
import ar.edu.itba.pdc.utils.HttpEventHandlerFactory;
import ar.edu.itba.pdc.utils.HttpMessageValidator;
import ar.thorium.acceptor.Acceptor;
import ar.thorium.acceptor.implementations.BasicSocketAcceptor;
import ar.thorium.dispatcher.Dispatcher;
import ar.thorium.dispatcher.SelectorGuard;
import ar.thorium.dispatcher.implementation.NioDispatcher;
import ar.thorium.dispatcher.implementation.ReadWriteBlockingGuard;
import ar.thorium.handler.EventHandler;
import ar.thorium.handler.EventHandlerFactory;
import ar.thorium.queues.InputQueueFactory;
import ar.thorium.queues.MessageValidator;
import ar.thorium.queues.OutputQueueFactory;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public enum Hyttrop {

    HYTTROP(ConfigurationHelper.getInstance().getProxyPort(), new HttpMessageValidator(), new HttpEventHandlerFactory()),
    ADMINISTRATION(ConfigurationHelper.getInstance().getAdministratorPort(), new AdminMessageValidator(), new AdminHandlerFactory());

    private final Dispatcher dispatcher;
    private final Acceptor acceptor;
    private final Executor executor;
    private final EventHandlerFactory<?> eventHandlerFactory;

    private <H extends EventHandler> Hyttrop(int port, MessageValidator validator, EventHandlerFactory<H> eventHandlerFactory) throws ExceptionInInitializerError {
        try{
            this.eventHandlerFactory = eventHandlerFactory;
            this.executor = Executors.newCachedThreadPool();
            SelectorGuard guard = new ReadWriteBlockingGuard();
            OutputQueueFactory outputQueueFactory = OutputQueueFactory.newInstance();
            InputQueueFactory inputQueueFactory = InputQueueFactory.newInstance(validator);
            this.dispatcher = new NioDispatcher(executor, guard, inputQueueFactory, outputQueueFactory);
            this.acceptor = new BasicSocketAcceptor(port, eventHandlerFactory, dispatcher);
        }catch (IOException e){
            Logger logger = Logger.getLogger(Hyttrop.class);
            logger.fatal("Servers could not be initialized.", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    public static void main(String[] args) throws IOException {
        ((HttpEventHandlerFactory)Hyttrop.HYTTROP.getEventHandlerFactory()).setDispatcher(Hyttrop.HYTTROP.getDispatcher());
        Hyttrop.HYTTROP.start();
        Hyttrop.ADMINISTRATION.start();
    }

    public void start() {
        this.acceptor.newThread();
    }

    public Dispatcher getDispatcher() {
        return this.dispatcher;
    }

    public Acceptor getAcceptor() {
        return this.acceptor;
    }

    public EventHandlerFactory<?> getEventHandlerFactory() {
        return this.eventHandlerFactory;
    }
}
