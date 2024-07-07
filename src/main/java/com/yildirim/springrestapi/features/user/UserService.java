package com.yildirim.springrestapi.features.user;

import com.yildirim.springrestapi.features.auth.AuthenticationService;
import com.yildirim.springrestapi.features.auth.Role;
import com.yildirim.springrestapi.features.user.dto.RegisterUserDto;
import com.yildirim.springrestapi.features.user.dto.UpdateUserDto;
import com.yildirim.springrestapi.features.user.exceptions.UserNotFoundException;
import com.yildirim.springrestapi.features.user.exceptions.UserUpdateException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authService;
    private final ApplicationEventPublisher publisher;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User saveUser(RegisterUserDto userDto) {
        return userRepository.save(User.builder()
                .username(userDto.username())
                .flags(0)
                .birthDate(userDto.birthDate())
                .displayName(userDto.displayName())
                .password(passwordEncoder.encode(userDto.password()))
                .privacy(UserPrivacy.PRIVATE)
                .role(Role.REGULAR)
                .gender(userDto.gender())
                .build()
        );
    }

    private String handlePasswordChange(String password) {
        if (authService.isValidPassword(password)) {
            return passwordEncoder.encode(password);
        } else {
            throw new IllegalArgumentException("Invalid password");
        }
    }

    public void disableUser(String id) throws UserNotFoundException {
        try {
            User user = userRepository.getReferenceById(id);
            user.setDeleted(true);
            user.setPrivacy(UserPrivacy.PRIVATE);
            userRepository.save(user);
        } catch (EntityNotFoundException e) {
            throw new UserNotFoundException();
        }
    }

    public void updateEmail(String id, String email) throws UserNotFoundException {
        try {
            User user = userRepository.getReferenceById(id);
            if (!canUpdateEmail(user)) {
                throw new UserUpdateException("Cannot update email");
            }
            if (userRepository.existsByEmail(email)) {
                throw new UserUpdateException("Email already in use");
            }
            user.setEmail(email);
            user.setEmailUpdated(true);
            user.setEmailVerified(false);
            userRepository.save(user);
            publisher.publishEvent(new UserEvents.EmailUpdatedEvent(user));
        } catch (EntityNotFoundException e) {
            throw new UserNotFoundException();
        }
    }

    private boolean canUpdateEmail(User user) {
        return !Flags.isFlagSet(user.getFlags(), Flags.UserFlag.EMAIL_UPDATED)
                || !Flags.isFlagSet(user.getFlags(), Flags.UserFlag.PASSWORD_RESET)
                || Flags.isFlagSet(user.getFlags(), Flags.UserFlag.EMAIL_VERIFIED);
    }

    public void updateUsername(String id, String username) throws UserNotFoundException, UserUpdateException {
        try {
            if (userRepository.existsByUsername(username)) {
                throw new UserUpdateException("Username already in use");
            }
            User user = userRepository.getReferenceById(id);
            if (username.equals(user.getUsername())) {
                throw new UserUpdateException("New username cannot be the same as the old username");
            }
            user.setUsername(username);
            userRepository.save(user);
        } catch (EntityNotFoundException e) {
            throw new UserNotFoundException();
        }
    }

    public void updatePassword(String id, String password) throws UserNotFoundException {
        try {
            User user = userRepository.getReferenceById(id);
            if (password.equals(user.getPassword())) {
                throw new UserUpdateException("New password cannot be the same as the old password");
            }
            user.setPassword(handlePasswordChange(password));
            userRepository.save(user);
            publisher.publishEvent(new UserEvents.PasswordChangedEvent(user));
        } catch (EntityNotFoundException e) {
            throw new UserNotFoundException();
        }
    }

    public boolean canUpdatePrivacy(User user) throws UserNotFoundException {
        try {
            int userFlags = user.getFlags();
            return !Flags.isFlagSet(userFlags, Flags.UserFlag.DISABLED)
                    && !Flags.isFlagSet(userFlags, Flags.UserFlag.PASSWORD_RESET)
                    && Flags.isFlagSet(userFlags, Flags.UserFlag.EMAIL_VERIFIED);
        } catch (EntityNotFoundException e) {
            throw new UserNotFoundException();
        }
    }


    public User updateUser(String id, UpdateUserDto user) throws UserNotFoundException, UserUpdateException {
        try {
            var existingUser = userRepository.getReferenceById(id);

            if (user.displayName() != null) {
                if (user.displayName().equals(existingUser.getDisplayName())) {
                    throw new UserUpdateException("New display name cannot be the same as the old display name");
                }
                existingUser.setDisplayName(user.displayName());
            }
            if (user.bio() != null) {
                existingUser.setBio(user.bio());
            }
            if (user.birthDate() != null) {
                existingUser.setBirthDate(user.birthDate());
            }
            if (user.gender() != null) {
                existingUser.setGender(user.gender());
            }
            if (user.privacy() != null && canUpdatePrivacy(existingUser)) {
                existingUser.setPrivacy(user.privacy());
            }
            return existingUser;
        } catch (EntityNotFoundException e) {
            throw new UserNotFoundException();
        } catch (Exception e) {
            throw new UserUpdateException("Could not update user");
        }
    }
}
