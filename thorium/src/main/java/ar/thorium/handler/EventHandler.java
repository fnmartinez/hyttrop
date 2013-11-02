package ar.thorium.handler;

import ar.thorium.utils.ChannelFacade;
import ar.thorium.utils.Message;


public interface EventHandler {
	void handleRead(ChannelFacade channelFacade, Message message);
    void handleWrite(ChannelFacade channelFacade);
    void handleConnection(ChannelFacade channelFacade);
    void stopped(ChannelFacade channelFacade);
    void stopping(ChannelFacade channelFacade);
}
