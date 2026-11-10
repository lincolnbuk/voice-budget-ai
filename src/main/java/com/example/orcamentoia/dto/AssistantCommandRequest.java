package com.example.orcamentoia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssistantCommandRequest(@NotBlank @Size(max = 2000) String text) { }
