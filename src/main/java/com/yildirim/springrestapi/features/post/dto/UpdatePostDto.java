package com.yildirim.springrestapi.features.post.dto;

public record UpdatePostDto(
        String content,
        String userId,
        boolean published
) {
}
