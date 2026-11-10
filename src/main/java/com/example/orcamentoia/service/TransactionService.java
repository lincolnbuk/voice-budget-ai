package com.example.orcamentoia.service;

import com.example.orcamentoia.dto.CreateTransactionRequest;
import com.example.orcamentoia.dto.FinancialSummary;
import com.example.orcamentoia.model.AppUser;
import com.example.orcamentoia.model.Transaction;
import com.example.orcamentoia.model.TransactionType;
import com.example.orcamentoia.repository.AppUserRepository;
import com.example.orcamentoia.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionService {
    private final TransactionRepository repository;
    private final AppUserRepository users;

    public TransactionService(TransactionRepository repository, AppUserRepository users) {
        this.repository = repository;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<Transaction> listAll(String username, TransactionType type) {
        return type == null ? repository.findAllByOwner_UsernameOrderByCreatedAtDesc(username)
                : repository.findAllByOwner_UsernameAndTypeOrderByCreatedAtDesc(username, type);
    }

    @Transactional(readOnly = true)
    public Transaction getById(long id, String username) {
        return repository.findByIdAndOwner_Username(id, username).orElseThrow(() -> new TransactionNotFoundException(id));
    }

    @Transactional
    public Transaction create(String username, CreateTransactionRequest request) {
        return create(username, request.description(), request.amount(), request.type(), request.category());
    }

    @Transactional
    public Transaction create(String username, String description, BigDecimal amount, TransactionType type, String category) {
        String cleanDescription = description == null ? "" : description.trim();
        String cleanCategory = category == null ? "" : category.trim();
        if (cleanDescription.isBlank()) throw new IllegalArgumentException("Descrição é obrigatória.");
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("O valor precisa ser maior que zero.");
        if (type == null) throw new IllegalArgumentException("Tipo da transação inválido.");
        if (cleanCategory.isBlank()) throw new IllegalArgumentException("Categoria é obrigatória.");
        if (cleanDescription.length() > 200 || cleanCategory.length() > 80) throw new IllegalArgumentException("Descrição ou categoria excede o limite de caracteres.");
        AppUser owner = users.findByUsername(username).orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado."));
        return repository.save(new Transaction(cleanDescription, amount, type, cleanCategory, owner));
    }

    @Transactional
    public void delete(long id, String username) {
        repository.delete(getById(id, username));
    }

    @Transactional(readOnly = true)
    public FinancialSummary summary(String username) {
        List<Transaction> transactions = listAll(username, null);
        BigDecimal income = sum(transactions, TransactionType.INCOME);
        BigDecimal expense = sum(transactions, TransactionType.EXPENSE);
        return new FinancialSummary(income, expense, income.subtract(expense), transactions.size());
    }

    private BigDecimal sum(List<Transaction> transactions, TransactionType type) {
        return transactions.stream().filter(transaction -> transaction.getType() == type)
                .map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
