package com.example.orcamentoia.controller;

import com.example.orcamentoia.dto.CreateTransactionRequest;
import com.example.orcamentoia.dto.FinancialSummary;
import com.example.orcamentoia.model.Transaction;
import com.example.orcamentoia.model.TransactionType;
import com.example.orcamentoia.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction create(@Valid @RequestBody CreateTransactionRequest request, @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt) {
        return service.create(jwt.getSubject(), request);
    }

    @GetMapping
    public List<Transaction> list(@RequestParam(required = false) TransactionType type, @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt) {
        return service.listAll(jwt.getSubject(), type);
    }

    @GetMapping("/{id}")
    public Transaction get(@PathVariable long id, @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt) {
        return service.getById(id, jwt.getSubject());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id, @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt) {
        service.delete(id, jwt.getSubject());
    }

    @GetMapping("/summary")
    public FinancialSummary summary(@org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt) {
        return service.summary(jwt.getSubject());
    }
}
