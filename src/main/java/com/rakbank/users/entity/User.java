package com.rakbank.users.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_t")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is mandatory")
    @Length(max = 50, message = "Name must not exceed 50 characters")
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password is mandatory")
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
