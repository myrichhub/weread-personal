package com.weread.controller;

import com.weread.service.AuthService;
import com.weread.service.SyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final SyncService syncService;
    private final AuthService authService;

    public SyncController(SyncService syncService, AuthService authService) {
        this.syncService = syncService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> trigger() {
        String sessionId = SecurityContextHolder.getContext().getAuthentication().getName();
        String sessionCookie = authService.getSessionCookie(sessionId);
        if (sessionCookie == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Session expired, please login again"));
        }
        syncService.reset();
        syncService.syncAll(sessionCookie);
        return ResponseEntity.accepted().body(Map.of("message", "Sync started"));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> status() {
        return ResponseEntity.ok(Map.of(
                "state", syncService.getState().name(),
                "message", syncService.getMessage()
        ));
    }
}
