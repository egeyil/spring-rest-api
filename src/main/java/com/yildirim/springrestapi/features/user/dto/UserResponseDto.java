package com.yildirim.springrestapi.features.user.dto;


import com.yildirim.springrestapi.features.auth.Role;
import com.yildirim.springrestapi.features.user.Gender;
import com.yildirim.springrestapi.features.user.User;
import com.yildirim.springrestapi.features.user.UserPrivacy;

import java.time.LocalDateTime;

public record UserResponseDto(
        String id,
        String username,
        String displayName,
        String email,
        String bio,
        Role role,
        boolean disabled,
        boolean emailVerified,
        LocalDateTime disabledAt,
        LocalDateTime birthDate,
        Gender gender,
        UserPrivacy privacy
) {

    public UserResponseDto(User user) {
        this(user.getId(), user.getUsername(), user.getDisplayName(), user.getEmail(), user.getBio(), user.getRole(), user.isDisabled(), user.isEmailVerified(), user.getDisabledAt(), user.getBirthDate(), user.getGender(), user.getPrivacy());
    }
}
