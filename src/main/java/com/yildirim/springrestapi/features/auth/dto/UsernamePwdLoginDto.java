package com.yildirim.springrestapi.features.auth.dto;

import com.yildirim.springrestapi.features.user.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsernamePwdLoginDto(
        @NotBlank
        @Size(min = User.MIN_USERNAME_LENGTH, max = User.MAX_USERNAME_LENGTH)
        String username,

        @NotBlank
        @Size(min = User.MIN_PASSWORD_LEN, max = User.MAX_PASSWORD_LEN)
        String password
) {
}
