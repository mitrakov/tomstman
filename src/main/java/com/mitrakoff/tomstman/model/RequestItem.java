package com.mitrakoff.tomstman.model;

import java.util.Objects;

public class RequestItem {
    final public String name;
    final public String url;
    final public String method;
    final public String jsonBody;

    public RequestItem(String name, String url, String method, String jsonBody) {
        assert !name.isEmpty();
        assert !url.isEmpty();
        assert method.equals("GET") || method.equals("POST") || method.equals("PUT") || method.equals("DELETE")
                || method.equals("PATCH") || method.equals("OPTIONS") || method.equals("HEAD");

        this.name = name;
        this.url = url;
        this.method = method;
        this.jsonBody = jsonBody;
    }

    public static RequestItem fromString(String s) {
        final String[] p = s.split("๏");
        return new RequestItem(p[0], p[1], p[2], p.length == 4 ? p[3] : "");
    }

    @Override
    public String toString() {
        return String.format("%s๏%s๏%s๏%s", name, url, method, jsonBody);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RequestItem)
            return ((RequestItem)o).name.equals(this.name);
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, url, method, jsonBody);
    }
}
