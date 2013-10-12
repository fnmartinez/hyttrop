package ar.thorium.handler;

public interface EventHandlerFactory<H extends EventHandler> {
	H newHandler() throws IllegalAccessException, InstantiationException;

}
