package com.mitrakoff.tomstman.model;

import java.util.*;

public class RequestItem {
    final public String name;
    final public String url;
    final public String method;
    final public String jsonBody;
    final public Map<String, String> headers;

    public RequestItem(String name, String url, String method, String jsonBody, Map<String, String> headers) {
        this.name = name;
        this.url = url;
        this.method = method;
        this.jsonBody = jsonBody;
        this.headers = headers;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RequestItem.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("url='" + url + "'")
                .add("method='" + method + "'")
                .add("jsonBody='" + jsonBody + "'")
                .add("headers=" + headers)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestItem that = (RequestItem) o;
        return name.equals(that.name) && url.equals(that.url) && method.equals(that.method) && jsonBody.equals(that.jsonBody) && headers.equals(that.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, url, method, jsonBody, headers);
    }
}
