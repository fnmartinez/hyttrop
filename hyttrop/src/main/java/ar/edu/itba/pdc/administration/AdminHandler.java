package ar.edu.itba.pdc.administration;

import java.nio.ByteBuffer;

import ar.thorium.handler.EventHandler;
import ar.thorium.queues.InputQueue;
import ar.thorium.utils.ChannelFacade;
import ar.thorium.utils.Message;

public class AdminHandler implements EventHandler {

    private AdminProtocol protocol;

    public AdminHandler(AdminProtocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public void handleRead(ChannelFacade channelFacade, Message message) {
        ByteBuffer response = protocol.handleMessage((ByteBuffer)message);
        channelFacade.outputQueue().enqueue(response.array());
    }

    @Override
    public void handleWrite(ChannelFacade channelFacade) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handleConnection(ChannelFacade channelFacade) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void stopped(ChannelFacade channelFacade) {
        // TODO Auto-generated method stub

    }

    @Override
    public void stopping(ChannelFacade channelFacade) {
        // TODO Auto-generated method stub

    }
}
