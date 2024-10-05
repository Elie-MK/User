package com.rakbank.users.repository;

import com.rakbank.users.entity.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.2"));

    @BeforeEach
    void setup() {
        User user = User.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password("Password4589")
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);
    }

    @AfterEach
    void teardown() {
        userRepository.deleteAll();
    }



    @Test
    void canEstablishedConnection() {
        assertThat(postgreSQLContainer.isCreated()).isTrue();
        assertThat(postgreSQLContainer.isRunning()).isTrue();
    }

    @Test
    void shouldReturnTrueWhenEmailAlreadyExist() {
        String email = "john.doe@example.com";

        boolean findUser = userRepository.existsByEmail(email);

        assertThat(findUser).isTrue();
    }

    @Test
    void shouldReturnFalseWhenEmailAlreadyExist() {
        String email = "johndoe@email.com";

        boolean findUser = userRepository.existsByEmail(email);

        assertThat(findUser).isFalse();
    }
}