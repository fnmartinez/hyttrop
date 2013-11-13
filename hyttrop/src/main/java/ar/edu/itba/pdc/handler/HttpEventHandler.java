package ar.edu.itba.pdc.handler;

import ar.edu.itba.pdc.message.HttpHeader;
import ar.edu.itba.pdc.message.HttpMessage;
import ar.edu.itba.pdc.message.HttpRequestMessage;
import ar.edu.itba.pdc.message.HttpResponseMessage;
import ar.edu.itba.pdc.statistics.StatisticsWatcher;
import ar.edu.itba.pdc.transformations.L33tTransformation;
import ar.edu.itba.pdc.transformations.TransformationChain;
import ar.thorium.dispatcher.Dispatcher;
import ar.thorium.handler.EventHandler;
import ar.thorium.utils.ChannelFacade;
import ar.thorium.utils.Message;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
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
    private boolean responseHeadersSent;

    public HttpEventHandler(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void handleRead(ChannelFacade channelFacade, Message message) {
        if (logger.isDebugEnabled()) logger.debug("Handling read from client; ChannelFacade: " + channelFacade + " Message: " + message);
        if (httpRequestMessage == null) {
            if (logger.isTraceEnabled()) logger.trace("A message arrived from the client.");
            if (message instanceof HttpResponseMessage) {
                httpResponseMessage = (HttpResponseMessage) message;
                logger.info("Sending a proxy made response message: " + httpResponseMessage);
                sendResponseMessage();
                return;
            }
            httpRequestMessage = (HttpRequestMessage) message;
            SocketChannel channel;
            try {
                String[] addressPort = httpRequestMessage.getHeader("Host").getValue().split(":");
                InetSocketAddress address = new InetSocketAddress(addressPort[0],
                        (addressPort.length == 1 ? DEFAULT_HTTP_PORT: Integer.parseInt(addressPort[addressPort.length - 1])));
                if (address.isUnresolved()) {
                    logger.info("Could not resolve host " + address + ":" + DEFAULT_HTTP_PORT + ". Responding with 404 Not Found.");
                    httpResponseMessage = HttpResponseMessage.NOT_FOUND;
                    sendResponseMessage();
                    return;
                }
                channel = SocketChannel.open(address);
                if (logger.isInfoEnabled()) logger.info("Connection with the origin server " + channel.getRemoteAddress() + " was established successfully.");

                dispatcher.registerChannel(channel, new EventHandler() {
                    @Override
                    public void handleRead(ChannelFacade channelFacade, Message message) {
                        if (logger.isDebugEnabled()) logger.debug("Handling read from server; ChannelFacade: " + channelFacade + " Message: " + message);
						httpResponseMessage = (HttpResponseMessage) message;
                        if(httpResponseMessage.readyToSend()){
	                        HttpEventHandler.this.sendResponseMessage();
	                    }else{
	                    	if (logger.isDebugEnabled()) logger.debug(message.toString() + " Not Ready to Send");
	                    }
                    }

                    @Override
                    public void handleWrite(ChannelFacade channelFacade) {
                        if (logger.isDebugEnabled()) logger.debug("Handling write from server; ChannelFacade: " + channelFacade);
                    }

                    @Override
                    public void handleConnection(ChannelFacade channelFacade) {
                        if (logger.isDebugEnabled()) logger.debug("Handling connection from server; ChannelFacade: " + channelFacade);
                        serverSideFacade = channelFacade;
                        if (logger.isTraceEnabled()) logger.trace("Sending HTTP Request to Server; Request Line=\"" + httpRequestMessage.getRequestLine() + "\"");
                        sendRequestMessage();
                    }

                    @Override
                    public void stopped(ChannelFacade channelFacade) {
                        if (logger.isDebugEnabled()) logger.debug("Server handler stopped; ChannelFacade: " + channelFacade);
                    }

                    @Override
                    public void stopping(ChannelFacade channelFacade) {
                        if (logger.isDebugEnabled()) logger.debug("Server handler stopping; ChannelFacade: " + channelFacade);
                    }
                });
            } catch (IOException e) {
                logger.error(this.toString(), e);
                throw new UnknownError();
            }
        } else {
            if (logger.isTraceEnabled()) logger.trace("Receiving body from client.");
            sendBody(serverSideFacade, httpRequestMessage);
            if (httpRequestMessage.isFinalized()) {
                httpRequestMessage = null;
            }
        }
    }

    private void sendRequestMessage() {
        serverSideFacade.outputQueue().enqueue((httpRequestMessage.getRequestLine() + CRLF).getBytes());
        sendHeaders(serverSideFacade, httpRequestMessage);
        sendBody(serverSideFacade, httpRequestMessage);
    }

    private void sendResponseMessage() {
        if (!responseHeadersSent) {
            if (logger.isDebugEnabled()) logger.debug("Receiving HTTP headers from Server.");
            if (logger.isTraceEnabled()) logger.trace(httpResponseMessage.getStatusLine());
            clientSideFacade.outputQueue().enqueue((httpResponseMessage.getStatusLine() + CRLF).getBytes());
            sendHeaders(clientSideFacade, httpResponseMessage);
            responseHeadersSent = true;
        }
        int bytesToRead = 0;
        while ((bytesToRead = httpResponseMessage.getBody().available()) > 0) {;
            byte[] bytes = new byte[bytesToRead];
            int bytesRead = httpResponseMessage.getBody().read(bytes);
            if (logger.isDebugEnabled()) logger.debug("Read " + bytesRead + " from server. Sending to client.");
            if (logger.isTraceEnabled()) logger.trace(new String(bytes));
            clientSideFacade.outputQueue().enqueue(bytes);
        }
        if (httpResponseMessage.isFinalized()) {
            httpResponseMessage = null;
            clientSideFacade.outputQueue().close();
        }
    }

    private void sendHeaders(ChannelFacade facade, HttpMessage message) {
        for(HttpHeader header : message.getHeaders()) {
            if (logger.isTraceEnabled()) logger.trace(header.toString());
            if (header.getName().equalsIgnoreCase("Connection")) {
                header = new HttpHeader("Connection", "close");
            }
            facade.outputQueue().enqueue((header.toString() + CRLF).getBytes());
        }
        facade.outputQueue().enqueue(CRLF.getBytes());
    }

    private void sendBody(ChannelFacade facade, HttpMessage message) {
        int bytesToRead = 0;
        while ((bytesToRead = message.getBody().available()) > 0) {;
            byte[] bytes = new byte[bytesToRead];
            int bytesRead = message.getBody().read(bytes);
            if (logger.isDebugEnabled()) logger.debug("Read " + bytesRead + " from server. Sending to client.");
            if (logger.isTraceEnabled()) logger.trace(new String(bytes));
            facade.outputQueue().enqueue(bytes);
        }
    }

    @Override
    public void handleWrite(ChannelFacade channelFacade) {
        if (logger.isDebugEnabled()) logger.debug("Handling write from client; ChannelFacade: " + channelFacade);
        TransformationChain.getInstance().transform(channelFacade.outputQueue().getQueue());
    }

    @Override
    public void handleConnection(ChannelFacade channelFacade) {
        if (logger.isDebugEnabled()) logger.debug("Handling connection from client; ChannelFacade: " + channelFacade);
        clientSideFacade = channelFacade;

        StatisticsWatcher w = StatisticsWatcher.getInstance();
        if(w.isRunning()){
            w.updateConnectionsQty();
        }
    }

    @Override
    public void stopped(ChannelFacade channelFacade) {
        if (logger.isDebugEnabled()) logger.debug("Client Handler stopping; ChannelFacade: " + channelFacade);
    }

    @Override
    public void stopping(ChannelFacade channelFacade) {
        if (logger.isDebugEnabled()) logger.debug("Client Handler stopped; ChannelFacade: " + channelFacade);
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
