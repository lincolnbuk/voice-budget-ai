package com.example.orcamentoia.service;

import com.example.orcamentoia.dto.AssistantResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VoiceAssistantService {
    private final ChatClient chatClient;
    private final BudgetTools budgetTools;

    public VoiceAssistantService(ChatClient.Builder builder, BudgetTools budgetTools) {
        this.chatClient = builder.build();
        this.budgetTools = budgetTools;
    }

    public AssistantResponse processCommand(String text) {
        String reply = chatClient.prompt()
                .system("Você é um assistente financeiro em português do Brasil. Seja claro e breve. " +
                        "Use as ferramentas disponíveis para criar transações ou consultar dados reais; não afirme que salvou algo sem a ferramenta retornar sucesso. " +
                        "Peça esclarecimento se faltar valor, tipo, descrição ou informação necessária. Nunca invente transações ou valores. " +
                        "Valores de transações são positivos; o tipo distingue receita e despesa.")
                .user(text)
                .tools(budgetTools)
                .call()
                .content();
        if (reply == null || reply.isBlank()) reply = "Não consegui gerar uma resposta. Tente reformular o comando.";
        return new AssistantResponse(text, reply, LocalDateTime.now());
    }
}
