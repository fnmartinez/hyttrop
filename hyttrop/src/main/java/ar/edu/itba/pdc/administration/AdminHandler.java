package ar.edu.itba.pdc.administration;

import java.nio.ByteBuffer;

import ar.thorium.handler.EventHandler;
import ar.thorium.queues.InputQueue;
import ar.thorium.utils.ChannelFacade;
import ar.thorium.utils.Message;

public class AdminHandler implements EventHandler {

    private AdminProtocol protocol;
    private boolean firstConnection;

    public AdminHandler(AdminProtocol protocol) {
        this.protocol = protocol;
        this.firstConnection = true;
    }

    @Override
    public void handleRead(ChannelFacade channelFacade, Message message) {
        ByteBuffer response = protocol.handleMessage((AdminMessage)message);
        channelFacade.outputQueue().enqueue(response.array());
    }

    @Override
    public void handleWrite(ChannelFacade channelFacade) {
        if (firstConnection) {
            ByteBuffer bf = ByteBuffer.allocate(1024);
            bf.put("Bienvenido al sistema de administracion de Hyttrop. Ingrese un comando para comenzar...\n".getBytes());
            channelFacade.outputQueue().enqueue(bf.array());
            firstConnection = false;
        }
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
