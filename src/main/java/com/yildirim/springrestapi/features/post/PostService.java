package com.yildirim.springrestapi.features.post;


import com.yildirim.springrestapi.features.post.dto.CreatePostDto;
import com.yildirim.springrestapi.features.post.dto.UpdatePostDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PostService {
    private static final Integer PAGE_SIZE = 10;
    private static final Integer MAX_PAGE_SIZE = 30;
    private static final Integer DEFAULT_PAGE_NUMBER = 0;
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public void deletePost(Long postId) {
        try {
            Post post = postRepository.getReferenceById(postId);
            postRepository.delete(post);
        } catch (EntityNotFoundException e) {
            throw new PostNotFoundException(postId);
        }
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    public List<Post> getPostsByUserId(String userId, int page) {
        return postRepository.findByUserId(userId, Pageable.ofSize(PAGE_SIZE).withPage(page));
    }

    public Post updatePost(UpdatePostDto update, Long postId) {
        try {
            var post = postRepository.getReferenceById(postId);

            if (update.content() != null) {
                post.setContent(update.content());
            }

            if (update.published() != post.isPublished()) {
                post.setPublished(update.published());
            }

            return postRepository.save(post);
        } catch (EntityNotFoundException e) {
            throw new PostNotFoundException(postId);
        }
    }

    public void updatePostContent(Post post, String content) {
        post.setContent(content);
        postRepository.save(post);
    }

    public Post savePost(CreatePostDto createPostDto, String userId) {
        Post post = Post.builder()
                .content(createPostDto.content())
                //.user(createPostDto.userId())
                .build();
        return postRepository.save(post);
    }

/*
    public void updatePostLikes(Post post, int likes) {
        post.setLikes(likes);
        postRepository.save(post);
    }

    public void likePost(Post post) {
        post.setLikes(post.getLikes() + 1);
        postRepository.save(post);
    }

    public void unlikePost(Post post) {
        post.setLikes(post.getLikes() - 1);
        postRepository.save(post);
    }
*/
}
