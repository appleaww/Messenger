package io.github.appleaww.messenger.controller;

import io.github.appleaww.messenger.model.dto.request.SubscribeRequestDTO;
import io.github.appleaww.messenger.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.github.appleaww.messenger.model.entity.User;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SubscribeController {
    private final UserService userService;

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(@RequestBody SubscribeRequestDTO subscribeRequestDTO, @AuthenticationPrincipal User user){
        userService.activateSubscription(user, subscribeRequestDTO);
        return ResponseEntity.ok("Subscription " + subscribeRequestDTO.tier() + " successfully activated" );

    }
}
