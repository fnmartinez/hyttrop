package ar.edu.itba.pdc.message;

import java.util.HashMap;
import java.util.Map;

public enum HttpMethod {
    OPTIONS("OPTIONS"),
    GET("GET"),
    HEAD("HEAD"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    TRACE("TRACE"),
    CONNECT("CONNECT"),
    UNKNOWN("UKNOWN");

    private final static Map<String, HttpMethod> methodMap;

    static {
        methodMap = new HashMap<>();
        methodMap.put(OPTIONS.getName(), OPTIONS);
        methodMap.put(GET.getName(), GET);
        methodMap.put(HEAD.getName(), HEAD);
        methodMap.put(POST.getName(), POST);
        methodMap.put(PUT.getName(), PUT);
        methodMap.put(DELETE.getName(), DELETE);
        methodMap.put(TRACE.getName(), TRACE);
        methodMap.put(CONNECT.getName(), CONNECT);
    }

    private String name;

    private HttpMethod(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static HttpMethod getMethod(String method) {
        HttpMethod m = methodMap.get(method);
        return (m == null)? UNKNOWN : m;
    }
}
