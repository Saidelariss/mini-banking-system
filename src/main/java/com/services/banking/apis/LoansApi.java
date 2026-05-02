package com.services.banking.apis;

import com.services.banking.dtos.request.LoanEligibilityRequest;
import com.services.banking.dtos.response.LoanEligibilityResponse;
import com.services.banking.services.LoansService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/loans")
@AllArgsConstructor
public class LoansApi {
    private final LoansService loansService;

    @PostMapping("/eligibility")
    LoanEligibilityResponse checkLoanEligibility(@RequestBody @Valid LoanEligibilityRequest loanEligibilityRequest) {
        return loansService.checkLoanEligibility(loanEligibilityRequest);
    }

}
