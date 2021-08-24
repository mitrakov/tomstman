package com.mitrakoff.tomstman.model;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import org.ini4j.Ini;

public class Model {
    private final HttpClient httpClient = new HttpClient();
    private /*final*/ RequestManager requestManager;

    public Model() {
        try {
            final File file = new File("settings.ini");
            final boolean newFileWasCreated = file.createNewFile();
            final Ini ini = new Ini(file);
            requestManager = new RequestManager(ini);
            if (newFileWasCreated)
                requestManager.addSampleRequests();
            else requestManager.reloadRequests();
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void setErrorHandler(Consumer<Throwable> handler) {
        requestManager.setErrorHandler(handler);
    }

    public ResponseItem sendRequest(RequestItem item) {
        return httpClient.sendRequest(item);
    }

    public List<RequestItem> getRequests() {
        return requestManager.getRequests();
    }

    public void saveRequests(RequestItem ... items) {
        requestManager.saveRequests(items);
    }

    public void moveRequest(int index, boolean upOrDown) {
        requestManager.moveRequest(index, upOrDown);
    }

    public void removeRequest(String name) {
        requestManager.removeRequest(name);
    }
}
