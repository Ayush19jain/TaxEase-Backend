package com.taxease.repository;

import com.taxease.model.UserInvestment;
import com.taxease.model.UserInvestment.Section;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInvestmentRepository extends MongoRepository<UserInvestment, String> {

    List<UserInvestment> findByUserIdAndFinancialYear(String userId, String financialYear);

    List<UserInvestment> findByUserIdAndFinancialYearOrderBySectionAsc(String userId, String financialYear);

    Optional<UserInvestment> findByUserIdAndFinancialYearAndSection(String userId,
                                                                     String financialYear,
                                                                     Section section);
}
