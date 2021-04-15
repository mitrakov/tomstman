package com.mitrakoff.tomstman;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.mitrakoff.tomstman.model.Model;
import com.mitrakoff.tomstman.model.RequestItem;
import com.mitrakoff.tomstman.view.Controller;
import com.mitrakoff.tomstman.view.RequestData;

public class SimpleController implements Controller {
    private final Model model;

    public SimpleController(Model model) {
        this.model = model;
    }

    @Override
    public String[] sendRequest(String url, String method, String jsonBody, Map<String, String> headers) {
        return model.sendRequest(new RequestItem("", url, method, jsonBody, headers));
    }

    @Override
    public void saveRequest(String name, String url, String method, String jsonBody, Map<String, String> headers) {
        model.saveRequest(new RequestItem(name, url, method, jsonBody, headers));
    }

    @Override
    public void removeRequest(String name) {
        model.removeRequest(name);
    }

    @Override
    public List<RequestData> getRequests() {
        return model.getRequests().stream().map(r -> new RequestData(r.name, r.url, r.method, r.jsonBody)).collect(Collectors.toList());
    }
}
