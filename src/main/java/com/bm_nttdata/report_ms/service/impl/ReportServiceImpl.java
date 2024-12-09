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
import com.bm_nttdata.report_ms.model.AccountFeeDto;
import com.bm_nttdata.report_ms.model.BankFeeReportDto;
import com.bm_nttdata.report_ms.model.BankFeeReportDtoAccountFees;
import com.bm_nttdata.report_ms.model.CreditBalanceDto;
import com.bm_nttdata.report_ms.model.CreditCardBalanceDto;
import com.bm_nttdata.report_ms.model.DailyBalanceDto;
import com.bm_nttdata.report_ms.model.DailyBalanceReportDto;
import com.bm_nttdata.report_ms.model.DailyBalanceReportDtoAccounts;
import com.bm_nttdata.report_ms.model.DailyBalanceReportDtoCredits;
import com.bm_nttdata.report_ms.model.FeeDetailDto;
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
     * Genera un reporte de comisiones bancarias dentro de un pediodo de tiempo.
     * El reporte incluye información detallada sobre las comisiones según los tipos de cuentas.
     *
     * @param startDate fecha de inicio de la busqueda
     * @param endDate fecha de fin de la busqueda
     * @return BankFeeReportDto Objeto que contiene el reporte completo de las comisiones bancarias
     */
    @Override
    public BankFeeReportDto getBankFeesReport(LocalDate startDate, LocalDate endDate) {
        try {
            List<AccountDto> accounts = accountClient.getActiveAccounts("ACTIVE");

            BankFeeReportDto report = new BankFeeReportDto();
            report.setStartDate(startDate);
            report.setEndDate(endDate);

            BankFeeReportDtoAccountFees accountFees = new BankFeeReportDtoAccountFees();
            accountFees.setSavings(
                    calculateAccountFees(
                        accounts, AccountFeeDto.AccountTypeEnum.SAVINGS, startDate, endDate));
            accountFees.setChecking(
                    calculateAccountFees(
                        accounts, AccountFeeDto.AccountTypeEnum.CHECKING, startDate, endDate));
            accountFees.setFixedTerm(
                    calculateAccountFees(
                        accounts, AccountFeeDto.AccountTypeEnum.FIXED_TERM, startDate, endDate));
            accountFees.setSavingsVip(
                    calculateAccountFees(
                        accounts, AccountFeeDto.AccountTypeEnum.SAVINGS_VIP, startDate, endDate));
            accountFees.setCheckingPyme(
                    calculateAccountFees(
                        accounts, AccountFeeDto.AccountTypeEnum.CHECKING_PYME, startDate, endDate));

            BigDecimal totalFeeAmount = getTotalFeeAmount(accountFees);
            int totalNumberOfFees = getTotalNumberOfFees(accountFees);

            report.setAccountFees(accountFees);
            report.setTotalFeesAmount(totalFeeAmount);
            report.setTotalFeesNumber(totalNumberOfFees);

            return report;
        } catch (Exception e) {
            log.error(
                    "Unexpected error while generating bank fees report: {}: " + e.getMessage());
            throw new ServiceException(
                    "Unexpected error while generating bank fees report: " + e.getMessage());
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

    /**
     * Calcula las comisiones de cuentas para una lista de cuentas filtrada por
     * tipo de cuenta y rango de fechas.
     *
     * @param accounts Lista de DTOs de cuentas a procesar
     * @param accountType El tipo de cuenta a filtrar (ej., SAVINGS, CHECKING)
     * @param startDate La fecha de inicio del período de cálculo de comisiones
     * @param endDate La fecha de fin del período de cálculo de comisiones
     * @return Lista de AccountFeeDto con detalles de comisiones para cada cuenta que coincida
     * @throws ServiceException Si ocurre un error inesperado durante el procesamiento
     */
    private List<AccountFeeDto> calculateAccountFees(
            List<AccountDto> accounts,
            AccountFeeDto.AccountTypeEnum accountType,
            LocalDate startDate,
            LocalDate endDate) {

        try {
            List<AccountFeeDto> accountFeeDtoList = accounts.stream()
                    .filter(account -> account.getAccountType().equals(accountType.getValue()))
                    .map(account -> {
                        AccountFeeDto accountFee = new AccountFeeDto();
                        accountFee.setAccountId(account.getId());
                        accountFee.setAccountType(accountType);
                        accountFee.setCustomerId(account.getCustomerId());

                        List<FeeDetailDto> feeList =
                                accountClient.getAllAccountFees(
                                        account.getId(), startDate, endDate);

                        BigDecimal totalFees = feeList.stream()
                                .map(FeeDetailDto::getFeeAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        log.info("totalFee: " + totalFees);
                        accountFee.feeDetails(feeList);
                        accountFee.setNumberOfFees(feeList.size());
                        accountFee.setTotalFees(totalFees);

                        return accountFee;
                    })
                    .filter(accountFeeDto -> !accountFeeDto.getFeeDetails().isEmpty())
                    .collect(Collectors.toList());

            return accountFeeDtoList;
        } catch (Exception e) {
            log.error(
                    "Unexpected error while getting account fees: {}: " + e.getMessage());
            throw new ServiceException(
                    "Unexpected error while getting account fees: " + e.getMessage());
        }
    }

    /**
     * Calcula el monto total de comisiones a través de todos los tipos de cuenta en el reporte.
     * Maneja diferentes tipos de cuenta incluyendo Ahorros, Corriente, Plazo Fijo, Ahorros VIP,
     * y cuentas Corriente PYME.
     *
     * @param accountFees El DTO que contiene listas de comisiones para diferentes tipos de cuenta
     * @return La suma total de las comisiones a través de todos los tipos de cuenta
     */
    private BigDecimal getTotalFeeAmount(BankFeeReportDtoAccountFees accountFees) {
        BigDecimal totalFeeAmount = BigDecimal.ZERO;
        totalFeeAmount = totalFeeAmount.add(
                accountFees.getSavings()
                        .isEmpty() ? BigDecimal.ZERO : getFeesSum(accountFees.getSavings()));
        totalFeeAmount = totalFeeAmount.add(
                accountFees.getChecking()
                        .isEmpty() ? BigDecimal.ZERO : getFeesSum(accountFees.getChecking()));
        totalFeeAmount = totalFeeAmount.add(
                accountFees.getFixedTerm()
                        .isEmpty() ? BigDecimal.ZERO : getFeesSum(accountFees.getFixedTerm()));
        totalFeeAmount = totalFeeAmount.add(
                accountFees.getSavingsVip()
                        .isEmpty() ? BigDecimal.ZERO : getFeesSum(accountFees.getSavingsVip()));
        totalFeeAmount = totalFeeAmount.add(
                accountFees.getCheckingPyme()
                        .isEmpty() ? BigDecimal.ZERO : getFeesSum(accountFees.getCheckingPyme()));
        return totalFeeAmount;
    }

    /**
     * Calcula la suma de todas las comisiones para una lista específica de comisiones de cuenta.
     * Este método aplana los detalles de comisiones de todas las cuentas y suma sus montos.
     *
     * @param accountFeeList Lista de AccountFeeDto que contiene información de comisiones
     * @return La suma total de todos los montos de comisiones como BigDecimal
     */
    private BigDecimal getFeesSum(List<AccountFeeDto> accountFeeList) {

        return accountFeeList.stream()
                .flatMap(accountFee -> accountFee.getFeeDetails().stream())
                .map(FeeDetailDto::getFeeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                ;
    }

    /**
     * Calcula el número total de comisiones a través de todos los tipos de cuenta en el reporte.
     * Similar a getTotalFeeAmount pero cuenta el número de ocurrencias de comisiones en lugar
     * de sumar montos.
     *
     * @param accountFees El DTO que contiene listas de comisiones para diferentes tipos de cuenta
     * @return El conteo total de todas las comisiones a través de todos los tipos de cuenta
     */
    private int getTotalNumberOfFees(BankFeeReportDtoAccountFees accountFees) {
        int totalFeesNumber = 0;
        totalFeesNumber +=
                accountFees.getSavings()
                        .isEmpty() ? 0 : getTotalFees(accountFees.getSavings());
        totalFeesNumber +=
                accountFees.getChecking()
                        .isEmpty() ? 0 : getTotalFees(accountFees.getChecking());
        totalFeesNumber +=
                accountFees.getFixedTerm()
                        .isEmpty() ? 0 : getTotalFees(accountFees.getFixedTerm());
        totalFeesNumber +=
                accountFees.getSavingsVip()
                        .isEmpty() ? 0 : getTotalFees(accountFees.getSavingsVip());
        totalFeesNumber +=
                accountFees.getCheckingPyme()
                        .isEmpty() ? 0 : getTotalFees(accountFees.getCheckingPyme());
        return totalFeesNumber;
    }

    /**
     * Calcula el número total de comisiones para una lista específica de comisiones de cuenta.
     * Este método suma el número de comisiones de cada cuenta en la lista.
     *
     * @param accountFeeList Lista de AccountFeeDto que contiene información de comisiones
     * @return El conteo total de comisiones para las cuentas dadas
     */
    private int getTotalFees(List<AccountFeeDto> accountFeeList) {

        return accountFeeList.stream()
                .map(AccountFeeDto::getNumberOfFees)
                .reduce(0, Integer::sum)
                ;
    }
}
