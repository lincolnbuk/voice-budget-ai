package com.lincolnbuk.voicebudgetai.repository;

import com.lincolnbuk.voicebudgetai.model.Transaction;
import com.lincolnbuk.voicebudgetai.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByOwner_UsernameOrderByCreatedAtDesc(String username);
    List<Transaction> findAllByOwner_UsernameAndTypeOrderByCreatedAtDesc(String username, TransactionType type);
    Optional<Transaction> findByIdAndOwner_Username(long id, String username);
}
