package com.mitrakoff.tomstman.model;

import java.util.*;

public class ResponseItem {
    final public String response;
    final public int status;
    final public long elapsedTimeMsec;

    public ResponseItem(String response, int status, long elapsedTimeMsec) {
        this.response = response;
        this.status = status;
        this.elapsedTimeMsec = elapsedTimeMsec;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ResponseItem.class.getSimpleName() + "[", "]")
                .add("response='" + response + "'")
                .add("status=" + status)
                .add("elapsedTimeMsec=" + elapsedTimeMsec)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResponseItem that = (ResponseItem) o;
        return status == that.status && elapsedTimeMsec == that.elapsedTimeMsec && response.equals(that.response);
    }

    @Override
    public int hashCode() {
        return Objects.hash(response, status, elapsedTimeMsec);
    }
}
