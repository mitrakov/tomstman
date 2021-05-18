package com.mitrakoff.tomstman;

import java.util.*;
import java.util.stream.Collectors;
import com.mitrakoff.tomstman.model.*;
import com.mitrakoff.tomstman.view.*;

public class SimpleController implements Controller {
    private final Model model;

    public SimpleController(Model model) {
        this.model = model;
    }

    @Override
    public ResponseData sendRequest(String url, String method, String jsonBody, Map<String, String> headers) {
        final ResponseItem response = model.sendRequest(new RequestItem("", url, method, jsonBody, headers));
        return new ResponseData(response.response, response.status, response.elapsedTimeMsec);
    }

    @Override
    public void saveRequest(String name, String url, String method, String jsonBody, Map<String, String> headers) {
        model.saveRequests(new RequestItem(name, url, method, jsonBody, headers));
    }

    @Override
    public void removeRequest(String name) {
        model.removeRequest(name);
    }

    @Override
    public List<RequestData> getRequests() {
        return model.getRequests().stream().map(r -> new RequestData(r.name, r.url, r.method, r.jsonBody, r.headers)).collect(Collectors.toList());
    }
}
