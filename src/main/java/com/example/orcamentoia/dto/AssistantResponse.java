package com.example.orcamentoia.dto;

import java.time.LocalDateTime;

public record AssistantResponse(String originalText, String reply, LocalDateTime timestamp) { }
