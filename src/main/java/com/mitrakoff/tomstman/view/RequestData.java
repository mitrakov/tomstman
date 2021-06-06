package com.mitrakoff.tomstman.view;

import java.util.*;

public class RequestData {
    final public String name;
    final public String url;
    final public String method;
    final public String jsonBody;
    final public String jmesPath;
    final public Map<String, String> headers;

    public RequestData(String name, String url, String method, String jsonBody, String jmesPath, Map<String, String> headers) {
        this.name = name != null ? name : "";
        this.url = url != null ? url : "";
        this.method = method != null ? method : "";
        this.jsonBody = jsonBody != null ? jsonBody : "";
        this.jmesPath = jmesPath != null ? jmesPath : "";
        this.headers = headers != null ? headers : Collections.emptyMap();
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
        return name.equals(that.name)
            && url.equals(that.url)
            && method.equals(that.method)
            && jsonBody.equals(that.jsonBody)
            && jmesPath.equals(that.jmesPath)
            && headers.equals(that.headers)
        ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, url, method, jsonBody, jmesPath, headers);
    }
}
