package io.github.appleaww.messenger.websocket;

import io.github.appleaww.messenger.model.entity.User;
import lombok.Getter;

import java.security.Principal;

@Getter
public class UserPrincipal implements Principal {
    private final Long userId;
    private final String username;
    private final User user;

    public UserPrincipal(User user) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.user = user;
    }

    @Override
    public String getName() {
        return userId.toString();
    }
}