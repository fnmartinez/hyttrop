package ar.edu.itba.pdc.message;

import java.io.IOException;
import java.net.URI;

public class HttpRequestMessage extends HttpMessage {

    private HttpMethod method;
    private URI uri;
    private String protocol;
    private String requestLine;

    public HttpRequestMessage(HttpMethod method, URI uri, String protocol) throws IOException {
        this(method, uri, protocol, method + " " + uri.toASCIIString() + " " + protocol);
    }

    public HttpRequestMessage(HttpMethod method, URI uri, String protocol, String requestLine) throws IOException {
        super();
        this.method = method;
        this.uri = uri;
        this.protocol = protocol;
        this.requestLine = requestLine;
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
        return requestLine;
    }
}
