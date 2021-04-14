package com.mitrakoff.tomstman.model;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import okhttp3.*;
import org.ini4j.Ini;
import org.ini4j.Profile;

public class Model {
    private final OkHttpClient client = new OkHttpClient();
    private /*final*/ Ini ini;
    private List<RequestItem> requests = Collections.emptyList();

    public Model() {
        try {
            final File file = new File("settings.ini");
            file.createNewFile();
            ini = new Ini(file);
            reloadRequests();
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
            final Request request = new Request.Builder().url(item.url).method(item.method, item.method.equals("GET") ? null : body).build();
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

    public synchronized void saveRequest(RequestItem item) {
        try {
            ini.add("requests", "item", item);
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
            requests.forEach(item -> ini.add("requests", "item", item));
            ini.store();
            reloadRequests();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void reloadRequests() {
        final Profile.Section section = ini.get("requests");
        if (section != null) {
            final List<String> items = section.getAll("item");
            if (items != null)
                this.requests = items.stream().map(RequestItem::fromString).collect(Collectors.toList());
        }
    }
}
