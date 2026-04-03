package io.github.appleaww.messenger.service;

import io.github.appleaww.messenger.model.dto.request.SubscribeRequestDTO;
import io.github.appleaww.messenger.model.entity.User;
import io.github.appleaww.messenger.repository.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final MeterRegistry meterRegistry;

    @Transactional
    public void activateSubscription(User currentUser, SubscribeRequestDTO subscribeRequestDTO) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("User with id " + currentUser.getId() + " not found"));

        if (user.getIsPremium()) {
            throw new IllegalArgumentException("User with id " + user.getId() + " already has a premium subscription");
        }
        user.setIsPremium(true);
        userRepository.save(user);

        meterRegistry.counter("messenger.subscriptions.started", "tier", subscribeRequestDTO.tier(), "userId", user.getId().toString()).increment();
        log.info("User with id {} activated {} subscription", user.getId(), subscribeRequestDTO.tier());


    }
}
