package com.mitrakoff.tomstman.view;

import java.util.*;

public class ResponseData {
    public final String response;
    public final int status;
    public final long elapsedTimeMsec;

    public ResponseData(String response, int status, long elapsedTimeMsec) {
        this.response = response;
        this.status = status;
        this.elapsedTimeMsec = elapsedTimeMsec;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ResponseData.class.getSimpleName() + "[", "]")
                .add("response='" + response + "'")
                .add("status=" + status)
                .add("elapsedTimeMsec=" + elapsedTimeMsec)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResponseData that = (ResponseData) o;
        return status == that.status && elapsedTimeMsec == that.elapsedTimeMsec && response.equals(that.response);
    }

    @Override
    public int hashCode() {
        return Objects.hash(response, status, elapsedTimeMsec);
    }
}
