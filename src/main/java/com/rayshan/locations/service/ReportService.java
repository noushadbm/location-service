package com.rayshan.locations.service;

import com.rayshan.locations.entity.Report;
import com.rayshan.locations.entity.ReportId;
import com.rayshan.locations.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public Optional<Report> getReportById(String idShort, Integer timestamp) {
        return reportRepository.findById(new ReportId(idShort, timestamp));
    }

    public List<Report> getReportsByIdShort(String idShort) {
        return reportRepository.findByIdShort(idShort);
    }

    public List<Report> getReportsByStatusCode(Integer statusCode) {
        return reportRepository.findByStatusCode(statusCode);
    }

    public List<Report> getReportsByDateRange(Long startDate, Long endDate) {
        return reportRepository.findByDatePublishedBetween(startDate, endDate);
    }

    public Report saveReport(Report report) {
        return reportRepository.save(report);
    }

    public void deleteReport(String idShort, Integer timestamp) {
        reportRepository.deleteById(new ReportId(idShort, timestamp));
    }
}
