package com.grapefruitapps.marketplace.security;

import com.grapefruitapps.marketplace.user.entity.User;
import com.grapefruitapps.marketplace.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Get user by username: {}", username);
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("Not found user by username: " + username)
        );
        log.info("Found user with username: {}", username);
        return new UserDetailsImpl(user);
    }
}
