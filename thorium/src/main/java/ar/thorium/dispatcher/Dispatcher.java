package ar.thorium.dispatcher;

import ar.thorium.handler.EventHandler;
import ar.thorium.handler.HandlerAdapter;
import ar.thorium.utils.ChannelFacade;

import java.io.IOException;
import java.nio.channels.SelectableChannel;

public interface Dispatcher<H extends EventHandler, F extends ChannelFacade, A extends HandlerAdapter<H>> {

	void dispatch() throws IOException;

	void shutdown();

	F registerChannel(SelectableChannel channel,
                      H handler) throws IOException;

	void unregisterChannel(F key);

	void enqueueStatusChange(A adapter, Object handle);

	Thread start();
}
