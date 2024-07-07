package com.yildirim.springrestapi.features.post;


import com.yildirim.springrestapi.features.post.dto.CreatePostDto;
import com.yildirim.springrestapi.features.post.dto.UpdatePostDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;

    @PostMapping
    public Post createPost(@Valid CreatePostDto createPostDto, Principal principal) {
        return postService.savePost(createPostDto, principal.getName());
    }

    @PatchMapping("{id}")
    public Post updatePost(@Valid UpdatePostDto updatePostDto, @PathVariable Long id) {
        return postService.updatePost(updatePostDto, id);
    }

    @GetMapping("/{id}")
    public Post getPost(@PathVariable Long id) {
        return postService.getPostById(id);
    }

    @GetMapping("/user/{userId}")
    public List<Post> getPostsByUserId(@PathVariable String userId, @RequestParam(defaultValue = "0") Optional<Integer> page) {
        return postService.getPostsByUserId(userId, page.orElse(0));
    }

    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id) {
        postService.deletePost(id);
    }


}
