package ar.thorium.handler;

import ar.edu.itba.it.pdc.jabxy.network.utils.ChannelFacade;


public interface EventHandler {
	void starting(ChannelFacade channelFacade);
	void started(ChannelFacade channelFacade);
	void stopping(ChannelFacade channelFacade);
	void stopped(ChannelFacade channelFacade);
}
