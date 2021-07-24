package com.mitrakoff.tomstman.view;

import java.util.*;

public interface Controller {
    ResponseData sendRequest(RequestData request);
    void saveRequest(RequestData request);
    void removeRequest(String name);
    void moveRequestUp(int index);
    void moveRequestDown(int index);
    List<RequestData> getRequests();
}
