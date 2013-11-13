package ar.edu.itba.pdc.message;

import ar.edu.itba.pdc.statistics.StatisticsWatcher;
import ar.edu.itba.pdc.utils.ByteArrayQueue;
import org.apache.log4j.Logger;

import java.io.IOException;

public class HttpResponseMessage extends HttpMessage {

    public static final HttpResponseMessage INTERNAL_SERVER_ERROR;
    public static final HttpResponseMessage NOT_IMPLEMENTED;
    public static final HttpResponseMessage BAD_REQUEST;
    public static final HttpResponseMessage BAD_GATEWAY;
    public static final HttpResponseMessage GATEWAY_TIMEOUT;
    public static final HttpResponseMessage NOT_FOUND;
    private static Logger logger = Logger.getLogger(HttpResponseMessage.class);


    static {
        try {
            BAD_REQUEST = new HttpProxyResponseMessage("HTTP/1.1", HttpStatusCode.SC_BAD_REQUEST);
            BAD_REQUEST.finalizeMessage();
            INTERNAL_SERVER_ERROR = new HttpProxyResponseMessage("HTTP/1.1", HttpStatusCode.SC_INTERNAL_SERVER_ERROR);
            INTERNAL_SERVER_ERROR.finalizeMessage();
            NOT_IMPLEMENTED = new HttpProxyResponseMessage("HTTP/1.1", HttpStatusCode.SC_NOT_IMPLEMENTED);
            NOT_IMPLEMENTED.finalizeMessage();
            GATEWAY_TIMEOUT = new HttpProxyResponseMessage("HTTP/1.1", HttpStatusCode.SC_GATEWAY_TIMEOUT);
            GATEWAY_TIMEOUT.finalizeMessage();
            BAD_GATEWAY = new HttpProxyResponseMessage("HTTP/1.1", HttpStatusCode.SC_BAD_GATEWAY);
            BAD_GATEWAY.finalizeMessage();
            NOT_FOUND = new HttpProxyResponseMessage("HTTP/1.1", HttpStatusCode.SC_NOT_FOUND);
            NOT_FOUND.finalizeMessage();
        } catch (IOException e) {
            logger.fatal("An unknown error occurred while creating 'Bad request' response message.", e);
            throw new UnknownError();
        }
    }

    private final String protocol;
    private final HttpStatusCode statusCode;
    private final int code;
    private final String reasonPhrase;

    public HttpResponseMessage(String protocol, HttpStatusCode statusCode) throws IOException {
        this(protocol, statusCode, statusCode.getCode(), statusCode.getReasonPhrase());
    }

    public HttpResponseMessage(String protocol, HttpStatusCode statusCode, int intStatusCode, String reasonPhrase) throws IOException {
        super();
        this.protocol = protocol;
        this.statusCode = statusCode;
        this.code = intStatusCode;
        this.reasonPhrase = reasonPhrase;

        StatisticsWatcher w = StatisticsWatcher.getInstance();
        if(w.isRunning()){
            w.updateStatusCodeStatistics(code);
        }
    }

    public String getProtocol(){
        return this.protocol;
    }

    public HttpStatusCode getStatusCode(){
        return this.statusCode;
    }

    public int getIntStatusCode() {
        return this.code;
    }

    public String getReasonPhrase() {
        return this.reasonPhrase;
    }

    public String getStatusLine() {
        return protocol + " " + code + " " + reasonPhrase;
    }

    @Override
    public String toString() {
        return "HttpResponseMessage{" +
                "protocol='" + protocol + '\'' +
                ", statusCode=" + statusCode +
                ", reasonPhrase='" + reasonPhrase + '\'' +
                '}';
    }

    private static final class HttpProxyResponseMessage extends HttpResponseMessage {

        private final ByteArrayQueue body;

        public HttpProxyResponseMessage(String protocol, HttpStatusCode statusCode) throws IOException {
            super(protocol, statusCode);
            body = new ByteArrayQueue();
            body.write(("<html><title>" + this.getStatusLine() + "</title><body><h1>" + this.getStatusLine() + "</h1><p><i>thorium 0.9</i></body></html>").getBytes());
        }

        @Override
        public ByteArrayQueue getBody() {
            return body;
        }
    }
}
