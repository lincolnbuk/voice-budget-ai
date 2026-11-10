package com.example.orcamentoia.service;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(long id) {
        super("Transação " + id + " não encontrada.");
    }
}
