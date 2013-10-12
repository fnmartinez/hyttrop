package ar.thorium.handler;

import ar.thorium.utils.ChannelFacade;

import java.nio.ByteBuffer;

public interface ServerEventHandler extends EventHandler{
	ByteBuffer nextMessage(ChannelFacade channelFacade);
	void handleInput(ByteBuffer message, ChannelFacade channelFacade);
}
