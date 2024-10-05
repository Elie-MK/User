package com.rakbank.users.service;

import com.rakbank.users.dto.UserDto;
import com.rakbank.users.dto.UserPasswordDto;
import com.rakbank.users.dto.UserRegistrationDto;
import com.rakbank.users.dto.UserUpdateDto;
import com.rakbank.users.entity.User;
import com.rakbank.users.repository.UserRepository;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    Validator validator;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, validator);
    }

    @Test
    void shouldCreateUser() {

        UserRegistrationDto userRegistrationDto = new UserRegistrationDto();
        userRegistrationDto.setId(1L);
        userRegistrationDto.setEmail("joh@email.com");
        userRegistrationDto.setPassword("Password456");
        userRegistrationDto.setName("john");
        when(userRepository.existsByEmail(userRegistrationDto.getEmail())).thenReturn(false);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail(userRegistrationDto.getEmail());
        savedUser.setName(userRegistrationDto.getName());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDto result = userService.createUser(userRegistrationDto);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(userRegistrationDto.getEmail());
        assertThat(result.getName()).isEqualTo(userRegistrationDto.getName());
        assertThat(result.getId()).isEqualTo(userRegistrationDto.getId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldGetAllUsers() {
        // Given
        Pageable page = PageRequest.of(0, 10);
        List<User> users = Arrays.asList(new User(), new User());
        Page<User> usersPage = new PageImpl<>(users, page, users.size());
        when(userRepository.findAll(page)).thenReturn(usersPage);

        // When
        Page<UserDto> result = userService.getUsers(page);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(users.size());
        assertThat(result.getContent()).hasSize(users.size());
        verify(userRepository).findAll(page);
    }

    @Test
    void shouldGetUserById() {
        UserRegistrationDto userRegistrationDto = new UserRegistrationDto();
        userRegistrationDto.setId(1L);
        userRegistrationDto.setEmail("joh@email.com");
        userRegistrationDto.setPassword("Password456");
        userRegistrationDto.setName("john");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail(userRegistrationDto.getEmail());
        savedUser.setName(userRegistrationDto.getName());
        when(userRepository.findById(1L)).thenReturn(Optional.of(savedUser));

        UserDto result = userService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(userRegistrationDto.getEmail());
        assertThat(result.getName()).isEqualTo(userRegistrationDto.getName());
        assertThat(result.getId()).isEqualTo(userRegistrationDto.getId());

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void shouldUpdateUser() {
        UserUpdateDto userRegistrationDto = new UserUpdateDto();
        userRegistrationDto.setEmail("joh@email.com");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setEmail(userRegistrationDto.getEmail());
        when(userRepository.findById(2L)).thenReturn(Optional.of(savedUser));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        Optional<UserDto> result = userService.updateUser(savedUser.getId(), userRegistrationDto);

        assertThat(result).isNotEmpty();
        assertThat(result.get().getEmail()).isEqualTo(userRegistrationDto.getEmail());
        assertThat(result.get().getName()).isEqualTo(userRegistrationDto.getName());

        verify(userRepository, times(1)).findById(2L);

    }

    @Test
    void shouldChangePassword() {
        UserPasswordDto userPasswordDto = new UserPasswordDto();
        userPasswordDto.setPassword("JohnDoe9876");
        userPasswordDto.setConfirmPassword("JohnDoe9876");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setPassword(userPasswordDto.getConfirmPassword());
        when(userRepository.findById(2L)).thenReturn(Optional.of(savedUser));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        String result = userService.changePassword(savedUser.getId(), userPasswordDto);

        assertThat(result).isEqualTo("Your password was changed successfully");
        verify(userRepository, times(1)).findById(2L);
    }

    @Test
    void shouldDeleteUser() {

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setEmail("doe@email.com");
        savedUser.setName("Doe");
        when(userRepository.findById(2L)).thenReturn(Optional.of(savedUser));

        String result = userService.deleteUser(savedUser.getId());

        assertThat(result).isEqualTo("User with Id " + savedUser.getId() + " was deleted successfully");

        verify(userRepository, times(1)).findById(2L);

    }
}