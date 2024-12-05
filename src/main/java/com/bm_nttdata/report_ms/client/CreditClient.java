package com.bm_nttdata.report_ms.client;

import com.bm_nttdata.report_ms.dto.CreditCardDto;
import com.bm_nttdata.report_ms.dto.CreditDto;
import com.bm_nttdata.report_ms.model.DailyBalanceDto;
import feign.FeignException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Cliente Feign para la comunicación con el microservicio de créditos.
 * Proporciona métodos para realizar operaciones relacionadas con creditos
 * y trajetas de créditos a través de llamadas HTTP REST.
 */
@FeignClient(name = "credit-ms", url = "${credit-service.url}")
public interface CreditClient {

    /**
     * Obtiene todos los créditos asociadas a un cliente específico.
     *
     * @param customerId identificador único del cliente cuyos créditos se desean consultar
     * @return lista de créditos asociadas al cliente
     * @throws FeignException cuando ocurre un error en la comunicación con el servicio
     */
    @GetMapping("/credits")
    List<CreditDto> getCustomerCredits(@RequestParam(value = "customerId") String customerId);

    /**
     * Obtiene todos los saldos diarios de un crédito.
     *
     * @param id identificador único del crédito
     * @param searchMonth mes del cual se quiere obtener la informacion
     * @return lista de saldos diarios encontrados para el mes de busqueda
     */
    @GetMapping("/credits/{id}/daily-balance")
    List<DailyBalanceDto> getAllCreditDailyBalances(
            @PathVariable("id") String id,
            @RequestParam(value = "searchMonth")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate searchMonth);

    /**
     * Obtiene todas las tarjetas de crédito asociadas a un cliente específico.
     *
     * @param customerId identificador único del cliente cuyas tarjetas se desean consultar
     * @return lista de tarjetas de crédito asociadas al cliente
     * @throws FeignException cuando ocurre un error en la comunicación con el servicio
     */
    @GetMapping("/credit-cards")
    List<CreditCardDto> getCustomerCreditCards(
            @RequestParam(value = "customerId") String customerId);

    /**
     * Obtiene todos los saldos diarios de un crédito.
     *
     * @param id identificador único del crédito
     * @param searchMonth mes del cual se quiere obtener la informacion
     * @return lista de saldos diarios encontrados para el mes de busqueda
     */
    @GetMapping("/credit-cards/{id}/daily-balance")
    List<DailyBalanceDto> getAllCreditCardDailyBalances(
            @PathVariable("id") String id,
            @RequestParam(value = "searchMonth")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate searchMonth);
}
