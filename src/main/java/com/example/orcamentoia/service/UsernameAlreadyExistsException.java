package com.example.orcamentoia.service;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException() {
        super("Este nome de usuário já está em uso.");
    }
}
