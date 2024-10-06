package com.rakbank.users.controller;

import com.rakbank.users.TestUtil;
import com.rakbank.users.dto.UserPasswordDto;
import com.rakbank.users.entity.User;
import com.rakbank.users.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.function.Consumer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.2"));

    private static final String DEFAULT_EMAIL_DOMAIN = "@localhost.com";
    private static final String DEFAULT_NAME = "John Doe";
    private static final String DEFAULT_PASSWORD = "JohnDoe897";

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restUserMockMvc;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeAll
    public static void setUpContainer() {
        postgreSQLContainer.start();
    }

    @BeforeEach
    public void initTest() {
        user = createEntity(em);
    }

//    @AfterAll
//    public static void tearDownContainer() {
//        postgreSQLContainer.stop();
//    }

    public static User createEntity(EntityManager em) {
        String uniqueEmail = "johndoe" + RandomStringUtils.randomNumeric(5) + DEFAULT_EMAIL_DOMAIN;
        User user = new User();
        user.setEmail(uniqueEmail);
        user.setName(DEFAULT_NAME);
        user.setPassword(DEFAULT_PASSWORD);
        return user;
    }

    @Test
    void canEstablishedConnection() {
        assertThat(postgreSQLContainer.isCreated()).isTrue();
        assertThat(postgreSQLContainer.isRunning()).isTrue();
    }

    @Test
    @Transactional
    void shouldCreateUser() throws Exception {
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        restUserMockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.convertObjectToJsonBytes(user))
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated());

        // Validate the User in the database
        assertPersistedUsers(users -> {
            assertThat(users).hasSize(databaseSizeBeforeCreate + 1);
            User testUser = users.get(users.size() - 1);
            assertThat(testUser.getName()).isEqualTo(DEFAULT_NAME);
        });
    }

    @Test
    @Transactional
    void shouldGetAllUsers() throws Exception {
        userRepository.saveAndFlush(user);

        restUserMockMvc.perform(get("/api/user")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.content[0].name").value(DEFAULT_NAME));
    }

    @Test
    @Transactional
    void shouldgetUserById() throws Exception {
        userRepository.saveAndFlush(user);

        restUserMockMvc.perform(get("/api/user/"+user.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.name").value(DEFAULT_NAME));
    }

    @Test
    @Transactional
    void shouldUpdateUser() throws Exception {
        long userId = 1L;
        String email = "johndoe2024@gmail.com";
        user.setEmail(email);
        userRepository.saveAndFlush(user);

        restUserMockMvc.perform(put("/api/user/"+userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(TestUtil.convertObjectToJsonBytes(user))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @Transactional
    void shouldChangePassword() throws Exception {
        String password = "johnDoe2024";
        UserPasswordDto userPasswordDto = UserPasswordDto.builder()
                .password(password)
                .confirmPassword(password)
                .build();
        userRepository.saveAndFlush(user);

        restUserMockMvc.perform(patch("/api/user/"+user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(TestUtil.convertObjectToJsonBytes(userPasswordDto))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    @Transactional
    void shouldDeleteUser() throws Exception {
        userRepository.saveAndFlush(user);
        int databaseSizeBeforeDelete = userRepository.findAll().size();
        restUserMockMvc.perform(delete("/api/user/"+user.getId())
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

        assertPersistedUsers(users -> assertThat(users).hasSize(databaseSizeBeforeDelete - 1));
    }

    private void assertPersistedUsers(Consumer<List<User>> userAssertion) {
        userAssertion.accept(userRepository.findAll());
    }
}