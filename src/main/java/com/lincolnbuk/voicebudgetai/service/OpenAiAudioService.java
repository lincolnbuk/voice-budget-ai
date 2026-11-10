package com.lincolnbuk.voicebudgetai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.client.MultipartBodyBuilder;

import java.util.Map;

@Service
public class OpenAiAudioService {
    private final String apiKey;
    private final RestClient restClient;

    public OpenAiAudioService(@Value("${spring.ai.openai.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public String transcribe(MultipartFile audio) {
        if (audio == null || audio.isEmpty()) throw new IllegalArgumentException("Envie um arquivo de áudio não vazio.");
        if (apiKey.isBlank() || "missing-openai-key".equals(apiKey)) throw new IllegalStateException("Configure OPENAI_API_KEY para usar transcrição de áudio.");

        MultipartBodyBuilder parts = new MultipartBodyBuilder();
        String filename = audio.getOriginalFilename() == null ? "audio.wav" : audio.getOriginalFilename();
        MediaType contentType = audio.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(audio.getContentType());
        parts.part("file", audio.getResource()).filename(filename).contentType(contentType);
        parts.part("model", "whisper-1");
        TranscriptionResponse response = restClient.post()
                .uri("/audio/transcriptions")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(parts.build())
                .retrieve()
                .body(TranscriptionResponse.class);
        if (response == null || response.text() == null || response.text().isBlank()) throw new IllegalStateException("O serviço de transcrição não retornou texto.");
        return response.text();
    }

    public byte[] speak(String text) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("O texto para sintetizar é obrigatório.");
        if (text.length() > 4000) throw new IllegalArgumentException("O texto excede o limite de 4000 caracteres.");
        if (apiKey.isBlank() || "missing-openai-key".equals(apiKey)) throw new IllegalStateException("Configure OPENAI_API_KEY para usar síntese de voz.");
        byte[] response = restClient.post()
                .uri("/audio/speech")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .body(Map.of("model", "tts-1", "voice", "alloy", "input", text, "response_format", "mp3"))
                .retrieve()
                .body(byte[].class);
        if (response == null || response.length == 0) throw new IllegalStateException("O serviço de voz não retornou áudio.");
        return response;
    }

    private record TranscriptionResponse(String text) { }
}
