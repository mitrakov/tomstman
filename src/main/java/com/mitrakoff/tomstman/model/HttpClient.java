package com.mitrakoff.tomstman.model;

import javax.net.ssl.*;
import java.security.*;
import java.security.cert.X509Certificate;
import com.google.gson.*;
import io.burt.jmespath.gson.GsonRuntime;
import okhttp3.*;

public class HttpClient {
    static private final String APPLICATION_JSON = "application/json";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final GsonRuntime gsonRuntime = new GsonRuntime();
    private final OkHttpClient client = buildClient(false);

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
                        ? gson.toJson(jq(item.jmesPath, gson.fromJson(responseBody.string(), JsonElement.class))) // pretty json
                        : responseBody.string()
                    : "Invalid content type"
                : "Invalid response body";
            return new ResponseItem(result, response.code(), System.currentTimeMillis() - ts);
        } catch (Throwable e) {
            return new ResponseItem(String.format("ERROR: %s", e.getMessage()), 0, System.currentTimeMillis() - ts);
        }
    }

    private OkHttpClient buildClient(boolean secure) {
        if (secure) return new OkHttpClient();

        // insecure http client
        final X509TrustManager manager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {}
            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {}
            @Override
            public X509Certificate[] getAcceptedIssuers() {return new X509Certificate[0];}
        };

        try {
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{manager}, new SecureRandom());
            return new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), manager)
                .hostnameVerifier((h,s) -> true)
                .build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean bodyApplicable(RequestItem item) {
        return !item.method.equals("GET") && !item.method.equals("HEAD");
    }

    private JsonElement jq(String what, JsonElement where) {
        if (what.isEmpty()) return where;
        return gsonRuntime.compile(what).search(where);
    }
}
