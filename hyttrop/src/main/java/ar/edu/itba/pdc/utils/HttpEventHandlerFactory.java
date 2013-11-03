package ar.edu.itba.pdc.utils;

import ar.edu.itba.pdc.handler.HttpEventHandler;
import ar.thorium.dispatcher.Dispatcher;
import ar.thorium.handler.EventHandlerFactory;

public class HttpEventHandlerFactory implements EventHandlerFactory<HttpEventHandler>{

    private Dispatcher dispatcher;

    public HttpEventHandlerFactory() {};

    public void setDispatcher(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public HttpEventHandler newHandler() throws IllegalAccessException, InstantiationException {
        return new HttpEventHandler(this.dispatcher);  //To change body of implemented methods use File | Settings | File Templates.
    }
}
