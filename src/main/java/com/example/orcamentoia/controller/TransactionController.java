package com.example.orcamentoia.controller;

import com.example.orcamentoia.dto.CreateTransactionRequest;
import com.example.orcamentoia.dto.FinancialSummary;
import com.example.orcamentoia.model.Transaction;
import com.example.orcamentoia.model.TransactionType;
import com.example.orcamentoia.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    public Transaction create(@Valid @RequestBody CreateTransactionRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<Transaction> list(@RequestParam(required = false) TransactionType type) {
        return service.listAll(type);
    }

    @GetMapping("/{id}")
    public Transaction get(@PathVariable long id) {
        return service.getById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        service.delete(id);
    }

    @GetMapping("/summary")
    public FinancialSummary summary() {
        return service.summary();
    }
}
