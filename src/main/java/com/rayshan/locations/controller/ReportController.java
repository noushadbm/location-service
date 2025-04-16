package com.rayshan.locations.controller;

import com.rayshan.locations.entity.Report;
import com.rayshan.locations.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<List<Report>> getAllReports() throws Exception {
        return ResponseEntity.ok(reportService.getAllReports());
    }

//    @GetMapping("/")
//    public ResponseEntity<List<Report>> getAllReports() {
//
//        List<Report> report = reportService.getAllReports();
//        return ResponseEntity.ok(report);
//    }

    @GetMapping("/{idShort}/{timestamp}")
    public ResponseEntity<Report> getReportById(
            @PathVariable String idShort,
            @PathVariable Integer timestamp) {

        Optional<Report> report = reportService.getReportById(idShort, timestamp);
        return report.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search/idShort/{idShort}")
    public ResponseEntity<List<Report>> getReportsByIdShort(@PathVariable String idShort) {
        List<Report> reports = reportService.getReportsByIdShort(idShort);
        if (reports.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/search/statusCode/{statusCode}")
    public ResponseEntity<List<Report>> getReportsByStatusCode(@PathVariable Integer statusCode) {
        List<Report> reports = reportService.getReportsByStatusCode(statusCode);
        if (reports.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/search/dateRange")
    public ResponseEntity<List<Report>> getReportsByDateRange(
            @RequestParam Long startDate,
            @RequestParam Long endDate) {

        List<Report> reports = reportService.getReportsByDateRange(startDate, endDate);
        if (reports.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reports);
    }

    @PostMapping
    public ResponseEntity<Report> createReport(@RequestBody Report report) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportService.saveReport(report));
    }

    @PutMapping("/{idShort}/{timestamp}")
    public ResponseEntity<Report> updateReport(
            @PathVariable String idShort,
            @PathVariable Integer timestamp,
            @RequestBody Report reportDetails) {

        Optional<Report> existingReport = reportService.getReportById(idShort, timestamp);
        if (existingReport.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Ensure we're updating the correct report (maintaining IDs)
        reportDetails.setIdShort(idShort);
        reportDetails.setTimestamp(timestamp);

        return ResponseEntity.ok(reportService.saveReport(reportDetails));
    }

    @DeleteMapping("/{idShort}/{timestamp}")
    public ResponseEntity<Void> deleteReport(
            @PathVariable String idShort,
            @PathVariable Integer timestamp) {

        Optional<Report> existingReport = reportService.getReportById(idShort, timestamp);
        if (existingReport.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        reportService.deleteReport(idShort, timestamp);
        return ResponseEntity.noContent().build();
    }
}
