package ar.edu.itba.pdc.handler;

import ar.edu.itba.pdc.message.HttpHeader;
import ar.edu.itba.pdc.message.HttpRequestMessage;
import ar.edu.itba.pdc.message.HttpResponseMessage;
import ar.thorium.dispatcher.Dispatcher;
import ar.thorium.handler.EventHandler;
import ar.thorium.utils.ChannelFacade;
import ar.thorium.utils.Message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class HttpEventHandler implements EventHandler {

    private static final int DEFAULT_HTTP_PORT = 80;
    private final Dispatcher dispatcher;
    private HttpRequestMessage httpRequestMessage;
    private HttpResponseMessage httpResponseMessage;
    private ChannelFacade clientSideFacade;
    private ChannelFacade serverSideFacade;

    public HttpEventHandler(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void handleRead(ChannelFacade channelFacade, Message message) {
        if (httpRequestMessage == null) {
            httpRequestMessage = (HttpRequestMessage) message;

            SocketChannel channel;
            try {
                String host = httpRequestMessage.getHeader("Host").getValue();
                channel = SocketChannel.open(InetSocketAddress.createUnresolved(host, DEFAULT_HTTP_PORT));
                dispatcher.registerChannel(channel, new EventHandler() {
                    private boolean sendBody = false;
                    @Override
                    public void handleRead(ChannelFacade channelFacade, Message message) {
                        if (sendBody) {
                            int bytesToRead = 0;
                            try {
                                if ((bytesToRead = httpResponseMessage.getBody().available())>0) {
                                    byte[] bytes = new byte[bytesToRead];
                                    int bytesRead = httpResponseMessage.getBody().read(bytes);
                                    clientSideFacade.outputQueue().enqueue(ByteBuffer.wrap(bytes));
                                    if (httpResponseMessage.isFinilized()) {
                                        httpResponseMessage = null;
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                        } else {
                            httpResponseMessage = (HttpResponseMessage) message;
                            clientSideFacade = channelFacade;
                            clientSideFacade.outputQueue().enqueue(ByteBuffer.wrap(httpRequestMessage.getRequestLine().getBytes()));
                            for(HttpHeader header : httpRequestMessage.getHeaders()) {
                                clientSideFacade.outputQueue().enqueue(ByteBuffer.wrap(header.toString().getBytes()));
                            }
                            clientSideFacade.outputQueue().enqueue(ByteBuffer.wrap(new byte[]{'\n', '\r', '\n', '\r'}));
                        }
                    }

                    @Override
                    public void handleWrite(ChannelFacade channelFacade) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public void handleConnection(ChannelFacade channelFacade) {
                        serverSideFacade = channelFacade;
                        serverSideFacade.outputQueue().enqueue(ByteBuffer.wrap(httpRequestMessage.getRequestLine().getBytes()));
                        for(HttpHeader header : httpRequestMessage.getHeaders()) {
                            serverSideFacade.outputQueue().enqueue(ByteBuffer.wrap(header.toString().getBytes()));
                        }
                        serverSideFacade.outputQueue().enqueue(ByteBuffer.wrap(new byte[]{'\n', '\r', '\n', '\r'}));
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
        } else {
            int bytesToRead = 0;
            try {
                if ((bytesToRead = httpRequestMessage.getBody().available())>0) {
                    byte[] bytes = new byte[bytesToRead];
                    int bytesRead = httpRequestMessage.getBody().read(bytes);
                    serverSideFacade.outputQueue().enqueue(ByteBuffer.wrap(bytes));
                    if (httpRequestMessage.isFinilized()) {
                        httpRequestMessage = null;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handleWrite(ChannelFacade channelFacade) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handleConnection(ChannelFacade channelFacade) {
        clientSideFacade = channelFacade;
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
