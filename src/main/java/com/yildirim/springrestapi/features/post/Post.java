package com.yildirim.springrestapi.features.post;


import com.yildirim.springrestapi.common.base.BaseEntity;
import com.yildirim.springrestapi.features.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.Objects;
import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "posts")
public class Post extends BaseEntity {
    /**
     * Validation Constraints
     */
    public static final int MIN_CONTENT_LENGTH = 1;
    public static final int MAX_CONTENT_LENGTH = 300;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Setter
    @Size(min = MIN_CONTENT_LENGTH, max = MAX_CONTENT_LENGTH)
    @NotBlank(message = "Content is required")
    @Column(length = MAX_CONTENT_LENGTH)
    private String content;

    @Setter
    @Builder.Default
    @ColumnDefault("false")
    private boolean published = false;

    @Setter
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Setter
    @ManyToMany(mappedBy = "postLikes", fetch = FetchType.LAZY)
    private Set<User> likedBy;

    // Custom Getters, Setters and Methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return id == post.id && Objects.equals(user, post.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user);
    }
}
