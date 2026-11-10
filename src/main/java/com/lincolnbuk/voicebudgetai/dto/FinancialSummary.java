package com.lincolnbuk.voicebudgetai.dto;

import java.math.BigDecimal;

public record FinancialSummary(BigDecimal totalIncome, BigDecimal totalExpense, BigDecimal balance, long transactionCount) { }
