package com.taxease.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_investments")
public class UserInvestment {

    @Id
    @JsonProperty("_id")
    private String id;

    private String userId;

    private String financialYear;

    private Section section;

    private Double limit;

    private Double invested = 0.0;

    private List<InvestmentSlot> slots = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime lastUpdated;

    public enum Section {
        SECTION_80C("80C"),
        SECTION_80CCD_1B("80CCD(1B)"),
        SECTION_80D("80D"),
        SECTION_80DD("80DD"),
        SECTION_80DDB("80DDB"),
        SECTION_80E("80E"),
        SECTION_80G("80G"),
        SECTION_80TTA("80TTA");

        private final String value;

        Section(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Section fromValue(String value) {
            for (Section s : values()) {
                if (s.value.equalsIgnoreCase(value)) {
                    return s;
                }
            }
            throw new IllegalArgumentException("Invalid section: " + value);
        }
    }

    public void recalculateInvested() {
        this.invested = slots == null
                ? 0.0
                : slots.stream()
                        .mapToDouble(s -> s.getAmount() != null ? s.getAmount() : 0.0)
                        .sum();
    }

    @JsonProperty("remaining")
    public double getRemainingLimit() {
        double lim = limit != null ? limit : 0.0;
        double inv = invested != null ? invested : 0.0;
        return Math.max(0.0, lim - inv);
    }

    @JsonProperty("progress")
    public double getProgressPercentage() {
        double lim = limit != null ? limit : 0.0;
        double inv = invested != null ? invested : 0.0;
        return lim > 0 ? (inv / lim) * 100.0 : 0.0;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvestmentSlot {

        @JsonProperty("_id")
        private String id;

        private String name;

        private Double amount;

        private LocalDateTime dateAdded;

        public static InvestmentSlot create(String name, Double amount) {
            InvestmentSlot slot = new InvestmentSlot();
            slot.setId(UUID.randomUUID().toString());
            slot.setName(name);
            slot.setAmount(amount);
            slot.setDateAdded(LocalDateTime.now());
            return slot;
        }
    }
}
