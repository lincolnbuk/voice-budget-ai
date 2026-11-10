package com.example.orcamentoia.service;

import com.example.orcamentoia.dto.CreateTransactionRequest;
import com.example.orcamentoia.dto.FinancialSummary;
import com.example.orcamentoia.model.Transaction;
import com.example.orcamentoia.model.TransactionType;
import com.example.orcamentoia.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionService {
    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Transaction> listAll(TransactionType type) {
        return type == null ? repository.findAllByOrderByCreatedAtDesc() : repository.findAllByTypeOrderByCreatedAtDesc(type);
    }

    @Transactional(readOnly = true)
    public Transaction getById(long id) {
        return repository.findById(id).orElseThrow(() -> new TransactionNotFoundException(id));
    }

    @Transactional
    public Transaction create(CreateTransactionRequest request) {
        return repository.save(new Transaction(request.description().trim(), request.amount(), request.type(), request.category().trim()));
    }

    @Transactional
    public Transaction create(String description, BigDecimal amount, TransactionType type, String category) {
        if (description == null || description.isBlank()) throw new IllegalArgumentException("Descrição é obrigatória.");
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("O valor precisa ser maior que zero.");
        if (type == null) throw new IllegalArgumentException("Tipo da transação inválido.");
        if (category == null || category.isBlank()) throw new IllegalArgumentException("Categoria é obrigatória.");
        if (description.length() > 200 || category.length() > 80) throw new IllegalArgumentException("Descrição ou categoria excede o limite de caracteres.");
        return repository.save(new Transaction(description.trim(), amount, type, category.trim()));
    }

    @Transactional
    public void delete(long id) {
        Transaction transaction = getById(id);
        repository.delete(transaction);
    }

    @Transactional(readOnly = true)
    public FinancialSummary summary() {
        List<Transaction> transactions = repository.findAll();
        BigDecimal income = sum(transactions, TransactionType.INCOME);
        BigDecimal expense = sum(transactions, TransactionType.EXPENSE);
        return new FinancialSummary(income, expense, income.subtract(expense), transactions.size());
    }

    private BigDecimal sum(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(transaction -> transaction.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
