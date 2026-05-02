package com.services.banking.dtos.request;

import com.services.banking.enums.LoanPurpose;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanEligibilityRequest {
    @NotNull(message = "customerId must not be null")
    private Integer customerId;
    @NotNull(message = "requestedAmount must not be null")
    @Positive(message = "requestedAmount must be positive")
    private BigDecimal requestedAmount;
    @NotNull(message = "termMonths must not be null")
    @Positive(message = "termMonths must be positive")
    private Integer termMonths;
    @NotNull(message = "monthlyIncome must not be null")
    @Positive(message = "monthlyIncome must be positive")
    private BigDecimal monthlyIncome;
    @NotNull(message = "monthlyDebt must not be null")
    @PositiveOrZero(message = "monthlyDebt must be zero or positive")
    private BigDecimal monthlyDebt;
    @NotNull(message = "purpose must not be null")
    private LoanPurpose purpose;

}
