package io.github.appleaww.messenger.model.dto;


import io.github.appleaww.messenger.model.entity.User;

public record RegisterRequestDTO(String username,
                                 String name,
                                 String password,
                                 String email,
                                 User.Role role
){
    public RegisterRequestDTO(String username, String name, String password, String email){
        this(username,name, password, email, User.Role.USER);
    }
}

