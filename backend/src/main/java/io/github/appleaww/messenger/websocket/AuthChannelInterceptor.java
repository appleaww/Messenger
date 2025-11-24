package io.github.appleaww.messenger.websocket;

import io.github.appleaww.messenger.model.entity.User;
import io.github.appleaww.messenger.repository.UserRepository;
import io.github.appleaww.messenger.security.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 30)
public class AuthChannelInterceptor implements ChannelInterceptor {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.error("Missing or invalid Authorization header");
                throw new IllegalArgumentException("Missing Authorization header");
            }
            String token = authHeader.substring(7);
            try {
                if (!jwtTokenProvider.validateToken(token)) {
                    log.error("Invalid JWT token");
                    throw new IllegalArgumentException("Invalid JWT token");
                }
                Long userId = jwtTokenProvider.extractId(token);
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException("User with id " + userId + "not found"));

                List<GrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().toString())
                );

                UserPrincipal userPrincipal = new UserPrincipal(user);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userPrincipal,
                        null,
                        authorities
                );

                accessor.setUser(authentication);

                log.info("WebSocket authentication successful for user with id: {}", user.getId());

            } catch (Exception e) {
                log.error("WebSocket authentication failed: {}", e.getMessage(), e);
                throw new IllegalArgumentException("Authentication failed: " + e.getMessage());
            }
        }
        return message;
    }
}
