package com.lincolnbuk.voicebudgetai.service;

import com.lincolnbuk.voicebudgetai.model.Transaction;
import com.lincolnbuk.voicebudgetai.model.TransactionType;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class BudgetTools {
    private final TransactionService transactions;

    public BudgetTools(TransactionService transactions) {
        this.transactions = transactions;
    }

    @Tool(description = "Registra uma transação do usuário autenticado. Informe descrição, valor positivo, tipo INCOME ou EXPENSE e categoria. Nunca invente um valor; peça esclarecimento se faltar informação.")
    public Transaction createTransaction(String description, BigDecimal amount, String type, String category) {
        return transactions.create(currentUsername(), description, amount, parseType(type), category);
    }

    @Tool(description = "Lista transações do usuário autenticado. Informe INCOME ou EXPENSE para filtrar, ou deixe type vazio para listar todas.")
    public List<Transaction> listTransactions(String type) {
        return transactions.listAll(currentUsername(), type == null || type.isBlank() ? null : parseType(type));
    }

    @Tool(description = "Retorna totais de receitas, despesas, saldo e quantidade de transações do usuário autenticado.")
    public Object getFinancialSummary() {
        return transactions.summary(currentUsername());
    }

    private TransactionType parseType(String type) {
        try {
            return TransactionType.valueOf(type.trim().toUpperCase());
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("O tipo deve ser INCOME ou EXPENSE.");
        }
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new IllegalStateException("A ferramenta exige um usuário autenticado.");
        }
        return authentication.getName();
    }
}
