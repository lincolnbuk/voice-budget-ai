package com.example.orcamentoia.service;

import com.example.orcamentoia.model.Transaction;
import com.example.orcamentoia.model.TransactionType;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class BudgetTools {
    private final TransactionService transactions;

    public BudgetTools(TransactionService transactions) {
        this.transactions = transactions;
    }

    @Tool(description = "Registra uma transação financeira. Use INCOME para receita e EXPENSE para despesa. O valor deve ser positivo e informado pelo usuário; não invente valores.")
    public Transaction createTransaction(String description, BigDecimal amount, String type, String category) {
        TransactionType transactionType;
        try {
            transactionType = TransactionType.valueOf(type.trim().toUpperCase());
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("O tipo deve ser INCOME ou EXPENSE.");
        }
        return transactions.create(description, amount, transactionType, category);
    }

    @Tool(description = "Lista as transações financeiras. Informe INCOME ou EXPENSE para filtrar por tipo, ou deixe type vazio para listar todas.")
    public List<Transaction> listTransactions(String type) {
        if (type == null || type.isBlank()) return transactions.listAll(null);
        try {
            return transactions.listAll(TransactionType.valueOf(type.trim().toUpperCase()));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("O filtro deve ser INCOME ou EXPENSE.");
        }
    }

    @Tool(description = "Consulta totais de receitas, despesas, saldo e quantidade de transações. Use para perguntas sobre saldo ou resumo financeiro.")
    public Object getFinancialSummary() {
        return transactions.summary();
    }
}
