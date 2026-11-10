package com.example.orcamentoia.controller;

import com.example.orcamentoia.dto.AssistantCommandRequest;
import com.example.orcamentoia.dto.AssistantResponse;
import com.example.orcamentoia.service.OpenAiAudioService;
import com.example.orcamentoia.service.VoiceAssistantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {
    private final VoiceAssistantService assistant;
    private final OpenAiAudioService audio;

    public AssistantController(VoiceAssistantService assistant, OpenAiAudioService audio) {
        this.assistant = assistant;
        this.audio = audio;
    }

    @PostMapping("/command")
    public AssistantResponse command(@Valid @RequestBody AssistantCommandRequest request) {
        return assistant.processCommand(request.text());
    }

    @PostMapping(value = "/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AssistantResponse audioCommand(@RequestParam("audio") MultipartFile file) {
        String text = audio.transcribe(file);
        return assistant.processCommand(text);
    }

    @PostMapping(value = "/speak", produces = "audio/mpeg")
    public ResponseEntity<byte[]> speak(@Valid @RequestBody AssistantCommandRequest request) {
        byte[] result = audio.speak(request.text());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=assistant-response.mp3")
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(result);
    }
}
