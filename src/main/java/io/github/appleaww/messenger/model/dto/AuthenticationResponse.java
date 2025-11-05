package io.github.appleaww.messenger.model.dto;

public record AuthenticationResponse(String token,
                                     Long userId,
                                     String role
) {}
