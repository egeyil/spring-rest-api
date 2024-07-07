package com.yildirim.springrestapi.features.auth;

import com.yildirim.springrestapi.features.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * This class is used by spring security to authenticate and authorize user
 * It implements UserDetails interface, and holds a {@link User} object, which is our domain user
 * This provides us flexibility and separation of concerns since the {@link User} itself doesn't implement security related methods
 */

@NoArgsConstructor
@Getter
@Component
public class SecurityUser implements UserDetails {
    private User user;

    public SecurityUser(User user) {
        this.user = user;
    }

    /**
     * @return a list of authorities (roles) granted to the user
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !user.isDisabled() && !user.isDeleted();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return !user.isDisabled() && !user.isDeleted();
    }
}
