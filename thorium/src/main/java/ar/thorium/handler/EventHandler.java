package ar.thorium.handler;

import ar.thorium.utils.ChannelFacade;


public interface EventHandler {
	void starting(ChannelFacade channelFacade);
	void started(ChannelFacade channelFacade);
	void stopping(ChannelFacade channelFacade);
	void stopped(ChannelFacade channelFacade);
}
