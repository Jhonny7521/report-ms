package com.bm_nttdata.report_ms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Clase DTO para representar los datos de comisiones
 * de una cuenta bancaria obtenidos del microservicio de cuentas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionDto {
    private String id;
    private String productId;
    private LocalDate date;
    private String type;
    private BigDecimal amount;
    private String description;
}
