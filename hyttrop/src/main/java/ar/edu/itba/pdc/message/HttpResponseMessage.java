package ar.edu.itba.pdc.message;

import org.apache.log4j.Logger;

import java.io.IOException;

public class HttpResponseMessage extends HttpMessage {

    public static final HttpResponseMessage INTERAL_SERVER_ERROR_RESPONSE;
    public static final HttpResponseMessage BAD_REQUEST;
    private static Logger logger = Logger.getLogger(HttpResponseMessage.class);


    static {
        try {
            logger.info("Creating 'Bad request' response message.");
            BAD_REQUEST = new HttpResponseMessage("HTTP/1.1", HttpStatusCode.SC_BAD_REQUEST);
            INTERAL_SERVER_ERROR_RESPONSE = new HttpResponseMessage("HTTP/1.1", HttpStatusCode.SC_INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            logger.error("An unknown error occurred while creating 'Bad request' response message.", e);
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

}
