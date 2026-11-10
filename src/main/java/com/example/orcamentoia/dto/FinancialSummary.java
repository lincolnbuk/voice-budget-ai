package com.example.orcamentoia.dto;

import java.math.BigDecimal;

public record FinancialSummary(BigDecimal totalIncome, BigDecimal totalExpense, BigDecimal balance, long transactionCount) { }
