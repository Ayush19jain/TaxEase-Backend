package com.taxease.repository;

import com.taxease.model.TaxReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaxReportRepository extends MongoRepository<TaxReport, String> {
    List<TaxReport> findByUserId(String userId);
    List<TaxReport> findByUserIdOrderByCreatedAtDesc(String userId);
}
