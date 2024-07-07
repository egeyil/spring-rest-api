package com.yildirim.springrestapi.features.post.dto;

public record CreatePostDto(
        String content,
        boolean published
) {
}
