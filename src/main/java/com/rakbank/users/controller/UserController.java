package com.rakbank.users.controller;

import com.rakbank.users.dto.UserDto;
import com.rakbank.users.dto.UserPasswordDto;
import com.rakbank.users.dto.UserRegistrationDto;
import com.rakbank.users.dto.UserUpdateDto;
import com.rakbank.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserDto> createUser(@RequestBody UserRegistrationDto userDto) {
        log.info("Creating user: {}", userDto.getEmail());
        var result = userService.createUser(userDto);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Page<UserDto>> getAllUsers(Pageable pageable) {
        log.info("Getting all users");
        var result = userService.getUsers(pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UserDto> getUser(@PathVariable Long userId) {
        log.info("Getting user: {}", userId);
        var result = userService.getUserById(userId);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Optional<UserDto>> updateUser(@PathVariable Long userId, @RequestBody UserUpdateDto user) {
        log.info("Updating user: {}", userId);
        var result = userService.updateUser(userId, user);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> changePassword( @PathVariable Long userId, @RequestBody UserPasswordDto user) {
        log.info("change password for user with id : {}", userId);
        var result = userService.changePassword(userId, user);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> deleteUser(@PathVariable Long userId){
        log.info("Deleting user with id : {}", userId);
        var result = userService.deleteUser(userId);
        return ResponseEntity.ok(result);
    }


}
