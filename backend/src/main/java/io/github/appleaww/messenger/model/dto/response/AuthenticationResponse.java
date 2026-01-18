package io.github.appleaww.messenger.model.dto.response;

public record AuthenticationResponse(String token,
                                     Long userId,
                                     String role,
                                     String name,
                                     String email
) {}
