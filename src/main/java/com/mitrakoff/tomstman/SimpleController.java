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
    public ResponseData sendRequest(RequestData r) {
        final ResponseItem response = model.sendRequest(new RequestItem(r.name, r.url, r.method, r.jsonBody, r.jmesPath, r.headers));
        return new ResponseData(response.response, response.status, response.elapsedTimeMsec);
    }

    @Override
    public void saveRequest(RequestData r) {
        model.saveRequests(new RequestItem(r.name, r.url, r.method, r.jsonBody, r.jmesPath, r.headers));
    }

    @Override
    public void removeRequest(String name) {
        model.removeRequest(name);
    }

    @Override
    public void moveRequestUp(int index) {
        model.moveRequest(index, true);
    }

    @Override
    public void moveRequestDown(int index) {
        model.moveRequest(index, false);
    }

    @Override
    public List<RequestData> getRequests() {
        return model.getRequests().stream().map(r -> new RequestData(r.name, r.url, r.method, r.jsonBody, r.jmesPath, r.headers)).collect(Collectors.toList());
    }
}
