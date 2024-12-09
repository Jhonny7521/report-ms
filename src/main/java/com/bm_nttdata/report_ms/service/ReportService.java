package com.bm_nttdata.report_ms.service;

import com.bm_nttdata.report_ms.model.BankFeeReportDto;
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

    /**
     * Genera un reporte de comisiones bancarias dentro de un pediodo de tiempo.
     * El reporte incluye información detallada sobre las comisiones según los tipos de cuentas.
     *
     * @param startDate fecha de inicio de la busqueda
     * @param endDate fecha de fin de la busqueda
     * @return BankFeeReportDto Objeto que contiene el reporte completo de las comisiones bancarias
     */
    BankFeeReportDto getBankFeesReport(LocalDate startDate, LocalDate endDate);
}

