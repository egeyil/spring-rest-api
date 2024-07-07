package com.yildirim.springrestapi.features.auth.dto;

import com.yildirim.springrestapi.features.user.dto.UserResponseDto;

public record JwtAuthenticationResponseDto(
        UserResponseDto user
) {
}
