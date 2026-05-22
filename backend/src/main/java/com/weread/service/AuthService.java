package com.weread.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.weread.dto.PollResponse;
import com.weread.dto.QrCodeResponse;
import com.weread.security.JwtUtil;
import com.weread.weread.WeReadClient;
import com.weread.weread.WeReadClient.LoginStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final WeReadClient weReadClient;
    private final JwtUtil jwtUtil;

    public AuthService(WeReadClient weReadClient, JwtUtil jwtUtil) {
        this.weReadClient = weReadClient;
        this.jwtUtil = jwtUtil;
    }

    public String loginWithCookie(String cookieStr) throws IOException {
        JsonNode shelf = weReadClient.fetchBookshelf(cookieStr);
        // WeRead API uses "errCode" (capital C)
        int errCode = shelf.path("errCode").asInt(0);
        if (errCode != 0) {
            throw new IllegalArgumentException("Cookie 无效或已过期，errCode=" + errCode
                    + "，msg=" + shelf.path("errMsg").asText(""));
        }
        String sessionId = UUID.randomUUID().toString();
        weReadClient.storeSession(sessionId, cookieStr);
        log.info("User logged in, sessionId={}", sessionId);
        return jwtUtil.generate(sessionId);
    }

    public QrCodeResponse getQrCode() throws IOException {
        Map<String, String> result = weReadClient.generateQrCode();
        return new QrCodeResponse(result.get("uuid"), result.get("qrUrl"));
    }

    public PollResponse poll(String uuid) throws IOException {
        if (weReadClient.getSession(uuid) != null) {
            return new PollResponse(PollResponse.Status.SUCCESS, jwtUtil.generate(uuid));
        }
        LoginStatus status = weReadClient.checkLoginStatus(uuid);
        return switch (status) {
            case CONFIRMED -> new PollResponse(PollResponse.Status.SUCCESS, jwtUtil.generate(uuid));
            case SCANNED   -> new PollResponse(PollResponse.Status.SCANNED, null);
            case EXPIRED   -> new PollResponse(PollResponse.Status.EXPIRED, null);
            default        -> new PollResponse(PollResponse.Status.WAITING, null);
        };
    }

    public String getSessionCookie(String sessionId) {
        return weReadClient.getSession(sessionId);
    }

    public void logout(String sessionId) {
        weReadClient.removeSession(sessionId);
    }
}
