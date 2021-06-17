package com.mitrakoff.tomstman.view;

import java.util.*;

public interface Controller {
    ResponseData sendRequest(RequestData request);
    void saveRequest(RequestData request);
    void removeRequest(String name);
    List<RequestData> getRequests();
}
