package com.taxease.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "investments")
public class Investment {
    
    @Id
    private String id;
    
    private String userId;
    
    private String financialYear;
    
    private InvestmentType type;
    
    private Double amount;
    
    private TaxSection section = TaxSection.SECTION_80C;
    
    private Double returns = 0.0;
    
    private String description;
    
    private LocalDate dateInvested = LocalDate.now();
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    public enum InvestmentType {
        ELSS, PPF, NPS, SIP, FD, LIC, EPF, NSC, Other
    }
    
    public enum TaxSection {
        SECTION_80C("80C"),
        SECTION_80CCD_1B("80CCD(1B)"),
        SECTION_80D("80D"),
        OTHER("Other");
        
        private final String value;
        
        TaxSection(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
}
