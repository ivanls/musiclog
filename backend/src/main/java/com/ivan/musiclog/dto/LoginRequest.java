package com.ivan.musiclog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username or E-mail are required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;

}
