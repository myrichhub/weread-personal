package com.weread.weread;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WeReadClient {

    private static final Logger log = LoggerFactory.getLogger(WeReadClient.class);

    @Value("${app.weread.base-url}")
    private String baseUrl;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, String> sessionStore = new ConcurrentHashMap<>();

    public WeReadClient(OkHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    // -------------------------------------------------------------------------
    // Login
    // -------------------------------------------------------------------------

    public Map<String, String> generateQrCode() throws IOException {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        Request request = new Request.Builder()
                .url(baseUrl + "/web/login/getLoginStatus?uuid=" + uuid)
                .addHeader("User-Agent", WeReadConstants.USER_AGENT)
                .get()
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            String qrImageUrl = "https://open.weixin.qq.com/connect/qrcode/" + uuid;
            Map<String, String> result = new HashMap<>();
            result.put("uuid", uuid);
            result.put("qrUrl", qrImageUrl);
            return result;
        }
    }

    public LoginStatus checkLoginStatus(String uuid) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/web/login/checkLoginStatus?uuid=" + uuid)
                .addHeader("User-Agent", WeReadConstants.USER_AGENT)
                .addHeader("Referer", baseUrl)
                .get()
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "{}";
            log.debug("checkLoginStatus body: {}", body);
            JsonNode node = objectMapper.readTree(body);
            int code = node.path("errcode").asInt(-1);
            if (code == 0) {
                StringBuilder cookieStr = new StringBuilder();
                for (String header : response.headers("Set-Cookie")) {
                    if (!cookieStr.isEmpty()) cookieStr.append("; ");
                    cookieStr.append(header.split(";")[0]);
                }
                sessionStore.put(uuid, cookieStr.toString());
                return LoginStatus.CONFIRMED;
            } else if (code == -2) {
                return LoginStatus.SCANNED;
            } else if (code == -3 || code == -4) {
                return LoginStatus.EXPIRED;
            }
            return LoginStatus.WAITING;
        }
    }

    public String getSession(String key) { return sessionStore.get(key); }
    public void storeSession(String key, String cookie) { sessionStore.put(key, cookie); }
    public void removeSession(String key) { sessionStore.remove(key); }

    // -------------------------------------------------------------------------
    // Bookshelf
    // -------------------------------------------------------------------------

    public JsonNode fetchBookshelf(String sessionCookie) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/web/shelf/sync")
                .addHeader("User-Agent", WeReadConstants.USER_AGENT)
                .addHeader("Cookie", sessionCookie)
                .addHeader("Referer", baseUrl)
                .get()
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "{}";
            return objectMapper.readTree(body);
        }
    }

    // -------------------------------------------------------------------------
    // Annotations
    // -------------------------------------------------------------------------

    public JsonNode fetchAnnotations(String bookId, String sessionCookie) throws IOException {
        HttpUrl url = HttpUrl.parse(baseUrl + "/web/book/bookmarklist")
                .newBuilder()
                .addQueryParameter("bookId", bookId)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", WeReadConstants.USER_AGENT)
                .addHeader("Cookie", sessionCookie)
                .addHeader("Referer", baseUrl)
                .get()
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "{}";
            return objectMapper.readTree(body);
        }
    }

    // -------------------------------------------------------------------------
    // Thoughts
    // -------------------------------------------------------------------------

    public JsonNode fetchThoughts(String bookId, String sessionCookie) throws IOException {
        HttpUrl url = HttpUrl.parse(baseUrl + "/web/review/list")
                .newBuilder()
                .addQueryParameter("bookId", bookId)
                .addQueryParameter("listType", "11")
                .addQueryParameter("mine", "1")
                .addQueryParameter("synckey", "0")
                .build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", WeReadConstants.USER_AGENT)
                .addHeader("Cookie", sessionCookie)
                .addHeader("Referer", baseUrl)
                .get()
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "{}";
            return objectMapper.readTree(body);
        }
    }

    // -------------------------------------------------------------------------
    // Book info
    // -------------------------------------------------------------------------

    public JsonNode fetchBookInfo(String bookId, String sessionCookie) throws IOException {
        HttpUrl url = HttpUrl.parse(baseUrl + "/web/book/info")
                .newBuilder()
                .addQueryParameter("bookId", bookId)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", WeReadConstants.USER_AGENT)
                .addHeader("Cookie", sessionCookie)
                .addHeader("Referer", baseUrl)
                .get()
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "{}";
            return objectMapper.readTree(body);
        }
    }

    public enum LoginStatus { WAITING, SCANNED, CONFIRMED, EXPIRED }
}
