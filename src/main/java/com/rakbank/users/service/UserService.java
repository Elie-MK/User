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

    /**
     * Creates a new user in the system.
     *
     * @param userDto the user registration data transfer object containing user details
     * @return UserDto representing the created user
     * @throws UserException if the email already exists or if validation fails
     */
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

    /**
     * Retrieves a paginated list of all users.
     *
     * @param pageable the pagination information
     * @return Page<UserDto> containing the paginated user data
     */
    public Page<UserDto> getUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::mapToDto);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId the ID of the user to retrieve
     * @return UserDto representing the requested user
     * @throws UserException if the user is not found
     */
    public UserDto getUserById(Long userId) {
        log.info("Get user by id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found"));
        return mapToDto(user);
    }

    /**
     * Updates an existing user's information.
     *
     * @param userId the ID of the user to update
     * @param user the user update data transfer object containing updated user details
     * @return Optional<UserDto> representing the updated user, if found
     * @throws UserException if the user is not found or if validation fails
     */
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

    /**
     * Changes the password of a user.
     *
     * @param userId the ID of the user whose password is to be changed
     * @param userPassword the user password data transfer object containing the new password
     * @return String message indicating the result of the operation
     * @throws UserException if the user is not found or if validation fails
     */
    public String changePassword(Long userId, UserPasswordDto userPassword) {
        // Validate passwords
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

    /**
     * Deletes a user by their ID.
     *
     * @param userId the ID of the user to delete
     * @return String message indicating the result of the deletion
     * @throws UserException if the user is not found
     */
    public String deleteUser(Long userId) {
        Optional<User> existingUser = userRepository.findById(userId);
        if (existingUser.isEmpty()) {
            throw new UserException("User not found");
        }
        log.info("Delete user by id: {}", userId);
        userRepository.deleteById(userId);
        return "User with Id " + userId + " was deleted successfully";
    }


    /**
     * Validates the user registration data transfer object.
     *
     * @param userDto the user registration data transfer object to validate
     * @throws UserException if validation fails
     */
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

    /**
     * Validates the user update data transfer object.
     *
     * @param userDto the user update data transfer object to validate
     * @throws UserException if validation fails
     */
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

    /**
     * Validates the user password data transfer object.
     *
     * @param userDto the user password data transfer object to validate
     * @throws UserException if validation fails
     */
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

    /**
     * Maps a User entity to a UserDto.
     *
     * @param user the User entity to map
     * @return UserDto representing the mapped user
     */
    private UserDto mapToDto(User user) {
        return new UserDto(user);
    }

    /**
     * Encodes a raw password using BCrypt.
     *
     * @param password the raw password to encode
     * @return String representing the encoded password
     */
    private String encodePassword(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        return encoder.encode(password);
    }

    /**
     * Checks if the raw password matches the hashed password.
     *
     * @param hashedPassword the hashed password to check against
     * @param rawPassword the raw password to validate
     * @return boolean indicating if the passwords match
     */
    private boolean matchPassword(String hashedPassword, String rawPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        return encoder.matches(rawPassword, hashedPassword);
    }

}
