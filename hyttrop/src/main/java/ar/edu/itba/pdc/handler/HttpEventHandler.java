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
    private static final String CRLF = "\r\n";
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
        System.out.println("Handling Read from Client");
        if (httpRequestMessage == null) {
            System.out.println("We have a message from the client!");
            httpRequestMessage = (HttpRequestMessage) message;

            SocketChannel channel;
            try {
                String host = httpRequestMessage.getHeader("Host").getValue();
                channel = SocketChannel.open(new InetSocketAddress(host, DEFAULT_HTTP_PORT));
                System.out.println("We have a connection to the server!");
                dispatcher.registerChannel(channel, new EventHandler() {
                    private boolean sendBody = false;
                    private boolean headersSent = false;
                    @Override
                    public void handleRead(ChannelFacade channelFacade, Message message) {
                        System.out.println("Handling Read from Server");
                        httpResponseMessage = (HttpResponseMessage) message;
                        if (!headersSent) {
                            System.out.println("Receiving Headers!");
                            clientSideFacade.outputQueue().enqueue(ByteBuffer.wrap((httpResponseMessage.getStatusLine() + CRLF).getBytes()));
                            System.out.println("Writing Headers Back to Client!");
                            for(HttpHeader header : httpResponseMessage.getHeaders()) {
                                clientSideFacade.outputQueue().enqueue(ByteBuffer.wrap((header.toString() + CRLF).getBytes()));
                            }
                            clientSideFacade.outputQueue().enqueue(ByteBuffer.wrap(CRLF.getBytes()));
                            headersSent = true;
                        }
                        int bytesToRead = 0;
                        try {
                            while ((bytesToRead = httpResponseMessage.getBody().available()) > 0) {
                                System.out.println("Sending body!");
                                byte[] bytes = new byte[bytesToRead];
                                int bytesRead = httpResponseMessage.getBody().read(bytes);
                                System.out.println("Read " + bytesRead + " from server. Sending to client!");
                                clientSideFacade.outputQueue().enqueue(ByteBuffer.wrap(bytes));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }

                    @Override
                    public void handleWrite(ChannelFacade channelFacade) {
                        System.out.println("Handling Write to Server");
                        //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public void handleConnection(ChannelFacade channelFacade) {
                        System.out.println("Handling Connection from Server");
                        serverSideFacade = channelFacade;
                        serverSideFacade.outputQueue().enqueue(ByteBuffer.wrap((httpRequestMessage.getRequestLine() + CRLF).getBytes()));
                        for(HttpHeader header : httpRequestMessage.getHeaders()) {
                            serverSideFacade.outputQueue().enqueue(ByteBuffer.wrap((header.toString() + CRLF).getBytes()));
                        }
                        serverSideFacade.outputQueue().enqueue(ByteBuffer.wrap(CRLF.getBytes()));
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
        System.out.println("Handling Write to Client");
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handleConnection(ChannelFacade channelFacade) {
        System.out.println("Handling Connection from Client");
        clientSideFacade = channelFacade;
    }

    @Override
    public void stopped(ChannelFacade channelFacade) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void stopping(ChannelFacade channelFacade) {
		// To change body of implemented methods use File | Settings | File
		// Templates.
	}
}
