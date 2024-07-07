package com.yildirim.springrestapi.features.auth;

import com.yildirim.springrestapi.features.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


/**
 * A service we have to implement for working with UserDetails & JPA. Uses the {@link UserRepository} to retrieve user information.
 * <p>
 * The implemented {@link UserDetailsService#loadUserByUsername(String)} method needs to return a {@link UserDetails} interface,
 * we achieve this through retrieving a {@link com.yildirim.springrestapi.features.user.User} and mapping it to a {@link SecurityUser}, which implements the UserDetails interface.
 */
@RequiredArgsConstructor
@Service
public class JpaUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository
                .getByUsername(username)
                .map(SecurityUser::new)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + username));
    }
}