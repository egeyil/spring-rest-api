package com.yildirim.springrestapi.features.user.dto;


import com.yildirim.springrestapi.features.user.Gender;
import com.yildirim.springrestapi.features.user.User;
import com.yildirim.springrestapi.features.user.UserPrivacy;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateUserDto(
        @Size(min = User.MIN_DISPLAY_NAME_LENGTH, max = User.MAX_DISPLAY_NAME_LENGTH)
        String displayName,

        @Size(max = User.MAX_BIO_LENGTH)
        String bio,

        @PastOrPresent
        LocalDateTime birthDate,

        Gender gender,

        UserPrivacy privacy
) {
}
