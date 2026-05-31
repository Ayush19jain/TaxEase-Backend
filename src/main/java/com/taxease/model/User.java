package com.taxease.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    
    @Id
    private String id;
    
    private String name;
    
    @Indexed(name = "email_1", unique = true)
    private String email;
    
    private String passwordHash;
    
    @Indexed(name = "pan_1", unique = true, sparse = true)
    private String pan;
    
    private String phoneNumber;
    
    private TaxRegime taxRegime = TaxRegime.NEW;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    public enum TaxRegime {
        NEW("new"),
        OLD("old");
        
        private final String value;
        
        TaxRegime(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
}
