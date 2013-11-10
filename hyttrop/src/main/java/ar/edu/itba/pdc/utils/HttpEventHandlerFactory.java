package ar.edu.itba.pdc.utils;

import ar.edu.itba.pdc.handler.HttpEventHandler;
import ar.thorium.dispatcher.Dispatcher;
import ar.thorium.handler.EventHandlerFactory;
import org.apache.log4j.Logger;

public class HttpEventHandlerFactory implements EventHandlerFactory<HttpEventHandler>{

    private static Logger logger = Logger.getLogger(HttpEventHandlerFactory.class);
    private Dispatcher dispatcher;

    public HttpEventHandlerFactory() {};

    public void setDispatcher(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public HttpEventHandler newHandler() throws IllegalAccessException, InstantiationException {
        if (logger.isTraceEnabled()) logger.trace("Creating new handler");
        return new HttpEventHandler(this.dispatcher);
    }
}
