package com.yildirim.springrestapi.features.user.dto;

import com.yildirim.springrestapi.features.auth.ValidPassword;
import com.yildirim.springrestapi.features.user.Gender;
import com.yildirim.springrestapi.features.user.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record RegisterUserDto(
        @Size(min = User.MIN_USERNAME_LENGTH, max = User.MAX_USERNAME_LENGTH)
        @NotBlank
        String username,

        @Size(min = User.MIN_DISPLAY_NAME_LENGTH, max = User.MAX_DISPLAY_NAME_LENGTH)
        @NotBlank
        String displayName,

        @Size(max = 200)
        @NotBlank
        @Email
        String email,

        @Size(min = User.MIN_PASSWORD_LEN, max = User.MAX_PASSWORD_LEN)
        @ValidPassword
        @NotBlank
        String password,

        @PastOrPresent
        LocalDateTime birthDate,

        Gender gender
) {
}
