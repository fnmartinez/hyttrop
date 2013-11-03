package ar.edu.itba.pdc.handler;

import ar.thorium.dispatcher.Dispatcher;
import ar.thorium.handler.EventHandler;
import ar.thorium.utils.ChannelFacade;
import ar.thorium.utils.Message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.SocketHandler;

public class HttpEventHandler implements EventHandler {

    private final Dispatcher dispatcher;

    public HttpEventHandler(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void handleRead(ChannelFacade channelFacade, Message message) {
        SocketChannel channel;
        try {
            channel = SocketChannel.open(InetSocketAddress.createUnresolved("www.google.com.ar", 80));
            dispatcher.registerChannel(channel, new EventHandler() {
                @Override
                public void handleRead(ChannelFacade channelFacade, Message message) {
                    //To change body of implemented methods use File | Settings | File Templates.
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
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void stopping(ChannelFacade channelFacade) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }
            });
        } catch (IOException e) {
            throw new UnknownError();  //To change body of catch statement use File | Settings | File Templates.
        }
        //To change body of implemented methods use File | Settings | File Templates.
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
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void stopping(ChannelFacade channelFacade) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
