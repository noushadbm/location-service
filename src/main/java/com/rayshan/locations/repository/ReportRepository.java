package com.rayshan.locations.repository;

import com.rayshan.locations.entity.Report;
import com.rayshan.locations.entity.ReportId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, ReportId> {
    List<Report> findByIdShort(String idShort);
    List<Report> findByStatusCode(Integer statusCode);
    List<Report> findByDatePublishedBetween(Long startDate, Long endDate);
}
