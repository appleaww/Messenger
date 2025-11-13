package io.github.appleaww.messenger.controller;

import io.github.appleaww.messenger.model.dto.response.AuthenticationResponse;
import io.github.appleaww.messenger.model.dto.request.LoginRequestDTO;
import io.github.appleaww.messenger.model.dto.request.RegisterRequestDTO;
import io.github.appleaww.messenger.security.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequestDTO registerRequestDTO){
        try{
            authenticationService.register(registerRequestDTO);
            return ResponseEntity.ok("User has registered with the email " + registerRequestDTO.email());
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body("Error in the registration process: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequestDTO){
        try {
            AuthenticationResponse authenticationResponse = authenticationService.login(loginRequestDTO);
            return ResponseEntity.ok(authenticationResponse);
        }catch (RuntimeException e){
            return ResponseEntity.status(401).body("Error in the logging process: " + e.getMessage());
        }
    }

}
