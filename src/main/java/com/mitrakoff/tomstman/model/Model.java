package com.mitrakoff.tomstman.model;

import java.io.*;
import java.util.*;
import com.google.gson.*;
import io.burt.jmespath.gson.GsonRuntime;
import okhttp3.*;
import org.ini4j.Ini;
import org.ini4j.Profile;

public class Model {
    static private final String APPLICATION_JSON = "application/json";
    static private final String SECTION_NAME = "requests";

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
    private final GsonRuntime gsonRuntime = new GsonRuntime();
    private /*final*/ Ini ini;
    private List<RequestItem> requests = new ArrayList<>();

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
                            ? gsonPretty.toJson(jq(item.jmesPath, gson.fromJson(responseBody.string(), JsonElement.class))) // pretty json
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

    public synchronized void saveRequest(RequestItem item) {
        try {
            final String value = gson.toJson(item);
            final Profile.Section section = ini.get(SECTION_NAME);
            if (section == null)
                ini.add(SECTION_NAME, item.name, value);
            else if (section.containsKey(item.name))
                section.replace(item.name, value);
            else section.add(item.name, value);
            ini.store();
            reloadRequests();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void removeRequest(String name) {
        try {
            requests.removeIf(item -> item.name.equals(name));
            ini.remove(SECTION_NAME, name);
            ini.store();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void reloadRequests() {
        final Profile.Section section = ini.get(SECTION_NAME);
        if (section != null) {
            requests.clear();
            for (String key : section.keySet()) { // keySet preserves the order
                final String value = section.get(key);
                requests.add(gson.fromJson(value, RequestItem.class));
            }
        }
    }

    private synchronized void addSampleRequests() {
        saveRequest(new RequestItem("GET example.com", "https://example.com", "GET", "", "", Collections.emptyMap()));
        saveRequest(new RequestItem("GET google.com", "https://google.com", "GET", "", "", Collections.emptyMap()));
        saveRequest(new RequestItem("POST example.com", "https://example.com", "POST", "{\"json\": \"body\"}", "", Collections.singletonMap("Authorization", "Bearer 12345")));
    }

    private boolean bodyApplicable(RequestItem item) {
        return !item.method.equals("GET") && !item.method.equals("HEAD");
    }

    private JsonElement jq(String what, JsonElement where) {
        if (what.isEmpty()) return where;
        return gsonRuntime.compile(what).search(where);
    }
}
