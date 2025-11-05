package io.github.appleaww.messenger.security;

import io.github.appleaww.messenger.model.entity.User;
import io.github.appleaww.messenger.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try{
            String token = extractTokenFromRequest(request);

            if(token != null && jwtTokenProvider.validateToken(token)){
                Long userId = jwtTokenProvider.extractId(token);
                Optional<User> user = userRepository.findById(userId);

                if(user.isPresent()){
                    List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.get().getRole().toString()));
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            user.get(),
                            null,
                            authorities
                    );
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }

        }catch (Exception e){
            log.warn("JWT filter error: {}", e.getMessage());

        }
        filterChain.doFilter(request,response);

    }
    private String extractTokenFromRequest(HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            return authHeader.substring(7);
        }
        return null;
    }
}
