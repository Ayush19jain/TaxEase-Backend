package com.taxease.service;

import com.taxease.model.UserInvestment;
import com.taxease.model.UserInvestment.InvestmentSlot;
import com.taxease.model.UserInvestment.Section;
import com.taxease.repository.UserInvestmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WalletService {

    private static final Map<Section, Double> SECTION_LIMITS = new EnumMap<>(Section.class);

    static {
        SECTION_LIMITS.put(Section.SECTION_80C, 150000.0);
        SECTION_LIMITS.put(Section.SECTION_80CCD_1B, 50000.0);
        SECTION_LIMITS.put(Section.SECTION_80D, 25000.0);
        SECTION_LIMITS.put(Section.SECTION_80DD, 75000.0);
        SECTION_LIMITS.put(Section.SECTION_80DDB, 40000.0);
        SECTION_LIMITS.put(Section.SECTION_80E, 0.0);     // No limit
        SECTION_LIMITS.put(Section.SECTION_80G, 0.0);     // No limit
        SECTION_LIMITS.put(Section.SECTION_80TTA, 10000.0);
    }

    @Autowired
    private UserInvestmentRepository userInvestmentRepository;

    public List<UserInvestment> getWallet(String userId, String financialYear) {
        if (financialYear == null || financialYear.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Financial year is required");
        }

        List<UserInvestment> sections = userInvestmentRepository
                .findByUserIdAndFinancialYearOrderBySectionAsc(userId, financialYear);
        sections.forEach(UserInvestment::recalculateInvested);
        return sections;
    }

    public UserInvestment addInvestment(String userId,
                                        String financialYear,
                                        String sectionValue,
                                        String name,
                                        Double amount) {
        if (userId == null || financialYear == null || sectionValue == null || name == null || amount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All fields are required");
        }
        if (amount <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than 0");
        }

        Section section;
        try {
            section = Section.fromValue(sectionValue);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        UserInvestment sectionDoc = userInvestmentRepository
                .findByUserIdAndFinancialYearAndSection(userId, financialYear, section)
                .orElse(null);

        double limit = SECTION_LIMITS.getOrDefault(section, 0.0);

        if (sectionDoc != null) {
            sectionDoc.recalculateInvested();
            double newInvested = (sectionDoc.getInvested() != null ? sectionDoc.getInvested() : 0.0) + amount;
            if (limit > 0 && newInvested > limit) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        String.format("Adding %.0f exceeds section limit of %.0f", amount, limit)
                );
            }
            if (sectionDoc.getSlots() == null) {
                sectionDoc.setSlots(new ArrayList<>());
            }
            sectionDoc.getSlots().add(InvestmentSlot.create(name, amount));
            sectionDoc.recalculateInvested();
            return userInvestmentRepository.save(sectionDoc);
        } else {
            if (limit > 0 && amount > limit) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        String.format("Amount %.0f exceeds section limit of %.0f", amount, limit)
                );
            }
            UserInvestment newSection = new UserInvestment();
            newSection.setUserId(userId);
            newSection.setFinancialYear(financialYear);
            newSection.setSection(section);
            newSection.setLimit(limit);
            List<InvestmentSlot> slots = new ArrayList<>();
            slots.add(InvestmentSlot.create(name, amount));
            newSection.setSlots(slots);
            newSection.recalculateInvested();
            return userInvestmentRepository.save(newSection);
        }
    }

    @SuppressWarnings("null")
    public UserInvestment updateInvestmentSlot(String sectionId,
                                               String slotId,
                                               String name,
                                               Double amount) {
        UserInvestment sectionDoc = userInvestmentRepository.findById(sectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));

        if (sectionDoc.getSlots() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Investment slot not found");
        }

        InvestmentSlot slot = sectionDoc.getSlots().stream()
                .filter(s -> slotId.equals(s.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Investment slot not found"));

        double oldAmount = slot.getAmount() != null ? slot.getAmount() : 0.0;
        double newAmount = amount != null ? amount : oldAmount;

        double limit = SECTION_LIMITS.getOrDefault(sectionDoc.getSection(), 0.0);
        sectionDoc.recalculateInvested();
        double currentInvested = sectionDoc.getInvested() != null ? sectionDoc.getInvested() : 0.0;
        double newInvested = currentInvested - oldAmount + newAmount;

        if (limit > 0 && newInvested > limit) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Update exceeds section limit of %.0f", limit)
            );
        }

        if (name != null && !name.isBlank()) {
            slot.setName(name);
        }
        slot.setAmount(newAmount);

        sectionDoc.recalculateInvested();
        return userInvestmentRepository.save(sectionDoc);
    }

    @SuppressWarnings("null")
    public Map<String, Object> deleteInvestmentSlot(String sectionId, String slotId) {
        UserInvestment sectionDoc = userInvestmentRepository.findById(sectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));

        if (sectionDoc.getSlots() == null || sectionDoc.getSlots().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Investment slot not found");
        }

        boolean removed = sectionDoc.getSlots().removeIf(s -> slotId.equals(s.getId()));
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Investment slot not found");
        }

        Map<String, Object> response = new HashMap<>();

        if (sectionDoc.getSlots().isEmpty()) {
            userInvestmentRepository.delete(sectionDoc);
            response.put("message", "Investment deleted and section removed");
        } else {
            sectionDoc.recalculateInvested();
            UserInvestment saved = userInvestmentRepository.save(sectionDoc);

            Map<String, Object> section = new HashMap<>();
            section.put("_id", saved.getId());
            section.put("section", saved.getSection().getValue());
            section.put("limit", saved.getLimit());
            section.put("invested", saved.getInvested());
            section.put("remaining", saved.getRemainingLimit());
            section.put("progress", saved.getProgressPercentage());
            section.put("slots", saved.getSlots());
            section.put("lastUpdated", saved.getLastUpdated());

            response.put("message", "Investment slot deleted");
            response.put("section", section);
        }

        return response;
    }

    public Map<String, Object> getWalletSummary(String userId, String financialYear) {
        if (financialYear == null || financialYear.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Financial year is required");
        }

        List<UserInvestment> walletData = userInvestmentRepository.findByUserIdAndFinancialYear(userId, financialYear);
        walletData.forEach(UserInvestment::recalculateInvested);

        double totalInvested = walletData.stream()
                .mapToDouble(ui -> ui.getInvested() != null ? ui.getInvested() : 0.0)
                .sum();

        double totalLimit = walletData.stream()
                .mapToDouble(ui -> ui.getLimit() != null ? ui.getLimit() : 0.0)
                .sum();

        double totalRemaining = walletData.stream()
                .mapToDouble(UserInvestment::getRemainingLimit)
                .sum();

        List<Map<String, Object>> bySections = new ArrayList<>();
        for (UserInvestment ui : walletData) {
            Map<String, Object> s = new HashMap<>();
            s.put("section", ui.getSection().getValue());
            s.put("invested", ui.getInvested());
            s.put("limit", ui.getLimit());
            s.put("remaining", ui.getRemainingLimit());
            s.put("progress", ui.getProgressPercentage());
            bySections.add(s);
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalInvested", totalInvested);
        summary.put("totalLimit", totalLimit);
        summary.put("totalRemaining", totalRemaining);
        summary.put("bySections", bySections);

        return summary;
    }

    public List<UserInvestment> initializeWallet(String userId,
                                                 String financialYear,
                                                 List<String> sectionValues) {
        if (userId == null || financialYear == null || sectionValues == null || sectionValues.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "userId, financialYear, and sections array are required");
        }

        List<UserInvestment> createdSections = new ArrayList<>();

        for (String value : sectionValues) {
            Section section;
            try {
                section = Section.fromValue(value);
            } catch (IllegalArgumentException ex) {
                continue; // Skip invalid sections silently
            }

            boolean exists = userInvestmentRepository
                    .findByUserIdAndFinancialYearAndSection(userId, financialYear, section)
                    .isPresent();

            if (!exists) {
                double limit = SECTION_LIMITS.getOrDefault(section, 0.0);
                UserInvestment ui = new UserInvestment();
                ui.setUserId(userId);
                ui.setFinancialYear(financialYear);
                ui.setSection(section);
                ui.setLimit(limit);
                ui.setSlots(new ArrayList<>());
                ui.recalculateInvested();
                createdSections.add(userInvestmentRepository.save(ui));
            }
        }

        return createdSections;
    }
}
