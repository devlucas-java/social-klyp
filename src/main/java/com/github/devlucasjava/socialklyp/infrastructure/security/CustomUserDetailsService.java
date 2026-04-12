package com.github.devlucasjava.socialklyp.infrastructure.security;

import com.github.devlucasjava.socialklyp.delivery.rest.advice.InvalidCredentialsException;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String login) {
        return userRepository.findByUsernameOrEmail(login)
                .orElseThrow(InvalidCredentialsException::new);

    }
}
