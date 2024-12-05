package com.bm_nttdata.report_ms.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase DTO para representar los datos esenciales
 * de una tarjeta de crédito obtenidos del microservicio de créditos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditCardDto {

    private String id;
    private String customerId;
    private String cardNumber;
    private String cardType;
    private BigDecimal creditLimit;
    private BigDecimal availableCredit;
    private String status;

}
