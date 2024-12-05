package com.bm_nttdata.report_ms.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase DTO para representar los datos esenciales
 * de un crédito obtenidos del microservicio de créditos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditDto {
    private String id;
    private String customerId;
    private String creditType;
    private BigDecimal amount;
    private BigDecimal balance;
    private String status;
}
