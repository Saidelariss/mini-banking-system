package com.services.banking.services;

import com.services.banking.dtos.request.LoanEligibilityRequest;
import com.services.banking.dtos.response.LoanEligibilityResponse;

public interface LoansService {
    LoanEligibilityResponse checkLoanEligibility(LoanEligibilityRequest loanEligibilityRequest);
}
