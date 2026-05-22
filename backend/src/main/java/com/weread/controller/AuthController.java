package com.weread.controller;

import com.weread.dto.PollResponse;
import com.weread.dto.QrCodeResponse;
import com.weread.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/cookie")
    public ResponseEntity<?> loginWithCookie(@RequestBody Map<String, String> body) {
        String cookie = body.get("cookie");
        if (cookie == null || cookie.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "cookie is required"));
        }
        try {
            String token = authService.loginWithCookie(cookie.trim());
            return ResponseEntity.ok(Map.of("token", token));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(502).body(Map.of("error", "无法连接微信读书：" + e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(Map.of("sessionId", name));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        String sessionId = SecurityContextHolder.getContext().getAuthentication().getName();
        authService.logout(sessionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/qrcode")
    public ResponseEntity<QrCodeResponse> qrCode() throws IOException {
        return ResponseEntity.ok(authService.getQrCode());
    }

    @GetMapping("/poll")
    public ResponseEntity<PollResponse> poll(@RequestParam String uuid) throws IOException {
        return ResponseEntity.ok(authService.poll(uuid));
    }
}
