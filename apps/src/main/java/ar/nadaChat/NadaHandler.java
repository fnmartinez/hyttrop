package ar.nadaChat;

import ar.thorium.handler.EventHandler;
import ar.thorium.utils.ChannelFacade;
import ar.thorium.utils.Message;

import java.nio.ByteBuffer;

public class NadaHandler implements EventHandler {

    private final NadaProtocol protocol;
    private boolean firstConnection;

    public NadaHandler (NadaProtocol protocol)
    {
        this.protocol = protocol;
        this.firstConnection = true;
    }

    @Override
    public void handleRead(ChannelFacade channelFacade, Message message) {
        protocol.handleMessage(channelFacade, (NadaMessage)message);
    }

    @Override
    public void handleWrite(ChannelFacade channelFacade) {
        if (firstConnection) {
            ByteBuffer bf = ByteBuffer.allocate(1024);
            bf.put("Hola ".getBytes());
            channelFacade.outputQueue().enqueue(bf);
            firstConnection = false;
        }
    }

    @Override
    public void handleConnection(ChannelFacade channelFacade) {
        protocol.newUser(channelFacade);
    }

    @Override
    public void stopped(ChannelFacade channelFacade) {
        protocol.endUser (channelFacade);
    }

    @Override
    public void stopping(ChannelFacade channelFacade) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}
