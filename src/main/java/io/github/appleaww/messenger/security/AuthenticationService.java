package io.github.appleaww.messenger.security;

import io.github.appleaww.messenger.model.dto.AuthenticationResponse;
import io.github.appleaww.messenger.model.dto.LoginRequestDTO;
import io.github.appleaww.messenger.model.dto.RegisterRequestDTO;
import io.github.appleaww.messenger.model.entity.User;
import io.github.appleaww.messenger.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.security.auth.Login;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public void register(RegisterRequestDTO registerRequestDTO){
        if(userRepository.existsByEmail(registerRequestDTO.email())){
            throw new RuntimeException("user with the email " + registerRequestDTO.email() + " already exist");
        }
        if(userRepository.existsByUsername(registerRequestDTO.username())){
            throw new RuntimeException("user with the username " + registerRequestDTO.username() + " already exist");
        }

        User user = new User();
        user.setEmail(registerRequestDTO.email());
        user.setUsername(registerRequestDTO.username());
        user.setRole(registerRequestDTO.role());
        user.setName(registerRequestDTO.name());

        String hashedPassword = passwordEncoder.encode(registerRequestDTO.password());
        user.setPassword(hashedPassword);

        userRepository.save(user);
        log.debug("User has registered with the email {}", registerRequestDTO.email());
    }

    public AuthenticationResponse login(LoginRequestDTO loginRequestDTO){
        User user = userRepository.findByEmail(loginRequestDTO.email())
                .orElseThrow(()->new EntityNotFoundException("User with the email "+ loginRequestDTO.email() + " does not exist"));

        if(!passwordEncoder.matches(loginRequestDTO.password(),user.getPassword())){
            throw new RuntimeException("Invalid password");
        }

        String token = jwtTokenProvider.generateToken(user);

        log.debug("User has logged in with the email {}", user.getEmail());
        return new AuthenticationResponse(token,user.getId(),user.getRole().toString());
    }
}
