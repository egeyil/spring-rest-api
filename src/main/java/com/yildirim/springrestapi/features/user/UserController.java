package com.yildirim.springrestapi.features.user;

import com.yildirim.springrestapi.features.user.dto.RegisterUserDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Transactional
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher publisher;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable @org.hibernate.validator.constraints.UUID UUID id) {
        return userRepository.findById(id.toString()).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/exists")
    public boolean userExists(@RequestParam @org.hibernate.validator.constraints.UUID Optional<UUID> id, @RequestParam @Email Optional<String> email, @RequestParam Optional<String> username) {
        if (id.isEmpty() && email.isEmpty() && username.isEmpty()) {
            throw new IllegalArgumentException("At least one parameter must be provided");
        }
        String idValue = id.map(UUID::toString).orElse(null);

        return userRepository.existsByIdAndEmailAndUsername(idValue, email.orElse(null), username.orElse(null));
    }

    @PostMapping("/register")
    public User register(@RequestBody @Valid RegisterUserDto registerDto) {
        var user = this.userService.saveUser(registerDto);
        publisher.publishEvent(new UserEvents.RegisteredEvent(user));

        return user;
    }

    @PatchMapping("/{id}")
    public void updateUser(
            @PathVariable @NotBlank @org.hibernate.validator.constraints.UUID String id,
            @RequestParam @Size(min = User.MIN_DISPLAY_NAME_LENGTH, max = User.MAX_DISPLAY_NAME_LENGTH) Optional<String> displayName,
            @RequestParam @Size(max = User.MAX_BIO_LENGTH) Optional<String> bio,
            @RequestParam @PastOrPresent Optional<LocalDateTime> birthDate,
            @RequestParam Optional<Gender> gender,
            @RequestParam Optional<UserPrivacy> privacy,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
//        return ResponseEntity.ok(userService.updateUser(id, new UpdateUserDto());
    }

    @PatchMapping("/{id}/email")
    public void updateEmail(
            @PathVariable @NotBlank @org.hibernate.validator.constraints.UUID String id,
            @RequestParam @Size(max = User.MAX_EMAIL_LENGTH) @Email String email
    ) {
        userService.updateEmail(id, email);
    }

    @PatchMapping("/{id}/username")
    public void updateUsername(
            @PathVariable @NotBlank @org.hibernate.validator.constraints.UUID String id,
            @RequestParam @Size(min = User.MIN_USERNAME_LENGTH, max = User.MAX_USERNAME_LENGTH) String username
    ) {
        userService.updateUsername(id, username);
    }

    @PatchMapping("/{id}/password")
    public void updatePwd(
            @PathVariable @NotBlank @org.hibernate.validator.constraints.UUID String id,
            @RequestParam @Size(min = User.MIN_PASSWORD_LEN, max = User.MAX_PASSWORD_LEN) String password
    ) {
        userService.updatePassword(id, password);
    }

    @PatchMapping("/{id}/disable")
    public void disableUser(@PathVariable @NotBlank @org.hibernate.validator.constraints.UUID String id) {
        userService.disableUser(id);
    }

    @PatchMapping("/{id}/enable")
    public void enableUser(@PathVariable @NotBlank @org.hibernate.validator.constraints.UUID String id) {
        //userService.enableUser(id);
    }

    @PatchMapping("/{id}/delete")
    public void deleteUser(@PathVariable @NotBlank @org.hibernate.validator.constraints.UUID String id) {
        // userService.deleteUser(id);
    }

//    @GetMapping("/followers/{id}")
//    public List<User> getFollowers(@PathVariable @NotBlank @org.hibernate.validator.constraints.UUID String id,
//                                   @RequestParam(defaultValue = "0") int page
//    ){
//        return userService.getFollowers(id, page);
//    }
//
//    @GetMapping("/following/{id}")
//    public List<User> getFollowing(@PathVariable @NotBlank @org.hibernate.validator.constraints.UUID String id) {
//        return userService.getFollowing(id, page);
//    }
//
//    @GetMapping("/me")
//    public User getMe(@AuthenticationPrincipal UserDetails userDetails) {
//        return userService.getMe(userDetails);
//    }
}
