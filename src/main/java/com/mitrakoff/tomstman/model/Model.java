package com.mitrakoff.tomstman.model;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import com.google.gson.*;
import okhttp3.*;
import org.ini4j.Ini;
import org.ini4j.Profile;

public class Model {
    static private final String APPLICATION_JSON = "application/json";

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
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

    public ResponseItem sendRequest(RequestItem item) {
        final long ts = System.currentTimeMillis();
        try {
            final RequestBody body = RequestBody.create(item.jsonBody, MediaType.parse(APPLICATION_JSON));
            final Request.Builder builder = new Request.Builder().url(item.url).method(item.method, bodyApplicable(item) ? body : null);
            item.headers.forEach(builder::addHeader);

            final Response response = client.newCall(builder.build()).execute();
            final ResponseBody responseBody = response.body();
            final String result = responseBody != null
                    ? responseBody.contentType() != null
                        ? responseBody.contentType().toString().contains(APPLICATION_JSON)
                            ? gsonPretty.toJson(gson.fromJson(responseBody.string(), JsonElement.class)) // pretty json
                            : responseBody.string()
                        : "Invalid content type"
                    : "Invalid response body";
            return new ResponseItem(result, response.code(), System.currentTimeMillis() - ts);
        } catch (Exception e) {
            final StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            return new ResponseItem(writer.toString(), 0, System.currentTimeMillis() - ts);
        }
    }

    public synchronized void saveRequests(RequestItem ... items) {
        try {
            for (RequestItem item: items) {
                ini.add("requests", "item", gson.toJson(item));
            }
            ini.store();
            reloadRequests();
        } catch (Exception e) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private boolean bodyApplicable(RequestItem item) {
        return !item.method.equals("GET") && !item.method.equals("HEAD");
    }
}
