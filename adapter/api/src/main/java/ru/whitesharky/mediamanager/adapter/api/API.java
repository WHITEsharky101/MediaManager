package ru.whitesharky.mediamanager.adapter.api;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URIBuilder;

public abstract class API {
    protected String host;
    protected int port;
    protected String apiPath;

    public API(String host, int port, String apiPath) {
        this.host = host;
        this.port = port;
        this.apiPath = apiPath;
    }

    protected HttpRequest makeRequest(String apiURLMethod) {
        return makeRequest(apiURLMethod, new ArrayList<>());
    }
    protected HttpRequest makeRequest(String apiURLMethod, ArrayList<NameValuePair> httpParameters) {
        URI uri = buildURI(apiURLMethod, httpParameters);

        return HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
    }

    protected HttpRequest makeRequest(String apiURLMethod, Map<String, String> httpPostBody) {
        URI uri = buildURI(apiURLMethod);
        return HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(getFormDataAsString(httpPostBody)))
                .build();
    }

    private static String getFormDataAsString(Map<String, String> formData) {
        StringBuilder formBodyBuilder = new StringBuilder();
        for (Map.Entry<String, String> singleEntry : formData.entrySet()) {
            if (!formBodyBuilder.isEmpty()) {
                formBodyBuilder.append("&");
            }
            formBodyBuilder.append(URLEncoder.encode(singleEntry.getKey(), StandardCharsets.UTF_8));
            formBodyBuilder.append("=");
            formBodyBuilder.append(URLEncoder.encode(singleEntry.getValue(), StandardCharsets.UTF_8));
        }
        return formBodyBuilder.toString();
    }

    protected HttpResponse<String> getResponse(HttpRequest request) {
        return getResponse(request, null, ProxySelector.getDefault());
    }

    protected HttpResponse<String> getResponse(HttpRequest request, HttpCookie cookie) {
        return getResponse(request, cookie, ProxySelector.getDefault());
    }

    protected HttpResponse<String> getResponse(HttpRequest request, HttpCookie cookie, ProxySelector proxySelector) {
        CookieManager cookieManager = new CookieManager();
        if (cookie != null) {
            cookieManager.getCookieStore().add(request.uri(), cookie);
        }
        try (HttpClient client = HttpClient
                .newBuilder()
                .cookieHandler(cookieManager)
                .proxy(proxySelector)
                .build()) {
            return client
                    .send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private URI buildURI(String apiURLMethod) {
        return buildURI(apiURLMethod, new ArrayList<>());
    }
    private URI buildURI(String apiURLMethod, ArrayList<NameValuePair> httpParameters) {
        URI uri;
        try {
            uri = new URIBuilder()
                    .setScheme("http")
                    .setHost(host)
                    .setPort(port)
                    .setPath(apiPath + apiURLMethod)
                    .addParameters(httpParameters)
                    .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return uri;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
