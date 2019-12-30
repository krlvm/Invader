package ru.krlvm.invader;

import io.netty.handler.codec.http.HttpObject;

public class ComplexProxyToClientResponse {

    private final String serverHostAndPort;
    private final HttpObject httpObject;

    public ComplexProxyToClientResponse(String serverHostAndPort, HttpObject httpObject) {
        this.serverHostAndPort = serverHostAndPort;
        this.httpObject = httpObject;
    }

    public String getServerHostAndPort() {
        return serverHostAndPort;
    }

    public HttpObject getHttpObject() {
        return httpObject;
    }
}
