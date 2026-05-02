package com.services.banking.services;

import com.services.banking.dtos.request.LoanEligibilityRequest;
import com.services.banking.dtos.response.LoanEligibilityResponse;
import com.services.banking.enums.CheckLoanEligibilityDecision;
import com.services.banking.enums.LoanPurpose;
import com.services.banking.persistence.entities.CustomerEntity;
import com.services.banking.persistence.repositories.CustomerJpaRepository;
import com.services.banking.services.exceptions.FunctionalError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoansServiceImplTest {

    @Mock
    private CustomerJpaRepository customerJpaRepository;

    private LoansServiceImpl loansService;

    @BeforeEach
    void setUp() {
        loansService = new LoansServiceImpl(customerJpaRepository);
    }

    @Test
    void checkLoanEligibilityShouldApproveLowRiskRequest() {
        when(customerJpaRepository.findById(1)).thenReturn(Optional.of(new CustomerEntity()));

        LoanEligibilityResponse response = loansService.checkLoanEligibility(buildRequest(
                new BigDecimal("15000.00"),
                24,
                new BigDecimal("12000.00"),
                new BigDecimal("1000.00"),
                LoanPurpose.CAR
        ));

        assertAll(
                () -> assertTrue(response.getEligible()),
                () -> assertEquals(CheckLoanEligibilityDecision.APPROVED, response.getDecision()),
                () -> assertTrue(response.getRiskScore() >= 65),
                () -> assertNotNull(response.getEstimatedMonthlyPayment()),
                () -> assertNotNull(response.getMaxEligibleAmount()),
                () -> assertTrue(response.getReasons().isEmpty())
        );
    }

    @Test
    void checkLoanEligibilityShouldRejectWhenDebtRatioIsTooHigh() {
        when(customerJpaRepository.findById(1)).thenReturn(Optional.of(new CustomerEntity()));

        LoanEligibilityResponse response = loansService.checkLoanEligibility(buildRequest(
                new BigDecimal("50000.00"),
                12,
                new BigDecimal("5000.00"),
                new BigDecimal("1800.00"),
                LoanPurpose.PERSONAL
        ));

        assertAll(
                () -> assertFalse(response.getEligible()),
                () -> assertEquals(CheckLoanEligibilityDecision.REJECTED, response.getDecision()),
                () -> assertTrue(response.getReasons().contains("Debt ratio exceeds 40%"))
        );
    }

    @Test
    void checkLoanEligibilityShouldRejectWhenRequestedAmountExceedsLimit() {
        when(customerJpaRepository.findById(1)).thenReturn(Optional.of(new CustomerEntity()));

        LoanEligibilityResponse response = loansService.checkLoanEligibility(buildRequest(
                new BigDecimal("250000.00"),
                24,
                new BigDecimal("6000.00"),
                new BigDecimal("500.00"),
                LoanPurpose.BUSINESS
        ));

        assertAll(
                () -> assertFalse(response.getEligible()),
                () -> assertEquals(CheckLoanEligibilityDecision.REJECTED, response.getDecision()),
                () -> assertTrue(response.getReasons().contains("Requested amount exceeds allowed limit"))
        );
    }

    @Test
    void checkLoanEligibilityShouldThrowWhenCustomerDoesNotExist() {
        when(customerJpaRepository.findById(404)).thenReturn(Optional.empty());

        LoanEligibilityRequest request = buildRequest(
                new BigDecimal("15000.00"),
                24,
                new BigDecimal("12000.00"),
                new BigDecimal("1000.00"),
                LoanPurpose.CAR
        );
        request.setCustomerId(404);

        FunctionalError error = assertThrows(
                FunctionalError.class,
                () -> loansService.checkLoanEligibility(request)
        );

        assertEquals("customer with id 404 not found", error.getMessage());
    }

    private static LoanEligibilityRequest buildRequest(
            BigDecimal requestedAmount,
            Integer termMonths,
            BigDecimal monthlyIncome,
            BigDecimal monthlyDebt,
            LoanPurpose purpose
    ) {
        LoanEligibilityRequest request = new LoanEligibilityRequest();
        request.setCustomerId(1);
        request.setRequestedAmount(requestedAmount);
        request.setTermMonths(termMonths);
        request.setMonthlyIncome(monthlyIncome);
        request.setMonthlyDebt(monthlyDebt);
        request.setPurpose(purpose);
        return request;
    }
}
