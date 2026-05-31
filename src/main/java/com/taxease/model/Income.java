package com.taxease.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "incomes")
public class Income {
    
    @Id
    private String id;
    
    private String userId;
    
    private String financialYear;
    
    private Double salary = 0.0;
    
    private Double businessIncome = 0.0;
    
    private Double capitalGains = 0.0;
    
    private Double otherIncome = 0.0;
    
    private Double totalIncome;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    public void calculateTotalIncome() {
        this.totalIncome = (salary != null ? salary : 0) + 
                          (businessIncome != null ? businessIncome : 0) + 
                          (capitalGains != null ? capitalGains : 0) + 
                          (otherIncome != null ? otherIncome : 0);
    }
}
