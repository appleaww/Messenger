package io.github.appleaww.messenger.security;

import io.github.appleaww.messenger.metrics.MetricsService;
import io.github.appleaww.messenger.model.dto.response.AuthenticationResponse;
import io.github.appleaww.messenger.model.dto.request.LoginRequestDTO;
import io.github.appleaww.messenger.model.dto.request.RegisterRequestDTO;
import io.github.appleaww.messenger.model.entity.User;
import io.github.appleaww.messenger.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.Environment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final MetricsService metricsService;

    @Value("${app.admin-emails:}")
    private String adminEmails;

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
        user.setName(registerRequestDTO.name());

        boolean isAdmin = isAdminEmail(registerRequestDTO.email());
        user.setRole(isAdmin ? User.Role.ADMIN : User.Role.USER);

        String hashedPassword = passwordEncoder.encode(registerRequestDTO.password());
        user.setPassword(hashedPassword);

        userRepository.save(user);

        metricsService.userRegistered();

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

        metricsService.userLogged();

        return new AuthenticationResponse(token,user.getId(),user.getRole().toString(),user.getName(),user.getEmail());
    }

    private boolean isAdminEmail(String email){
        if(email == null || adminEmails.isBlank()){
            return false;
        }
        return Arrays.stream(adminEmails.split(","))
                .map(String::trim).anyMatch(adminEmail -> adminEmail.equalsIgnoreCase(email));

    }
}
