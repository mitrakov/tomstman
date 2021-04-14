package com.mitrakoff.tomstman.view;

import java.util.List;

public interface Controller {
    String[] sendRequest(String url, String method, String jsonBody);
    void saveRequest(String name, String url, String method, String jsonBody);
    void removeRequest(String name);
    List<RequestData> getRequests();
}
