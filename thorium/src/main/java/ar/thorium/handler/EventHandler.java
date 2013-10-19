package ar.thorium.handler;

import ar.thorium.utils.ChannelFacade;


public interface EventHandler {
	void handleInput(Message message, ChannelFacade channelFacade);
    void handleConnection(ChannelFacade channelFacade);
    void stopped(ChannelFacade channelFacade);
}
