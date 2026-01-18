package io.github.appleaww.messenger.controller;

import io.github.appleaww.messenger.service.OnlineStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/status")
@RequiredArgsConstructor
public class OnlineStatusController {
    private final OnlineStatusService onlineStatusService;

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Boolean>> getUserStatus(@PathVariable Long userId) {
        return ResponseEntity.ok(Map.of("isOnline", onlineStatusService.isUserOnline(userId)));
    }

    @GetMapping("/online")
    public ResponseEntity<Set<Long>> getOnlineUsers() {
        return ResponseEntity.ok(onlineStatusService.getOnlineUsers());
    }
}