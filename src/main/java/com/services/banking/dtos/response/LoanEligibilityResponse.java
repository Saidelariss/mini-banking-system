package com.services.banking.dtos.response;

import com.services.banking.enums.CheckLoanEligibilityDecision;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class LoanEligibilityResponse {
    private Boolean eligible;
    private CheckLoanEligibilityDecision decision;
    private Integer riskScore;
    private BigDecimal maxEligibleAmount;
    private BigDecimal interestRate;
    private BigDecimal estimatedMonthlyPayment;
    private List<String> reasons;
}
