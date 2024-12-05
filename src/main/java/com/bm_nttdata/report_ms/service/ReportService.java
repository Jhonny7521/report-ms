package com.bm_nttdata.report_ms.service;

import com.bm_nttdata.report_ms.model.CommissionReportDto;
import com.bm_nttdata.report_ms.model.DailyBalanceReportDto;

import java.time.LocalDate;

/**
 * java doc.
 */
public interface ReportService {

    /**
     * generateDailyBalanceReport.
     *
     * @param clientId rr
     * @param month rr
     * @return rr
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

