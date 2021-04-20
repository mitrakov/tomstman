package com.mitrakoff.tomstman.model;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import com.google.gson.Gson;
import okhttp3.*;
import org.ini4j.Ini;
import org.ini4j.Profile;

public class Model {
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private /*final*/ Ini ini;
    private List<RequestItem> requests = Collections.emptyList();

    public Model() {
        try {
            final File file = new File("settings.ini");
            final boolean newFileWasCreated = file.createNewFile();
            ini = new Ini(file);
            if (newFileWasCreated)
                addSampleRequests();
            else reloadRequests();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public List<RequestItem> getRequests() {
        return requests;
    }

    public String[] sendRequest(RequestItem item) {
        try {
            final RequestBody body = RequestBody.create(item.jsonBody, MediaType.parse("application/json"));
            final Request request = new Request.Builder().url(item.url).method(item.method, bodyAcceptable(item.method) ? body : null).build();
            final Response response = client.newCall(request).execute();
            if (response.body() != null)
                return new String[]{response.body().string(), String.valueOf(response.code())};
        } catch (Exception e) {
            final StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            return new String[]{writer.toString(), "ERROR"};
        }
        return new String[]{};
    }

    public synchronized void saveRequests(RequestItem ... items) {
        try {
            for (RequestItem item: items) {
                ini.add("requests", "item", gson.toJson(item));
            }
            ini.store();
            reloadRequests();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void removeRequest(String name) {
        try {
            requests.removeIf(item -> item.name.equals(name));
            ini.remove("requests", "item");
            requests.forEach(item -> ini.add("requests", "item", gson.toJson(item)));
            ini.store();
            reloadRequests();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean bodyAcceptable(String method) {
        return !method.equals("GET") && !method.equals("HEAD");
    }

    private synchronized void reloadRequests() {
        final Profile.Section section = ini.get("requests");
        if (section != null) {
            final List<String> items = section.getAll("item");
            if (items != null)
                this.requests = items.stream().map(s -> gson.fromJson(s, RequestItem.class)).collect(Collectors.toList());
        }
    }

    private synchronized void addSampleRequests() {
        saveRequests(
            new RequestItem("GET example.com", "https://example.com", "GET", "", Collections.emptyMap()),
            new RequestItem("GET google.com", "https://google.com", "GET", "", Collections.emptyMap()),
            new RequestItem("POST example.com", "https://example.com", "POST", "{\"json\": \"body\"}", Collections.singletonMap("Authorization", "Bearer 12345"))
        );
    }
}
