package io.github.appleaww.messenger.security;

import io.github.appleaww.messenger.model.dto.response.AuthenticationResponse;
import io.github.appleaww.messenger.model.dto.request.LoginRequestDTO;
import io.github.appleaww.messenger.model.dto.request.RegisterRequestDTO;
import io.github.appleaww.messenger.model.entity.User;
import io.github.appleaww.messenger.repository.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final MeterRegistry meterRegistry;

    @Transactional
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

        meterRegistry.counter("messenger.users.registered", "role", user.getRole().toString()).increment();

        log.debug("User has registered with the email {}", registerRequestDTO.email());
    }

    @Transactional(readOnly = true)
    public AuthenticationResponse login(LoginRequestDTO loginRequestDTO){
        User user = userRepository.findByEmail(loginRequestDTO.email())
                .orElseThrow(()->new EntityNotFoundException("User with the email "+ loginRequestDTO.email() + " does not exist"));

        if(!passwordEncoder.matches(loginRequestDTO.password(),user.getPassword())){
            throw new RuntimeException("Invalid password");
        }

        String token = jwtTokenProvider.generateToken(user);

        log.debug("User has logged in with the email {}", user.getEmail());

        meterRegistry.counter("messenger.user.activity", "userId", user.getId().toString(),"action_type", "login").increment();

        return new AuthenticationResponse(token,user.getId(),user.getRole().toString(),user.getName(),user.getEmail());
    }
}
