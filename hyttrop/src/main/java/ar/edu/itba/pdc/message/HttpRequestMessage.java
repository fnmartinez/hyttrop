package ar.edu.itba.pdc.message;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;

public class HttpRequestMessage extends HttpMessage {

    private static Logger logger = Logger.getLogger(HttpRequestMessage.class);
    private HttpMethod method;
    private URI uri;
    private String protocol;

    public HttpRequestMessage(HttpMethod method, URI uri, String protocol) throws IOException {
        super();
        this.method = method;
        this.uri = uri;
        this.protocol = protocol;
        if (logger.isDebugEnabled()) logger.debug("Creating HTTP Request " + this.toString());
    }

    public HttpMethod getMethod() {
        return method;
    }

    public URI getUri() {
        return uri;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getRequestLine() {
        HttpHeader host = null;
        if ((host = headers.get("Host")) == null || !uri.toASCIIString().contains(host.getValue())) {
            return method + " " + uri.toASCIIString() + " " + protocol;
        }
        return method + " " + uri.toASCIIString().split(host.getValue())[1] + " " + protocol;
    }

    @Override
    public String toString() {
        return "HttpRequestMessage{" +
                "method=" + method +
                ", uri=" + uri +
                ", protocol='" + protocol + '\'' +
                '}';
    }
}
