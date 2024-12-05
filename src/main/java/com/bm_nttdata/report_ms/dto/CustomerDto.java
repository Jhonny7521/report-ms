package com.bm_nttdata.report_ms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase DTO para representar los datos esenciales
 * de un cliente obtenidos del microservicio de clientes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {

    private String id;
    private String name;
    private String customerType;
    private String documentNumber;
}
