package com.mitrakoff.tomstman.model;

import java.util.*;
import java.util.function.Consumer;
import com.google.gson.Gson;
import org.ini4j.Ini;
import org.ini4j.Profile;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class RequestManager {
    static private final String SECTION_NAME = "requests";

    private final Gson gson = new Gson();
    private final Ini ini;
    private final List<RequestItem> requests = new ArrayList<>();
    private Optional<Consumer<Throwable>> errorHandler;

    public RequestManager(Ini ini) {
        this.ini = ini;
    }

    public void setErrorHandler(Consumer<Throwable> handler) {
        errorHandler = Optional.ofNullable(handler);
    }

    public List<RequestItem> getRequests() {
        return requests;
    }

    public synchronized void saveRequests(RequestItem ... items) {
        try {
            final Profile.Section section = ini.get(SECTION_NAME);
            for (RequestItem item : items) {
                if (item.name.isEmpty()) continue; // invalid INI key

                final String value = gson.toJson(item);
                if (section == null)
                    ini.add(SECTION_NAME, item.name, value);
                else if (section.containsKey(item.name))
                    section.replace(item.name, value);
                else section.add(item.name, value);
            }
            ini.store();
            reloadRequests();
        } catch (Throwable e) {
            if (errorHandler.isPresent())
                errorHandler.get().accept(e);
            else e.printStackTrace();
        }
    }

    public synchronized void moveRequest(int index, boolean upOrDown) {
        try {
            final boolean isSafe = upOrDown ? index > 0 : index < requests.size() - 1;
            if (isSafe) {
                final RequestItem item = requests.get(index);
                final int indexToDuplicate = upOrDown ? index - 1 : index + 2;
                final int indexToRemove = upOrDown ? index + 1 : index;

                requests.add(indexToDuplicate, item);
                requests.remove(indexToRemove);
                ini.remove(SECTION_NAME);
                saveRequests(requests.toArray(new RequestItem[0]));
            }
        } catch (Throwable e) {
            if (errorHandler.isPresent())
                errorHandler.get().accept(e);
            else e.printStackTrace();
        }
    }

    public synchronized void removeRequest(String name) {
        try {
            requests.removeIf(item -> item.name.equals(name));
            ini.remove(SECTION_NAME, name);
            ini.store();
        } catch (Throwable e) {
            if (errorHandler.isPresent())
                errorHandler.get().accept(e);
            else e.printStackTrace();
        }
    }

    public synchronized void reloadRequests() {
        try {
            final Profile.Section section = ini.get(SECTION_NAME);
            if (section != null) {
                requests.clear();
                for (String key : section.keySet()) { // keySet preserves the order
                    final String value = section.get(key);
                    requests.add(gson.fromJson(value, RequestItem.class));
                }
            }
        } catch (Throwable e) {
            if (errorHandler.isPresent())
                errorHandler.get().accept(e);
            else e.printStackTrace();
        }
    }

    public synchronized void addSampleRequests() {
        try {
            saveRequests(
                new RequestItem("GET example.com", "https://example.com", "GET", "", "", Collections.emptyMap()),
                new RequestItem("GET google.com", "https://google.com", "GET", "", "", Collections.emptyMap()),
                new RequestItem("POST example.com", "https://example.com", "POST", "{\"json\": \"body\"}", "", Collections.singletonMap("Authorization", "Bearer 12345"))
            );
        } catch (Throwable e) {
            if (errorHandler.isPresent())
                errorHandler.get().accept(e);
            else e.printStackTrace();
        }
    }
}
