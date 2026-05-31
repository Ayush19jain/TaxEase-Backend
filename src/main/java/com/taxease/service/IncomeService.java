package com.taxease.service;

import com.taxease.model.Income;
import com.taxease.repository.IncomeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncomeService {
    
    @Autowired
    private IncomeRepository incomeRepository;
    
    public List<Income> getIncome(String userId, String financialYear) {
        if (financialYear != null && !financialYear.isEmpty()) {
            return incomeRepository.findByUserIdAndFinancialYear(userId, financialYear);
        }
        return incomeRepository.findByUserId(userId);
    }
    
    public Income createIncome(Income income) {
        income.calculateTotalIncome();
        return incomeRepository.save(income);
    }
    
    @SuppressWarnings("null")
    public Income updateIncome(String id, Income incomeDetails) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Income record not found"));
        
        income.setSalary(incomeDetails.getSalary());
        income.setBusinessIncome(incomeDetails.getBusinessIncome());
        income.setCapitalGains(incomeDetails.getCapitalGains());
        income.setOtherIncome(incomeDetails.getOtherIncome());
        income.setFinancialYear(incomeDetails.getFinancialYear());
        income.calculateTotalIncome();
        
        return incomeRepository.save(income);
    }
    
    @SuppressWarnings("null")
    public void deleteIncome(String id) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Income record not found"));
        incomeRepository.delete(income);
    }
}
