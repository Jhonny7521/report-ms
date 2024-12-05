package com.bm_nttdata.report_ms.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase DTO para representar los datos esenciales
 * de una cuenta bancaria obtenidos del microservicio de cuentas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {

    private String id;
    private String customerId;
    private String clientName;
    private String accountType;
    private String accountNumber;
    private BigDecimal balance;
    private int currentMovements;
    private String status;

}
