package com.taxease.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "taxreports")
public class TaxReport {
    
    @Id
    private String id;
    
    private String userId;
    
    private String financialYear;
    
    private Double totalIncome;
    
    private Double totalDeductions = 0.0;
    
    private Double taxableIncome;
    
    private Double taxAmount;
    
    private String regime;
    
    private TaxBreakdown taxBreakdown;
    
    @DBRef
    private List<Investment> investments = new ArrayList<>();
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaxBreakdown {
        private Double income;
        private Double tax;
        private Double healthInsurance;
    }
}
