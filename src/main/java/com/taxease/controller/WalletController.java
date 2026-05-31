package com.taxease.controller;

import com.taxease.model.UserInvestment;
import com.taxease.service.WalletService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    // GET /api/wallet/{userId}?financialYear=...
    @GetMapping("/{userId}")
    public ResponseEntity<List<UserInvestment>> getWallet(@PathVariable String userId,
                                                          @RequestParam String financialYear) {
        List<UserInvestment> wallet = walletService.getWallet(userId, financialYear);
        return ResponseEntity.ok(wallet);
    }

    // GET /api/wallet/{userId}/summary?financialYear=...
    @GetMapping("/{userId}/summary")
    public ResponseEntity<Map<String, Object>> getWalletSummary(@PathVariable String userId,
                                                                @RequestParam String financialYear) {
        Map<String, Object> summary = walletService.getWalletSummary(userId, financialYear);
        return ResponseEntity.ok(summary);
    }

    // POST /api/wallet/add
    @PostMapping("/add")
    public ResponseEntity<UserInvestment> addInvestment(@RequestBody AddInvestmentRequest request) {
        UserInvestment section = walletService.addInvestment(
                request.getUserId(),
                request.getFinancialYear(),
                request.getSection(),
                request.getName(),
                request.getAmount()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(section);
    }

    // PUT /api/wallet/{sectionId}/slot/{slotId}
    @PutMapping("/{sectionId}/slot/{slotId}")
    public ResponseEntity<UserInvestment> updateInvestmentSlot(@PathVariable String sectionId,
                                                               @PathVariable String slotId,
                                                               @RequestBody UpdateSlotRequest request) {
        UserInvestment updated = walletService.updateInvestmentSlot(
                sectionId,
                slotId,
                request.getName(),
                request.getAmount()
        );
        return ResponseEntity.ok(updated);
    }

    // DELETE /api/wallet/{sectionId}/slot/{slotId}
    @DeleteMapping("/{sectionId}/slot/{slotId}")
    public ResponseEntity<Map<String, Object>> deleteInvestmentSlot(@PathVariable String sectionId,
                                                                    @PathVariable String slotId) {
        Map<String, Object> response = walletService.deleteInvestmentSlot(sectionId, slotId);
        return ResponseEntity.ok(response);
    }

    // POST /api/wallet/initialize
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initializeWallet(@RequestBody InitializeWalletRequest request) {
        List<UserInvestment> created = walletService.initializeWallet(
                request.getUserId(),
                request.getFinancialYear(),
                request.getSections()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Wallet initialized");
        response.put("sections", created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Data
    public static class AddInvestmentRequest {
        private String userId;
        private String financialYear;
        private String section;
        private String name;
        private Double amount;
    }

    @Data
    public static class UpdateSlotRequest {
        private String name;
        private Double amount;
    }

    @Data
    public static class InitializeWalletRequest {
        private String userId;
        private String financialYear;
        private List<String> sections;
    }
}
