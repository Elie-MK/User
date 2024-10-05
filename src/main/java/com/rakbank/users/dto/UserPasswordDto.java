package com.rakbank.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPasswordDto {
    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.{8,})([a-zA-Z0-9]+)$", message = "Password must contain at least 8 characters and only alphabets and numbers")
    private String password;

    @NotBlank(message = "Confirm Password is mandatory")
    @Size(min = 8, message = "Confirm Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.{8,})([a-zA-Z0-9]+)$", message = "Confirm Password must contain at least 8 characters and only alphabets and numbers")
    private String confirmPassword;
}
