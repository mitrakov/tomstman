package com.mitrakoff.tomstman.view;

import java.util.Objects;

public class RequestData {
    final public String name;
    final public String url;
    final public String method;
    final public String jsonBody;

    public RequestData(String name, String url, String method, String jsonBody) {
        this.name = name;
        this.url = url;
        this.method = method;
        this.jsonBody = jsonBody;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestData that = (RequestData) o;
        return url.equals(that.url) && method.equals(that.method) && jsonBody.equals(that.jsonBody);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, method, jsonBody);
    }
}
