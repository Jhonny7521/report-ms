package com.bm_nttdata.report_ms.client;

import com.bm_nttdata.report_ms.dto.CustomerDto;
import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Cliente Feign para la comunicación con el microservicio de clientes.
 * Proporciona métodos para realizar operaciones relacionadas con la información de clientes
 * a través de llamadas HTTP REST.
 */
@FeignClient(name = "customer-ms", url = "${customer-service.url}")
public interface CustomerClient {

    /**
     * Obtiene la información de un cliente por su identificador.
     *
     * @param id Identificador único del cliente
     * @return DTO con la información del cliente
     * @throws FeignException cuando ocurre un error en la comunicación con el servicio
     */
    @GetMapping("/customers/{id}")
    CustomerDto getCustomerById(@PathVariable("id") String id);
}
