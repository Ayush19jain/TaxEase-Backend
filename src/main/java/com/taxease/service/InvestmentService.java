package com.taxease.service;

import com.taxease.model.Investment;
import com.taxease.repository.InvestmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InvestmentService {
    
    @Autowired
    private InvestmentRepository investmentRepository;
    
    public List<Investment> getInvestments(String userId, String financialYear) {
        if (financialYear != null && !financialYear.isEmpty()) {
            return investmentRepository.findByUserIdAndFinancialYear(userId, financialYear);
        }
        return investmentRepository.findByUserId(userId);
    }
    
    public Map<String, Object> getInvestmentSummary(String userId, String financialYear) {
        List<Investment> investments = getInvestments(userId, financialYear);
        
        double totalInvested = investments.stream()
                .mapToDouble(Investment::getAmount)
                .sum();
        
        double section80C = investments.stream()
                .filter(inv -> inv.getSection() == Investment.TaxSection.SECTION_80C)
                .mapToDouble(Investment::getAmount)
                .sum();
        
        double section80CCD = investments.stream()
                .filter(inv -> inv.getSection() == Investment.TaxSection.SECTION_80CCD_1B)
                .mapToDouble(Investment::getAmount)
                .sum();
        
        double section80D = investments.stream()
                .filter(inv -> inv.getSection() == Investment.TaxSection.SECTION_80D)
                .mapToDouble(Investment::getAmount)
                .sum();
        
        Map<String, Double> byType = new HashMap<>();
        for (Investment inv : investments) {
            String type = inv.getType().name();
            byType.put(type, byType.getOrDefault(type, 0.0) + inv.getAmount());
        }
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalInvested", totalInvested);
        summary.put("section80C", section80C);
        summary.put("section80CCD", section80CCD);
        summary.put("section80D", section80D);
        summary.put("byType", byType);
        
        return summary;
    }
    
    @SuppressWarnings("null")
    public Investment createInvestment(Investment investment) {
        Investment saved = investmentRepository.save(investment);
        return saved;
    }
    
    @SuppressWarnings("null")
    public Investment updateInvestment(String id, Investment investmentDetails) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Investment not found"));
        
        investment.setType(investmentDetails.getType());
        investment.setAmount(investmentDetails.getAmount());
        investment.setSection(investmentDetails.getSection());
        investment.setReturns(investmentDetails.getReturns());
        investment.setDescription(investmentDetails.getDescription());
        investment.setFinancialYear(investmentDetails.getFinancialYear());
        
        return investmentRepository.save(investment);
    }
    
    @SuppressWarnings("null")
    public void deleteInvestment(String id) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Investment not found"));
        investmentRepository.delete(investment);
    }
}
