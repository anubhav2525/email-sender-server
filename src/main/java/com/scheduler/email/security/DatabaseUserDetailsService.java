package com.scheduler.email.security;

import com.scheduler.email.repositories.auth.AppUserRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {
    private final AuthorizationCacheService authorizationCacheService;
    private final AppUserRepository appUserRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        Optional<UserPrincipal> cached = authorizationCacheService.getCachedUserDetails(username);
        if (cached.isPresent()) {
            return cached.get();
        }

        // L2: Load from database
        UserPrincipal principal = appUserRepository.findSecurityUserByEmailAndDeletedFalse(username)
                .map(UserPrincipal::from)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        // Cache in Redis for subsequent requests
        authorizationCacheService.cacheUserDetails(username, principal);

        return principal;
    }
}
