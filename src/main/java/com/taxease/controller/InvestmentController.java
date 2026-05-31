package com.taxease.controller;

import com.taxease.model.Investment;
import com.taxease.service.InvestmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/investments")
public class InvestmentController {
    
    @Autowired
    private InvestmentService investmentService;
    
    @GetMapping("/{userId}")
    public ResponseEntity<List<Investment>> getInvestments(
            @PathVariable String userId,
            @RequestParam(required = false) String financialYear) {
        List<Investment> investments = investmentService.getInvestments(userId, financialYear);
        return ResponseEntity.ok(investments);
    }
    
    @GetMapping("/{userId}/summary")
    public ResponseEntity<Map<String, Object>> getInvestmentSummary(
            @PathVariable String userId,
            @RequestParam(required = false) String financialYear) {
        Map<String, Object> summary = investmentService.getInvestmentSummary(userId, financialYear);
        return ResponseEntity.ok(summary);
    }
    
    @PostMapping
    public ResponseEntity<Investment> createInvestment(@RequestBody Investment investment) {
        Investment created = investmentService.createInvestment(investment);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Investment> updateInvestment(
            @PathVariable String id,
            @RequestBody Investment investment) {
        Investment updated = investmentService.updateInvestment(id, investment);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteInvestment(@PathVariable String id) {
        investmentService.deleteInvestment(id);
        return ResponseEntity.ok(Map.of("message", "Investment deleted"));
    }
}
