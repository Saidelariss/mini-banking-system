package com.services.banking.services;

import com.services.banking.dtos.request.LoanEligibilityRequest;
import com.services.banking.dtos.response.LoanEligibilityResponse;
import com.services.banking.enums.CheckLoanEligibilityDecision;
import com.services.banking.enums.LoanPurpose;
import com.services.banking.persistence.repositories.CustomerJpaRepository;
import com.services.banking.services.exceptions.FunctionalError;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class LoansServiceImpl implements LoansService {
    private static final BigDecimal MIN_AMOUNT = BigDecimal.valueOf(1_000);
    private static final BigDecimal MIN_MONTHLY_INCOME = BigDecimal.valueOf(2_000);
    private static final BigDecimal MAX_DEBT_RATIO = new BigDecimal("0.40");
    private static final int MIN_TERM_MONTHS = 6;
    private static final int MAX_TERM_MONTHS = 84;
    private static final int REVIEW_RISK_SCORE_THRESHOLD = 65;

    private final CustomerJpaRepository customerJpaRepository;

    @Override
    public LoanEligibilityResponse checkLoanEligibility(LoanEligibilityRequest request) {
        customerJpaRepository.findById(request.getCustomerId()).orElseThrow(
                () -> new FunctionalError("customer with id " + request.getCustomerId() + " not found")
        );

        List<String> reasons = new ArrayList<>();
        BigDecimal debtCapacity = calculateDebtCapacity(request);
        BigDecimal maxEligibleAmount = request.getMonthlyIncome()
                .multiply(BigDecimal.valueOf(12))
                .min(debtCapacity)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        int riskScore = calculateRiskScore(request, maxEligibleAmount);
        BigDecimal interestRate = calculateInterestRate(riskScore);
        BigDecimal estimatedMonthlyPayment = calculateEstimatedMonthlyPayment(
                request.getRequestedAmount(),
                request.getTermMonths(),
                interestRate
        );
        BigDecimal debtRatio = calculateDebtRatio(request.getMonthlyDebt(), estimatedMonthlyPayment, request.getMonthlyIncome());

        if (request.getRequestedAmount().compareTo(MIN_AMOUNT) < 0) {
            reasons.add("Requested amount must be at least " + MIN_AMOUNT + " MAD");
        }

        if (request.getTermMonths() < MIN_TERM_MONTHS || request.getTermMonths() > MAX_TERM_MONTHS) {
            reasons.add("Loan duration must be between " + MIN_TERM_MONTHS + " and " + MAX_TERM_MONTHS + " months");
        }

        if (request.getMonthlyIncome().compareTo(MIN_MONTHLY_INCOME) < 0) {
            reasons.add("Monthly income must be at least " + MIN_MONTHLY_INCOME + " MAD");
        }

        if (request.getMonthlyDebt().compareTo(request.getMonthlyIncome()) >= 0) {
            reasons.add("Monthly debt cannot be greater than or equal to monthly income");
        }

        if (request.getRequestedAmount().compareTo(maxEligibleAmount) > 0) {
            reasons.add("Requested amount exceeds allowed limit");
        }

        if (debtRatio.compareTo(MAX_DEBT_RATIO) > 0) {
            reasons.add("Debt ratio exceeds 40%");
        }

        CheckLoanEligibilityDecision decision = resolveDecision(reasons, riskScore);
        if (decision == CheckLoanEligibilityDecision.PENDING) {
            reasons.add("Application requires manual review because risk score is below " + REVIEW_RISK_SCORE_THRESHOLD);
        }

        LoanEligibilityResponse response = new LoanEligibilityResponse();
        response.setEligible(decision != CheckLoanEligibilityDecision.REJECTED);
        response.setDecision(decision);
        response.setRiskScore(riskScore);
        response.setMaxEligibleAmount(maxEligibleAmount);
        response.setInterestRate(toPercentage(interestRate));
        response.setEstimatedMonthlyPayment(estimatedMonthlyPayment);
        response.setReasons(reasons);
        return response;
    }

    private BigDecimal calculateDebtCapacity(LoanEligibilityRequest request) {
        BigDecimal availableMonthlyCapacity = request.getMonthlyIncome()
                .multiply(MAX_DEBT_RATIO)
                .subtract(request.getMonthlyDebt());

        return availableMonthlyCapacity.multiply(BigDecimal.valueOf(request.getTermMonths()));
    }

    private BigDecimal calculateEstimatedMonthlyPayment(BigDecimal amount, Integer termMonths, BigDecimal annualInterestRate) {
        BigDecimal years = BigDecimal.valueOf(termMonths)
                .divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);
        BigDecimal totalInterestMultiplier = BigDecimal.ONE.add(annualInterestRate.multiply(years));

        return amount.multiply(totalInterestMultiplier)
                .divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDebtRatio(BigDecimal monthlyDebt, BigDecimal estimatedMonthlyPayment, BigDecimal monthlyIncome) {
        return monthlyDebt.add(estimatedMonthlyPayment)
                .divide(monthlyIncome, 4, RoundingMode.HALF_UP);
    }

    private int calculateRiskScore(LoanEligibilityRequest request, BigDecimal maxEligibleAmount) {
        int score = 100;
        BigDecimal requestedAmountRatio = request.getRequestedAmount()
                .divide(maxEligibleAmount.max(BigDecimal.ONE), 4, RoundingMode.HALF_UP);
        BigDecimal existingDebtRatio = request.getMonthlyDebt()
                .divide(request.getMonthlyIncome(), 4, RoundingMode.HALF_UP);

        if (requestedAmountRatio.compareTo(new BigDecimal("0.80")) > 0) {
            score -= 20;
        } else if (requestedAmountRatio.compareTo(new BigDecimal("0.50")) > 0) {
            score -= 10;
        }

        if (existingDebtRatio.compareTo(new BigDecimal("0.35")) > 0) {
            score -= 25;
        } else if (existingDebtRatio.compareTo(new BigDecimal("0.25")) > 0) {
            score -= 12;
        }

        if (request.getTermMonths() > 60) {
            score -= 8;
        } else if (request.getTermMonths() > 36) {
            score -= 4;
        }

        score -= purposeRiskPenalty(request.getPurpose());
        return Math.max(score, 0);
    }

    private int purposeRiskPenalty(LoanPurpose purpose) {
        return switch (purpose) {
            case HOME -> 0;
            case EDUCATION -> 2;
            case CAR -> 4;
            case PERSONAL -> 5;
            case BUSINESS -> 8;
        };
    }

    private BigDecimal calculateInterestRate(int riskScore) {
        if (riskScore >= 85) {
            return new BigDecimal("0.0500");
        }
        if (riskScore >= 70) {
            return new BigDecimal("0.0750");
        }
        return new BigDecimal("0.1050");
    }

    private BigDecimal toPercentage(BigDecimal rate) {
        return rate.multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private CheckLoanEligibilityDecision resolveDecision(List<String> rejectionReasons, int riskScore) {
        if (!rejectionReasons.isEmpty()) {
            return CheckLoanEligibilityDecision.REJECTED;
        }
        if (riskScore < REVIEW_RISK_SCORE_THRESHOLD) {
            return CheckLoanEligibilityDecision.PENDING;
        }
        return CheckLoanEligibilityDecision.APPROVED;
    }
}
