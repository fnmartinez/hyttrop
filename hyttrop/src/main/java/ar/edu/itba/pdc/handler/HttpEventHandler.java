package ar.edu.itba.pdc.handler;

import ar.edu.itba.pdc.message.HttpHeader;
import ar.edu.itba.pdc.message.HttpRequestMessage;
import ar.edu.itba.pdc.message.HttpResponseMessage;
import ar.thorium.dispatcher.Dispatcher;
import ar.thorium.handler.EventHandler;
import ar.thorium.utils.ChannelFacade;
import ar.thorium.utils.Message;
import org.apache.log4j.Logger;

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
    private static Logger logger = Logger.getLogger(HttpEventHandler.class);

    public HttpEventHandler(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void handleRead(ChannelFacade channelFacade, Message message) {
        logger.debug("Handling read from client.");
        if (httpRequestMessage == null) {
            logger.debug("A message arrived from the client.");
            httpRequestMessage = (HttpRequestMessage) message;

            SocketChannel channel;
            try {
                String host = httpRequestMessage.getHeader("Host").getValue();
                channel = SocketChannel.open(new InetSocketAddress(host, DEFAULT_HTTP_PORT));
                logger.debug("Connection with the server was established successfully.");
                dispatcher.registerChannel(channel, new EventHandler() {
                    private boolean sendBody = false;
                    private boolean headersSent = false;
                    @Override
                    public void handleRead(ChannelFacade channelFacade, Message message) {
                        logger.debug("Handling read operation from server.");
                        httpResponseMessage = (HttpResponseMessage) message;
                        if (!headersSent) {
                            logger.debug("Receiving HTTP headers.");
                            clientSideFacade.outputQueue().enqueue(ByteBuffer.wrap((httpResponseMessage.getStatusLine() + CRLF).getBytes()));
                            logger.debug("Writing headers back to client.");
                            for(HttpHeader header : httpResponseMessage.getHeaders()) {
                                clientSideFacade.outputQueue().enqueue(ByteBuffer.wrap((header.toString() + CRLF).getBytes()));
                            }
                            clientSideFacade.outputQueue().enqueue(ByteBuffer.wrap(CRLF.getBytes()));
                            headersSent = true;
                        }
                        int bytesToRead = 0;
                        try {
                            while ((bytesToRead = httpResponseMessage.getBody().available()) > 0) {
                                logger.debug("Sending body.");
                                byte[] bytes = new byte[bytesToRead];
                                int bytesRead = httpResponseMessage.getBody().read(bytes);
                                logger.debug("Read " + bytesRead + " from server. Sending to client.");
                                clientSideFacade.outputQueue().enqueue(ByteBuffer.wrap(bytes));
                            }
                        } catch (IOException e) {
                            logger.error("An error occurred.", e);
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }

                    @Override
                    public void handleWrite(ChannelFacade channelFacade) {
                        logger.debug("Handling write operation to server.");
                        //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public void handleConnection(ChannelFacade channelFacade) {
                        logger.debug("Handling connection from server.");
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
                logger.error("An unknown error occurred.", e);
                throw new UnknownError();  //To change body of catch statement use File | Settings | File Templates.
            }
        } else {
            int bytesToRead = 0;
            try {
                logger.debug("Receiving message from client.");
                if ((bytesToRead = httpRequestMessage.getBody().available())>0) {
                    byte[] bytes = new byte[bytesToRead];
                    int bytesRead = httpRequestMessage.getBody().read(bytes);
                    serverSideFacade.outputQueue().enqueue(ByteBuffer.wrap(bytes));
                    if (httpRequestMessage.isFinilized()) {
                        httpRequestMessage = null;
                    }
                }
            } catch (IOException e) {
                logger.error("An error occurred while receiving message from client.", e);
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handleWrite(ChannelFacade channelFacade) {
        logger.debug("Handling write to client.");
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handleConnection(ChannelFacade channelFacade) {
        logger.debug("Handling connection from client.");
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
