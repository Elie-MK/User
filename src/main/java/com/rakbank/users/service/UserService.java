package com.rakbank.users.service;

import com.rakbank.users.dto.UserDto;
import com.rakbank.users.dto.UserPasswordDto;
import com.rakbank.users.dto.UserRegistrationDto;
import com.rakbank.users.dto.UserUpdateDto;
import com.rakbank.users.entity.User;
import com.rakbank.users.exceptions.UserException;
import com.rakbank.users.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final Validator validator;

    public UserDto createUser(UserRegistrationDto userDto) {
        validation(userDto);
        var emailExist = userRepository.existsByEmail(userDto.getEmail());
        if (emailExist) {
            throw new UserException("Email already exist");
        }
        log.info("Creating user: {}", userDto.getEmail());
        User user = User.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .password(encodePassword(userDto.getPassword()))
                .build();
        var savedUser = userRepository.save(user);
        return mapToDto(savedUser);
    }

    public Page<UserDto> getUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::mapToDto);
    }

    public UserDto getUserById(Long userId) {
        log.info("Get user by id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found"));
        return mapToDto(user);
    }

    public Optional<UserDto> updateUser(Long userId, UserUpdateDto user) {
        updateValidation(user);
        log.info("Modify user by id: {}", userId);
        User existingUser = userRepository.findById(userId).orElseThrow(() -> new UserException("User not found"));

        if (user.getEmail() != null && !user.getEmail().equals(existingUser.getEmail())) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getName() != null && !user.getName().equals(existingUser.getName())) {
            existingUser.setName(user.getName());
        }

        User updatedUser = userRepository.save(existingUser);
        return Optional.of(mapToDto(updatedUser));
    }

    public String changePassword(Long userId, UserPasswordDto userPassword) {
        // Validate passwords (e.g. length, strength)
        changePasswordValidation(userPassword);

        Optional<User> existingUser = userRepository.findById(userId);
        if (existingUser.isEmpty()) {
            throw new UserException("User not found");
        }

        User user = existingUser.get();

        // Ensure the new password and confirmation match
        if (!userPassword.getPassword().equals(userPassword.getConfirmPassword())) {
            throw new UserException("Passwords do not match");
        }

        // Check if the new password is the same as the current one
        if (matchPassword(user.getPassword(), userPassword.getPassword())) {
            throw new UserException("Your password must be different from your current password");
        }

        // If all validations pass, encode and save the new password
        user.setPassword(encodePassword(userPassword.getConfirmPassword()));
        userRepository.save(user);

        return "Your password was changed successfully";
    }

    public String deleteUser(Long userId) {
        Optional<User> existingUser = userRepository.findById(userId);
        if (existingUser.isEmpty()) {
            throw new UserException("User not found");
        }
        log.info("Delete user by id: {}", userId);
        userRepository.deleteById(userId);
        return "User with Id " + userId + " was deleted successfully";
    }


    private void validation(UserRegistrationDto userDto) {
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(userDto);
        if (!violations.isEmpty()) {
            StringBuilder message = new StringBuilder("Validation errors: ");
            for (ConstraintViolation<UserRegistrationDto> violation : violations) {
                message.append(violation.getMessage()).append("; ");
            }
            throw new UserException(message.toString());
        }
    }

    private void updateValidation(UserUpdateDto userDto) {
        Set<ConstraintViolation<UserUpdateDto>> violations = validator.validate(userDto);
        if (!violations.isEmpty()) {
            StringBuilder message = new StringBuilder("Validation errors: ");
            for (ConstraintViolation<UserUpdateDto> violation : violations) {
                message.append(violation.getMessage()).append("; ");
            }
            throw new UserException(message.toString());
        }
    }

    private void changePasswordValidation(UserPasswordDto userDto) {
        Set<ConstraintViolation<UserPasswordDto>> violations = validator.validate(userDto);
        if (!violations.isEmpty()) {
            StringBuilder message = new StringBuilder("Validation errors: ");
            for (ConstraintViolation<UserPasswordDto> violation : violations) {
                message.append(violation.getMessage()).append("; ");
            }
            throw new UserException(message.toString());
        }
    }

    private UserDto mapToDto(User user) {
        return new UserDto(user);
    }

    private String encodePassword(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        return encoder.encode(password);
    }

    private boolean matchPassword(String hashedPassword, String rawPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        return encoder.matches(rawPassword, hashedPassword);
    }

}
