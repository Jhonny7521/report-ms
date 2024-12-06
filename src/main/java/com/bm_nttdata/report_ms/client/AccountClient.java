package com.bm_nttdata.report_ms.client;

import com.bm_nttdata.report_ms.dto.AccountDto;
import com.bm_nttdata.report_ms.model.DailyBalanceDto;
import java.time.LocalDate;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Cliente Feign para interactuar con el microservicio de cuentas bancarias.
 * Proporciona operaciones para verificar comisiones de transacciones y realizar
 * operaciones de depósito, retiro y transferencias entre cuentas bancarias.
 */
@FeignClient(name = "account-ms", url = "${account-service.url}")
public interface AccountClient {

    /**
     * getCustomerAccounts.
     *
     * @param customerId identificador únido de cliente
     * @return datos escencielaes de un cliente
     */
    @GetMapping("/accounts")
    List<AccountDto> getCustomerAccounts(@RequestParam(value = "customerId") String customerId);

    /**
     * getCustomerAccounts.
     *
     * @param id identificador único de la cuenta bancaria
     * @param searchMonth mes del cual se quiere obtener la informacion
     * @return lista de saldos diarios encontrados para el mes de busqueda
     */
    @GetMapping("/accounts/{id}/daily-balance")
    List<DailyBalanceDto> getAllDailyBalances(
            @PathVariable("id") String id,
            @RequestParam(value = "searchMonth")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate searchMonth);
}
