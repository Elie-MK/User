package com.rakbank.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDto {
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;

    @Email(message = "Email should be valid")
    private String email;
}
