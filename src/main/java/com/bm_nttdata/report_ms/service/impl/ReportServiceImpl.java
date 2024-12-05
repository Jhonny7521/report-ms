package com.bm_nttdata.report_ms.service.impl;

import com.bm_nttdata.report_ms.client.AccountClient;
import com.bm_nttdata.report_ms.client.CreditClient;
import com.bm_nttdata.report_ms.client.CustomerClient;
import com.bm_nttdata.report_ms.dto.AccountDto;
import com.bm_nttdata.report_ms.dto.CreditDto;
import com.bm_nttdata.report_ms.dto.CustomerDto;
import com.bm_nttdata.report_ms.exception.BusinessRuleException;
import com.bm_nttdata.report_ms.exception.ServiceException;
import com.bm_nttdata.report_ms.model.*;
import com.bm_nttdata.report_ms.service.ReportService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * ReportServiceImpl.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final CustomerClient customerClient;
    private final AccountClient accountClient;
    private final CreditClient creditClient;
//    private final TransactionClient transactionClient;

    /**
     * generateDailyBalanceReport.
     *
     * @param clientId rr
     * @param month rr
     * @return rr
     */
    @Override
    public DailyBalanceReportDto generateDailyBalanceReport(String clientId, LocalDate month) {
        // Get customer data
        CustomerDto customer = customerClient.getCustomerById(clientId);

        // Get all accounts and credits
        List<AccountDto> accounts = accountClient.getCustomerAccounts(clientId);
        List<CreditDto> credits = creditClient.getCustomerCredits(clientId);
        /*List<CreditCardDto> creditCards = creditClient.getCustomerCreditCards(clientId);*/

        // Calculate daily balances for the entire month
        YearMonth yearMonth = YearMonth.from(month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // Initialize report
        DailyBalanceReportDto report = new DailyBalanceReportDto();
        report.setCustomerId(clientId);
        report.setCustomerName(customer.getName());
        report.setCustomerType(customer.getCustomerType());
        report.setMonth(month);

        log.info("Accounts: " + accounts);
        // Process accounts
        DailyBalanceReportDtoAccounts accountBalances = new DailyBalanceReportDtoAccounts();
        accountBalances.setSavings(
                calculateAccountBalances(
                        accounts, AccountBalanceDto.AccountTypeEnum.SAVINGS, month)); // put("savings", calculateAccountBalances(accounts, "SAVINGS", startDate, endDate, month));
        accountBalances.setChecking(
                calculateAccountBalances(
                        accounts, AccountBalanceDto.AccountTypeEnum.CHECKING, month));
        accountBalances.setFixedTerm(
                calculateAccountBalances(
                        accounts, AccountBalanceDto.AccountTypeEnum.FIXED_TERM, month));
        accountBalances.setSavingsVip(
                calculateAccountBalances(
                        accounts, AccountBalanceDto.AccountTypeEnum.SAVINGS_VIP, month));
        accountBalances.setCheckingPyme(
                calculateAccountBalances(
                        accounts, AccountBalanceDto.AccountTypeEnum.CHECKING_PYME, month));
        report.setAccounts(accountBalances);

        // Process credits
        DailyBalanceReportDtoCredits creditBalances = new DailyBalanceReportDtoCredits();
        creditBalances.setCredits(calculateCreditBalances(credits, "CREDIT", month));
        creditBalances.setCreditCards(calculateCreditBalances(credits, "CREDIT_CARD", month));

        report.setCredits(creditBalances);

        /*// Calculate total average
        double totalAverage = calculateTotalAverageBalance(accountBalances, creditBalances);
        report.setTotalAverageBalance(totalAverage);*/

        return report;
    }

    /**
     * calculateAccountBalances.
     *
     * @param accounts accounts
     * @param accountType rr
     * @param month rr
     * @return datos
     */
    private List<AccountBalanceDto> calculateAccountBalances(
            List<AccountDto> accounts, AccountBalanceDto.AccountTypeEnum accountType, LocalDate month) {
        try {
            List<AccountBalanceDto> accountBalanceDtoList = accounts.stream()
                    .filter(account -> account.getAccountType().equals(accountType.getValue()))
                    .map(account -> {
                        AccountBalanceDto balance = new AccountBalanceDto();
                        balance.setAccountId(account.getId());
                        balance.setAccountType(accountType);


                        List<DailyBalanceDto> dailyBalances =
                                accountClient.getAllDailyBalances(account.getId(), month);

                        BigDecimal totalBalance = dailyBalances.stream()
                                .map(DailyBalanceDto::getBalanceAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        int days = dailyBalances.size();
                        log.info("totalBalance: " + totalBalance + " - days: " + days);
                        balance.setDailyBalances(dailyBalances);
                        balance.setAverageBalance(totalBalance.divide(BigDecimal.valueOf(days)));

                        return balance;
                    })
                    .collect(Collectors.toList());

            return accountBalanceDtoList;
        } catch (Exception e) {
            log.error("Unexpected error: " + e.getMessage());
            throw new ServiceException("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * calculateCreditBalances.
     *
     * @param credits accounts
     * @param month rr
     * @return datos
     */
    private List<CreditBalanceDto> calculateCreditBalances(
            List<CreditDto> credits, String creditType, LocalDate month) {

        List<CreditBalanceDto> accountBalanceDtoList = credits.stream()
                .map(account -> {
                    CreditBalanceDto balance = new CreditBalanceDto();
                    balance.setCreditId(account.getId());
                    balance.setCreditType(CreditBalanceDto.CreditTypeEnum.valueOf(account.getCreditType()));

                    List<DailyBalanceDto> creditDailyBalances;

                    switch (creditType) {
                        case "CREDIT" -> {
                            creditDailyBalances =
                                    creditClient.getAllCreditDailyBalances(account.getId(), month);
                        }
                        case "CREDIT_CARD" -> {
                            creditDailyBalances =
                                    creditClient.getAllCreditCardDailyBalances(account.getId(), month);
                        }
                        default ->
                                throw new BusinessRuleException("Unknown product type");
                    }

                    BigDecimal totalBalance = creditDailyBalances.stream()
                            .map(DailyBalanceDto::getBalanceAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    int days = creditDailyBalances.size();
                    log.info("totalBalance: " + totalBalance + " - days: " + days);
                    balance.setDailyBalances(creditDailyBalances);
                    balance.setAverageBalance(totalBalance.divide(BigDecimal.valueOf(days)));

                    return balance;
                })
                .collect(Collectors.toList());

        return accountBalanceDtoList;
    }

}
