package ru.krlvm.invader;

import io.netty.handler.codec.http.HttpHeaders;

public class SnifferRecord {

    private final String source;
    private final HttpHeaders headers;
    private final String content;

    public SnifferRecord(String source, HttpHeaders headers, String content) {
        this.source = source;
        this.headers = headers;
        this.content = content;
    }

    public String getSource() {
        return source;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public String getContent() {
        return content;
    }
}
