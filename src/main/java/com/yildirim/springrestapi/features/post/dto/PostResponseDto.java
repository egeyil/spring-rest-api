package com.yildirim.springrestapi.features.post.dto;

public record PostResponseDto(
        Long id,
        String username,
        String content,
        String userId,
        int likes
) {
}
