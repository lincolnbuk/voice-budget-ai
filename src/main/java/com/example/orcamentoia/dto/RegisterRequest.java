package com.example.orcamentoia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 80) @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Use apenas letras, números, ponto, hífen ou sublinhado") String username,
        @NotBlank @Size(min = 12, max = 72, message = "A senha deve ter entre 12 e 72 caracteres") String password
) { }
