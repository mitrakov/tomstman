package com.mitrakoff.tomstman.view;

import java.util.*;

public interface Controller {
    ResponseData sendRequest(String url, String method, String jsonBody, String jmes, Map<String, String> headers);
    void saveRequest(String name, String url, String method, String jsonBody, String jmes, Map<String, String> headers);
    void removeRequest(String name);
    List<RequestData> getRequests();
}
