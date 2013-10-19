package ar.edu.itba.pdc.handler;

import ar.thorium.handler.EventHandler;
import ar.thorium.utils.ChannelFacade;

import java.nio.ByteBuffer;

public interface HttpEventHandler extends EventHandler {
    ByteBuffer nextMessage(ChannelFacade channelFacade);
    void handleInput(ByteBuffer message, ChannelFacade channelFacade);
    void handleConnection(ChannelFacade facade);
    void handleWrite(ChannelFacade channelFacade);
    void handleRead(ChannelFacade channelFacade, Message message);
}
