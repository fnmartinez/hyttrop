package ar.edu.itba.pdc.message;

import java.util.HashMap;
import java.util.Map;

public enum HttpStatusCode {
    SC_CONTINUE(100, "Continue"),
    SC_SWITCHING_PROTOCOLS(101, "Switching Protocols"),
    SC_OK(200, "OK"),
    SC_CREATED(201, "Created"),
    SC_ACCEPTED(202, "Accepted"),
    SC_NA_INFO(203, "Non-Authoritative Information"),
    SC_NO_CONTENT(204, "No Content"),
    SC_RESET_CONTENT(205, "Reset Content"),
    SC_PARTIAL_CONTENT(206, "Partial Content"),
    SC_MULTIPLE_CHOICES(300, "Multiple Choices"),
    SC_MOVED_PERMANENTLY(301, "Moved Permanently"),
    SC_FOUND(302, "Found"),
    SC_SEE_OTHER(303, "See Other"),
    SC_NOT_MODIFIED(304, "Not Modified"),
    SC_USE_PROXY(305, "Use Proxy"),
    SC_TEMPORARY_REDIRECT(307, "Temporary Redirect"),
    SC_BAD_REQUEST(400, "Bad Request"),
    SC_UNAUTHORIZED(401, "Unauthorized"),
    SC_PAYMENT_REQUIRED(402, "Payment Required"),
    SC_FORBIDDEN(403, "Forbidden"),
    SC_NOT_FOUND(404, "Not Found"),
    SC_METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    SC_NOT_ACCEPTABLE(406, "Not Acceptable"),
    SC_PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
    SC_REQUEST_TIMEOUT(408, "Request Time-out"),
    SC_CONFLICT(409, "Conflict"),
    SC_GONE(410, "Gone"),
    SC_LENGTH_REQUIRED(411, "Length Required"),
    SC_PRECONDITION_FAILED(412, "Precondition Failed"),
    SC_REQUEST_ENTITY_TOO_LARGE(413, "Request Entity Too Large"),
    SC_REQUEST_URI_TOO_LONG(414, "Request-URI Too Large"),
    SC_UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    SC_REQUEST_RANGE_NOT_SATISFIABLE(416, "Requested range not satisfiable"),
    SC_EXPECTATION_FAILED(417, "Expectation Failed"),
    SC_INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    SC_NOT_IMPLEMENTED(501, "Not Implemented"),
    SC_BAD_GATEWAY(502, "Bad Gateway"),
    SC_SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    SC_GATEWAY_TIMEOUT(504, "Gateway Time-out"),
    SC_HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version not supported"),
    SC_UNKNOWN(-1, "Unkown");

    private static final Map<Integer, HttpStatusCode> statusCode;
    static {
        statusCode = new HashMap<>();
        statusCode.put(SC_CONTINUE.getCode(), SC_CONTINUE);
        statusCode.put(SC_SWITCHING_PROTOCOLS.getCode(), SC_SWITCHING_PROTOCOLS);
        statusCode.put(SC_OK.getCode(), SC_OK);
        statusCode.put(SC_CREATED.getCode(), SC_CREATED);
        statusCode.put(SC_ACCEPTED.getCode(), SC_ACCEPTED);
        statusCode.put(SC_NA_INFO.getCode(), SC_NA_INFO);
        statusCode.put(SC_NO_CONTENT.getCode(), SC_NO_CONTENT);
        statusCode.put(SC_RESET_CONTENT.getCode(), SC_RESET_CONTENT);
        statusCode.put(SC_PARTIAL_CONTENT.getCode(), SC_PARTIAL_CONTENT);
        statusCode.put(SC_MULTIPLE_CHOICES.getCode(), SC_MULTIPLE_CHOICES);
        statusCode.put(SC_MOVED_PERMANENTLY.getCode(), SC_MOVED_PERMANENTLY);
        statusCode.put(SC_FOUND.getCode(), SC_FOUND);
        statusCode.put(SC_SEE_OTHER.getCode(), SC_SEE_OTHER);
        statusCode.put(SC_NOT_MODIFIED.getCode(), SC_NOT_MODIFIED);
        statusCode.put(SC_USE_PROXY.getCode(), SC_USE_PROXY);
        statusCode.put(SC_TEMPORARY_REDIRECT.getCode(), SC_TEMPORARY_REDIRECT);
        statusCode.put(SC_BAD_REQUEST.getCode(), SC_BAD_REQUEST);
        statusCode.put(SC_UNAUTHORIZED.getCode(), SC_UNAUTHORIZED);
        statusCode.put(SC_PAYMENT_REQUIRED.getCode(), SC_PAYMENT_REQUIRED);
        statusCode.put(SC_FORBIDDEN.getCode(), SC_FORBIDDEN);
        statusCode.put(SC_NOT_FOUND.getCode(), SC_NOT_FOUND);
        statusCode.put(SC_METHOD_NOT_ALLOWED.getCode(), SC_METHOD_NOT_ALLOWED);
        statusCode.put(SC_NOT_ACCEPTABLE.getCode(), SC_NOT_ACCEPTABLE);
        statusCode.put(SC_PROXY_AUTHENTICATION_REQUIRED.getCode(), SC_PROXY_AUTHENTICATION_REQUIRED);
        statusCode.put(SC_REQUEST_TIMEOUT.getCode(), SC_REQUEST_TIMEOUT);
        statusCode.put(SC_CONFLICT.getCode(), SC_CONFLICT);
        statusCode.put(SC_GONE.getCode(), SC_GONE);
        statusCode.put(SC_LENGTH_REQUIRED.getCode(), SC_LENGTH_REQUIRED);
        statusCode.put(SC_PRECONDITION_FAILED.getCode(), SC_PRECONDITION_FAILED);
        statusCode.put(SC_REQUEST_ENTITY_TOO_LARGE.getCode(), SC_REQUEST_ENTITY_TOO_LARGE);
        statusCode.put(SC_REQUEST_URI_TOO_LONG.getCode(), SC_REQUEST_URI_TOO_LONG);
        statusCode.put(SC_UNSUPPORTED_MEDIA_TYPE.getCode(), SC_UNSUPPORTED_MEDIA_TYPE);
        statusCode.put(SC_REQUEST_RANGE_NOT_SATISFIABLE.getCode(), SC_REQUEST_RANGE_NOT_SATISFIABLE);
        statusCode.put(SC_EXPECTATION_FAILED.getCode(), SC_EXPECTATION_FAILED);
        statusCode.put(SC_INTERNAL_SERVER_ERROR.getCode(), SC_INTERNAL_SERVER_ERROR);
        statusCode.put(SC_NOT_IMPLEMENTED.getCode(), SC_NOT_IMPLEMENTED);
        statusCode.put(SC_BAD_GATEWAY.getCode(), SC_BAD_GATEWAY);
        statusCode.put(SC_SERVICE_UNAVAILABLE.getCode(), SC_SERVICE_UNAVAILABLE);
        statusCode.put(SC_GATEWAY_TIMEOUT.getCode(), SC_GATEWAY_TIMEOUT);
        statusCode.put(SC_HTTP_VERSION_NOT_SUPPORTED.getCode(), SC_HTTP_VERSION_NOT_SUPPORTED);
    }

    private int code;
    private String reasonPhrase;

    private HttpStatusCode(int code, String reasonPhrase) {
        this.code = code;
        this.reasonPhrase = reasonPhrase;
    }

    public int getCode() {
        return this.code;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public static HttpStatusCode getStatusCode(int code) {
        HttpStatusCode sc = statusCode.get(code);
        return (sc == null)? SC_UNKNOWN : sc;
    }
}
