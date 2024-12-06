package com.bm_nttdata.report_ms.service;

import com.bm_nttdata.report_ms.model.CommissionReportDto;
import com.bm_nttdata.report_ms.model.DailyBalanceReportDto;
import java.time.LocalDate;

/**
 * Servicios de generación de reportes bancarios.
 * Maneja el procesamiento de cuentas bancarias, créditos y tarjetas de crédito
 * para generar reportes consolidados de saldos diarios.
 */
public interface ReportService {

    /**
     * Genera un reporte de balance diario para un cliente específico en un mes determinado.
     * El reporte incluye información detallada sobre todas las cuentas, créditos y tarjetas
     * de crédito del cliente.
     *
     * @param clientId Identificador único del cliente
     * @param month Mes para el cual se generará el reporte
     * @return DailyBalanceReportDto Objeto que contiene el reporte completo de balances
     */
    DailyBalanceReportDto generateDailyBalanceReport(String clientId, LocalDate month);

//    /**
//     * generateCommissionReport.
//     *
//     * @param startDate rr
//     * @param endDate rr
//     * @return rr
//     */
//    CommissionReportDto generateCommissionReport(LocalDate startDate, LocalDate endDate);
}

