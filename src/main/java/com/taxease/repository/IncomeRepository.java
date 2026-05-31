package com.taxease.repository;

import com.taxease.model.Income;
import org.springframework.data.mongodb.repository.MongoRepository;
    import org.springframework.stereotype.Repository;

    import java.util.List;@Repository
public interface IncomeRepository extends MongoRepository<Income, String> {
    List<Income> findByUserId(String userId);
    List<Income> findByUserIdAndFinancialYear(String userId, String financialYear);
}
