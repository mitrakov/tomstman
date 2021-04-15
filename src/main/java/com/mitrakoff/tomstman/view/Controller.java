package com.mitrakoff.tomstman.view;

import java.util.*;

public interface Controller {
    String[] sendRequest(String url, String method, String jsonBody, Map<String, String> headers);
    void saveRequest(String name, String url, String method, String jsonBody, Map<String, String> headers);
    void removeRequest(String name);
    List<RequestData> getRequests();
}
