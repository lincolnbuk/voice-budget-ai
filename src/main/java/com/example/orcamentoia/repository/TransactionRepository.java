package com.example.orcamentoia.repository;

import com.example.orcamentoia.model.Transaction;
import com.example.orcamentoia.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByOrderByCreatedAtDesc();
    List<Transaction> findAllByTypeOrderByCreatedAtDesc(TransactionType type);
}
