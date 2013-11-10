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
        if (logger.isDebugEnabled()) logger.debug("Handling read from client; ChannelFacade: " + channelFacade + " Message: " + message);
        if (httpRequestMessage == null) {
            if (logger.isTraceEnabled()) logger.trace("A message arrived from the client.");
            httpRequestMessage = (HttpRequestMessage) message;

            SocketChannel channel;
            try {
                String host = httpRequestMessage.getHeader("Host").getValue();
//                httpRequestMessage.removeHeader("Accept-Encoding");
                channel = SocketChannel.open(new InetSocketAddress(host, DEFAULT_HTTP_PORT));
                if (logger.isDebugEnabled()) logger.debug("Connection with the origin server " + channel.getRemoteAddress() + "was established successfully.");
                dispatcher.registerChannel(channel, new EventHandler() {
                    private boolean headersSent = false;
                    @Override
                    public void handleRead(ChannelFacade channelFacade, Message message) {
                        if (logger.isDebugEnabled()) logger.debug("Handling read from server; ChannelFacade: " + channelFacade + " Message: " + message);
						httpResponseMessage = (HttpResponseMessage) message;
                        if(httpResponseMessage.readyToSend()){
	                        if (!headersSent) {
                                if (logger.isDebugEnabled()) logger.debug("Receiving HTTP headers from Server.");
                                if (logger.isTraceEnabled()) logger.trace(httpResponseMessage.getStatusLine());
	                            clientSideFacade.outputQueue().enqueue((httpResponseMessage.getStatusLine() + CRLF).getBytes());
	                            for(HttpHeader header : httpResponseMessage.getHeaders()) {
                                    if (logger.isTraceEnabled()) logger.trace(header.toString());
	                                clientSideFacade.outputQueue().enqueue((header.toString() + CRLF).getBytes());
	                            }
	                            clientSideFacade.outputQueue().enqueue(CRLF.getBytes());
	                            headersSent = true;
	                        }
	                        int bytesToRead = 0;
	                        while ((bytesToRead = httpResponseMessage.getBody().available()) > 0) {;
							    byte[] bytes = new byte[bytesToRead];
							    int bytesRead = httpResponseMessage.getBody().read(bytes);
							    if (logger.isDebugEnabled()) logger.debug("Read " + bytesRead + " from server. Sending to client.");
                                if (logger.isTraceEnabled()) logger.trace(new String(bytes));
							    clientSideFacade.outputQueue().enqueue(bytes);
							}
	                    }else{
	                    	logger.debug("Not Ready to Send");
	                    }
                    }

                    @Override
                    public void handleWrite(ChannelFacade channelFacade) {
                        if (logger.isDebugEnabled()) logger.debug("Handling write from server; ChannelFacade: " + channelFacade);
                        //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public void handleConnection(ChannelFacade channelFacade) {
                        if (logger.isDebugEnabled()) logger.debug("Handling connection from server; ChannelFacade: " + channelFacade);
                        serverSideFacade = channelFacade;

                        if (logger.isTraceEnabled()) logger.trace("Sending HTTP Request to Server; Request Line=\"" + httpRequestMessage.getRequestLine() + "\"");
                        serverSideFacade.outputQueue().enqueue((httpRequestMessage.getRequestLine() + CRLF).getBytes());
                        for(HttpHeader header : httpRequestMessage.getHeaders()) {
                            if (logger.isTraceEnabled()) logger.trace(header.toString());
                            serverSideFacade.outputQueue().enqueue((header.toString() + CRLF).getBytes());
                        }
                        serverSideFacade.outputQueue().enqueue(CRLF.getBytes());
                    }

                    @Override
                    public void stopped(ChannelFacade channelFacade) {
                        if (logger.isDebugEnabled()) logger.debug("Server handler stopped; ChannelFacade: " + channelFacade);
                        //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public void stopping(ChannelFacade channelFacade) {
                        if (logger.isDebugEnabled()) logger.debug("Server handler stopping; ChannelFacade: " + channelFacade);
                        //To change body of implemented methods use File | Settings | File Templates.
                    }
                });
            } catch (IOException e) {
                logger.error(this.toString(), e);
                throw new UnknownError();  //To change body of catch statement use File | Settings | File Templates.
            }
        } else {
            int bytesToRead = 0;
            if (logger.isTraceEnabled()) logger.trace("Receiving body from client.");
			if ((bytesToRead = httpRequestMessage.getBody().available())>0) {
			    byte[] bytes = new byte[bytesToRead];
			    int bytesRead = httpRequestMessage.getBody().read(bytes);
                if (logger.isDebugEnabled()) logger.debug(bytesRead + " bytes in client boyd");
			    serverSideFacade.outputQueue().enqueue(bytes);
			    if (httpRequestMessage.isFinilized()) {
			        httpRequestMessage = null;
			    }
			}
        }
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handleWrite(ChannelFacade channelFacade) {
        if (logger.isDebugEnabled()) logger.debug("Handling write from client; ChannelFacade: " + channelFacade);
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handleConnection(ChannelFacade channelFacade) {
        if (logger.isDebugEnabled()) logger.debug("Handling connection from client; ChannelFacade: " + channelFacade);
        clientSideFacade = channelFacade;
    }

    @Override
    public void stopped(ChannelFacade channelFacade) {
        if (logger.isDebugEnabled()) logger.debug("Client Handler stopping; ChannelFacade: " + channelFacade);
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void stopping(ChannelFacade channelFacade) {
        if (logger.isDebugEnabled()) logger.debug("Client Handler stopped; ChannelFacade: " + channelFacade);
		// To change body of implemented methods use File | Settings | File
		// Templates.
	}

    @Override
    public String toString() {
        return "HttpEventHandler{" +
                "clientSideFacade=" + clientSideFacade +
                ", serverSideFacade=" + serverSideFacade +
                ", httpResponseMessage=" + httpResponseMessage +
                ", httpRequestMessage=" + httpRequestMessage +
                '}';
    }
}
