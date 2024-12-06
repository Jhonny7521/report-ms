package com.bm_nttdata.report_ms.service.impl;

import com.bm_nttdata.report_ms.client.AccountClient;
import com.bm_nttdata.report_ms.client.CreditClient;
import com.bm_nttdata.report_ms.client.CustomerClient;
import com.bm_nttdata.report_ms.dto.AccountDto;
import com.bm_nttdata.report_ms.dto.CreditCardDto;
import com.bm_nttdata.report_ms.dto.CreditDto;
import com.bm_nttdata.report_ms.dto.CustomerDto;
import com.bm_nttdata.report_ms.exception.ServiceException;
import com.bm_nttdata.report_ms.model.AccountBalanceDto;
import com.bm_nttdata.report_ms.model.CreditBalanceDto;
import com.bm_nttdata.report_ms.model.CreditCardBalanceDto;
import com.bm_nttdata.report_ms.model.DailyBalanceDto;
import com.bm_nttdata.report_ms.model.DailyBalanceReportDto;
import com.bm_nttdata.report_ms.model.DailyBalanceReportDtoAccounts;
import com.bm_nttdata.report_ms.model.DailyBalanceReportDtoCredits;
import com.bm_nttdata.report_ms.service.ReportService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementación de los servicios de generación de reportes bancarios.
 * Maneja el procesamiento de cuentas bancarias, créditos y tarjetas de crédito
 * para generar reportes consolidados de saldos diarios.
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
     * Genera un reporte de balance diario para un cliente específico en un mes determinado.
     * El reporte incluye información detallada sobre todas las cuentas, créditos y tarjetas
     * de crédito del cliente.
     *
     * @param clientId Identificador único del cliente
     * @param month Mes para el cual se generará el reporte
     * @return DailyBalanceReportDto Objeto que contiene el reporte completo de balances
     * @throws ServiceException Si ocurre algún error durante la generación del reporte
     */
    @Override
    public DailyBalanceReportDto generateDailyBalanceReport(String clientId, LocalDate month) {

        try {
            CustomerDto customer = customerClient.getCustomerById(clientId);
            List<AccountDto> accounts = accountClient.getCustomerAccounts(clientId);
            List<CreditDto> credits = creditClient.getCustomerCredits(clientId);
            List<CreditCardDto> creditCards = creditClient.getCustomerCreditCards(clientId);
            log.info("Customer: " + customer);
            log.info("Accounts: " + accounts);
            log.info("Credits: " + credits);
            log.info("CreditCards: " + creditCards);

            DailyBalanceReportDto report = new DailyBalanceReportDto();
            report.setCustomerId(clientId);
            report.setCustomerName(customer.getName());
            report.setCustomerType(customer.getCustomerType());
            report.setMonth(month);


            DailyBalanceReportDtoAccounts accountBalances = new DailyBalanceReportDtoAccounts();
            accountBalances.setSavings(
                    calculateAccountBalances(
                            accounts, AccountBalanceDto.AccountTypeEnum.SAVINGS, month));
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
            creditBalances.setCredits(calculateCreditBalances(credits, month));
            creditBalances.setCreditCards(calculateCreditCardBalances(creditCards, month));

            report.setCredits(creditBalances);

            return report;
        } catch (Exception e) {
            log.error(
                    "Unexpected error while generating balance report: {}: " + e.getMessage());
            throw new ServiceException(
                    "Unexpected error while generating balance report: " + e.getMessage());
        }
    }

    /**
     * Calcula los saldos diarios para un tipo específico de cuenta bancaria.
     * Procesa todas las cuentas del cliente del tipo especificado y calcula
     * el saldo promedio diario para el período.
     *
     * @param accounts Lista de cuentas del cliente
     * @param accountType Tipo de cuenta a procesar
     * @param month Mes para el cual se calculan los balances
     * @return Lista de saldos promedio diario para cada cuenta del tipo especificado
     * @throws ServiceException Si ocurre un error durante el cálculo de los saldos
     */
    private List<AccountBalanceDto> calculateAccountBalances(
            List<AccountDto> accounts,
            AccountBalanceDto.AccountTypeEnum accountType,
            LocalDate month) {

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
            log.error(
                    "Unexpected error while getting daily account balances: {}: " + e.getMessage());
            throw new ServiceException(
                    "Unexpected error while getting daily account balances: " + e.getMessage());
        }
    }

    /**
     * Calcula los saldos diarios para los créditos del cliente.
     * Procesa todos los créditos y calcula el saldo promedio diario
     * junto con otra información relevante del crédito.
     *
     * @param credits Lista de créditos del cliente
     * @param month Mes para el cual se calculan los balances
     * @return Lista de saldos promedio diario para cada crédito
     * @throws ServiceException Si ocurre un error durante el cálculo de los saldos
     */
    private List<CreditBalanceDto> calculateCreditBalances(
            List<CreditDto> credits, LocalDate month) {

        try {
            List<CreditBalanceDto> creditBalanceDtoList = credits.stream()
                    .map(credit -> {
                        CreditBalanceDto balance = new CreditBalanceDto();
                        balance.setCreditId(credit.getId());
                        balance.setCreditType(credit.getCreditType());
                        balance.setTotalCreditAmount(credit.getAmount());
                        balance.setCreditOutstandingBalance(credit.getBalance());

                        List<DailyBalanceDto> creditDailyBalances =
                                creditClient.getAllCreditDailyBalances(credit.getId(), month);

                        BigDecimal totalBalance = creditDailyBalances.stream()
                                .map(DailyBalanceDto::getBalanceAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        int days = creditDailyBalances.size();
                        log.info("totalBalance: " + totalBalance + " - days: " + days);
                        balance.setDailyBalances(creditDailyBalances);
                        balance.setAverageDailyBalance(
                                totalBalance.divide(BigDecimal.valueOf(days)));

                        return balance;
                    })
                    .collect(Collectors.toList());

            return creditBalanceDtoList;
        } catch (Exception e) {
            log.error(
                    "Unexpected error while getting daily credit balances: {}", e.getMessage());
            throw new ServiceException(
                    "Unexpected error while getting daily credit balances" + e.getMessage());
        }
    }

    /**
     * Calcula los saldos diarios para las tarjetas de crédito del cliente.
     * Procesa todas las tarjetas de crédito y calcula el saldo promedio diario,
     * incluyendo información sobre límites de crédito y crédito disponible.
     *
     * @param creditCards Lista de tarjetas de crédito del cliente
     * @param month Mes para el cual se calculan los saldos
     * @return Lista de saldos promedio diario para cada tarjeta de crédito
     * @throws ServiceException Si ocurre un error durante el cálculo de los saldos
     */
    private List<CreditCardBalanceDto> calculateCreditCardBalances(
            List<CreditCardDto> creditCards, LocalDate month) {
        try {
            List<CreditCardBalanceDto> creditCardBalanceDtoList = creditCards.stream()
                    .map(creditCard -> {
                        CreditCardBalanceDto balance = new CreditCardBalanceDto();
                        balance.setCreditCardId(creditCard.getId());
                        balance.setCreditCardType(creditCard.getCardType());
                        balance.setCardNumber(creditCard.getCardNumber());
                        balance.setCreditCardLimit(creditCard.getCreditLimit());
                        balance.setAvailableCredit(creditCard.getAvailableCredit());

                        List<DailyBalanceDto> cardDailyBalances =
                                creditClient.getAllCreditCardDailyBalances(
                                        creditCard.getId(), month);

                        BigDecimal totalBalance = cardDailyBalances.stream()
                                .map(DailyBalanceDto::getBalanceAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        int days = cardDailyBalances.size();
                        log.info("totalBalance: " + totalBalance + " - days: " + days);
                        balance.setDailyBalances(cardDailyBalances);
                        balance.setAverageDailyBalance(
                                totalBalance.divide(BigDecimal.valueOf(days)));

                        return balance;
                    })
                    .collect(Collectors.toList());

            return creditCardBalanceDtoList;
        } catch (Exception e) {
            log.error(
                    "Unexpected error while getting daily credit card balances: {}",
                    e.getMessage());
            throw new ServiceException(
                    "Unexpected error while getting daily credit card balances" + e.getMessage());
        }
    }
}
