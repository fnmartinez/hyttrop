package ar.thorium.handler;

import ar.edu.itba.it.pdc.jabxy.network.utils.ProxyChannelFacade;

import java.nio.ByteBuffer;

public interface ProxyEventHandler extends EventHandler{

	ByteBuffer nextMessage(ProxyChannelFacade channelFacade);
	void handleConnection(ProxyChannelFacade facade);
	void handleInput(ByteBuffer message, ProxyChannelFacade channelFacade);

	void starting(ProxyChannelFacade channelFacade);
	void started(ProxyChannelFacade channelFacade);
	void stopping(ProxyChannelFacade channelFacade);
	void stopped(ProxyChannelFacade channelFacade);
}
