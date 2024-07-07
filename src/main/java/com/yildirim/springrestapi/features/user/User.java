package com.yildirim.springrestapi.features.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yildirim.springrestapi.common.base.BaseEntity;
import com.yildirim.springrestapi.features.auth.Role;
import com.yildirim.springrestapi.features.post.Post;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity
@EntityListeners(UserAuditListener.class)
@Table(name = "users")
public class User extends BaseEntity {
    /**
     * Validation Constraints
     */
    public static final int MIN_DISPLAY_NAME_LENGTH = 2;
    public static final int MAX_DISPLAY_NAME_LENGTH = 20;
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 20;
    public static final int MAX_EMAIL_LENGTH = 100;
    public static final int MAX_BIO_LENGTH = 160;
    public static final int MAX_PASSWORD_LEN = 20;
    public static final int MIN_PASSWORD_LEN = 8;

    @Column(columnDefinition = "integer default 0")
    @Builder.Default
    private int flags = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank(message = "Display Name is required")
    @Size(min = MIN_DISPLAY_NAME_LENGTH, max = MAX_DISPLAY_NAME_LENGTH)
    @Column(length = MAX_DISPLAY_NAME_LENGTH)
    private String displayName;

    @NotBlank(message = "Username is required")
    @Size(min = MIN_USERNAME_LENGTH, max = MAX_USERNAME_LENGTH)
    @Column(unique = true, length = MAX_USERNAME_LENGTH)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    @Size(max = MAX_EMAIL_LENGTH, message = "Email must be less than " + MAX_EMAIL_LENGTH + " characters")
    @Column(unique = true, length = MAX_EMAIL_LENGTH)
    private String email;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @ColumnDefault(value = "'REGULAR'")
    private Role role = Role.REGULAR;

    @PastOrPresent
    private LocalDateTime disabledAt;

    @JsonIgnore
    @NotBlank(message = "Password is required")
    @Column(columnDefinition = "text")
    private String password;

    @Size(max = MAX_BIO_LENGTH, message = "Bio must be less than " + MAX_BIO_LENGTH + " characters")
    @Column(length = MAX_BIO_LENGTH)
    private String bio;

    @Past
    private LocalDateTime birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private UserPrivacy privacy;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private List<Post> posts;

    /**
     * Likes and Follows
     */
    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinTable(
            name = "post_likes",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "post_id")
    )
    private List<Post> postLikes;

    // Users that this user is following
    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_following",
            joinColumns = @JoinColumn(name = "follower_id"),
            inverseJoinColumns = @JoinColumn(name = "following_id")
    )
    private List<User> following;

    // Users that are following this user
    @ManyToMany(mappedBy = "following", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<User> followers;

    // Custom Getters, Setters and Methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Flags.isFlagSet(this.flags, Flags.UserFlag.EMAIL_VERIFIED) == Flags.isFlagSet(user.flags, Flags.UserFlag.EMAIL_VERIFIED) && Objects.equals(id, user.id) && Objects.equals(displayName, user.displayName) && Objects.equals(username, user.username) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, username, email);
    }

    protected void setEmail(String email) {
        this.email = email;
    }

    protected void setDisplayName(@NotBlank(message = "Display Name is required") @Size(min = MIN_DISPLAY_NAME_LENGTH, max = MAX_DISPLAY_NAME_LENGTH) String displayName) {
        this.displayName = displayName;
    }

    protected void setUsername(@NotBlank(message = "Username is required") @Size(min = MIN_USERNAME_LENGTH, max = MAX_USERNAME_LENGTH) String username) {
        this.username = username;
    }

    protected void setRole(Role role) {
        this.role = role;
    }

    protected void setDisabledAt(@PastOrPresent LocalDateTime disabledAt) {
        this.disabledAt = disabledAt;
    }

    public void setPassword(@NotBlank String password) {
        this.password = password;
    }

    protected void setBio(@Size(max = MAX_BIO_LENGTH, message = "Bio must be less than " + MAX_BIO_LENGTH + " characters") String bio) {
        this.bio = bio;
    }

    protected void setBirthDate(@Past LocalDateTime birthDate) {
        this.birthDate = birthDate;
    }

    protected void setGender(Gender gender) {
        this.gender = gender;
    }

    protected void setPrivacy(UserPrivacy privacy) {
        this.privacy = privacy;
    }

    protected void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    protected void setPostLikes(List<Post> postLikes) {
        this.postLikes = postLikes;
    }

    protected void setFollowing(List<User> following) {
        this.following = following;
    }

    protected void setFollowers(List<User> followers) {
        this.followers = followers;
    }

    public void setFlags(int userFlags) {
        this.flags = userFlags;
    }

    public boolean isDisabled() {
        return Flags.isFlagSet(this.flags, Flags.UserFlag.DISABLED);
    }

    public void setDisabled(boolean disabled) {
        this.flags = Flags.setFlag(this.flags, Flags.UserFlag.DISABLED);
    }

    public boolean isEmailVerified() {
        return Flags.isFlagSet(this.flags, Flags.UserFlag.EMAIL_VERIFIED);
    }

    public void setEmailVerified(boolean emailVerified) {
        this.flags = Flags.setFlag(this.flags, Flags.UserFlag.EMAIL_VERIFIED);
    }

    public boolean isDeleted() {
        return Flags.isFlagSet(this.flags, Flags.UserFlag.DELETED);
    }

    public void setDeleted(boolean deleted) {
        this.flags = Flags.setFlag(this.flags, Flags.UserFlag.DELETED);
    }

    public boolean isPasswordReset() {
        return Flags.isFlagSet(this.flags, Flags.UserFlag.PASSWORD_RESET);
    }

    public void setPasswordReset(boolean passwordReset) {
        this.flags = Flags.setFlag(this.flags, Flags.UserFlag.PASSWORD_RESET);
    }

    public boolean isUsernameUpdated() {
        return Flags.isFlagSet(this.flags, Flags.UserFlag.USERNAME_UPDATED);
    }

    public void setUsernameUpdated(boolean usernameUpdated) {
        this.flags = Flags.setFlag(this.flags, Flags.UserFlag.USERNAME_UPDATED);
    }

    public boolean isEmailUpdated() {
        return Flags.isFlagSet(this.flags, Flags.UserFlag.EMAIL_UPDATED);
    }

    public void setEmailUpdated(boolean emailUpdated) {
        this.flags = Flags.setFlag(this.flags, Flags.UserFlag.EMAIL_UPDATED);
    }
}

