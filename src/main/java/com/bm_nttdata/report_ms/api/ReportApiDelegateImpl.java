package com.bm_nttdata.report_ms.api;

import com.bm_nttdata.report_ms.model.CommissionReportDto;
import com.bm_nttdata.report_ms.model.DailyBalanceReportDto;
import com.bm_nttdata.report_ms.service.ReportService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Implementación de los métodos generados por OpenApi Generator.
 * Gestiona las operaciones definidas en el contrato OpenApi para el manejo de reportes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportApiDelegateImpl implements ReportApiDelegate {

    private final ReportService reportService;

    @Override
    @CircuitBreaker(name = "balanceReport", fallbackMethod = "getBalanceReportFallback")
    public ResponseEntity<DailyBalanceReportDto> getCustomerAverageBalances(
            String customerId, LocalDate month) {
        log.info("Getting average balances for client {} for month {}", customerId, month);
        DailyBalanceReportDto report = reportService.generateDailyBalanceReport(customerId, month);
        return ResponseEntity.ok(report);
    }

    // Fallback methods for circuit breaker
    private ResponseEntity<DailyBalanceReportDto> getBalanceReportFallback(
            String clientId, LocalDate month, Exception e) {
        log.error("Fallback for balance report. ClientId: {}, Month: {}, Error: {}",
                clientId, month, e.getMessage());
        return new ResponseEntity(
                "We are experiencing some errors. Please try again later", HttpStatus.OK);
    }

}
