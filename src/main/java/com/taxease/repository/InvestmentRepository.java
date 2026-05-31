package com.taxease.repository;

import com.taxease.model.Investment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvestmentRepository extends MongoRepository<Investment, String> {
    List<Investment> findByUserId(String userId);
    List<Investment> findByUserIdAndFinancialYear(String userId, String financialYear);
}
