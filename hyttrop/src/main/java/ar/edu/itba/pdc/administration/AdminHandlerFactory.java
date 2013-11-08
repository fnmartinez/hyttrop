package ar.edu.itba.pdc.administration;

import ar.thorium.handler.EventHandlerFactory;

public class AdminHandlerFactory implements EventHandlerFactory<AdminHandler> {

    private AdminProtocol protocol;

    public AdminHandlerFactory() {
        this.protocol = new AdminProtocol();
    }

    @Override
    public AdminHandler newHandler() throws IllegalAccessException,
            InstantiationException {
        return new AdminHandler(protocol);
    }

}
