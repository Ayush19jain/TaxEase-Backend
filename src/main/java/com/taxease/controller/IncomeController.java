package com.taxease.controller;

import com.taxease.model.Income;
import com.taxease.service.IncomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/income")
public class IncomeController {
    
    @Autowired
    private IncomeService incomeService;
    
    @GetMapping("/{userId}")
    public ResponseEntity<List<Income>> getIncome(
            @PathVariable String userId,
            @RequestParam(required = false) String financialYear) {
        List<Income> incomes = incomeService.getIncome(userId, financialYear);
        return ResponseEntity.ok(incomes);
    }
    
    @PostMapping
    public ResponseEntity<Income> createIncome(@RequestBody Income income) {
        Income created = incomeService.createIncome(income);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Income> updateIncome(
            @PathVariable String id,
            @RequestBody Income income) {
        Income updated = incomeService.updateIncome(id, income);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteIncome(@PathVariable String id) {
        incomeService.deleteIncome(id);
        return ResponseEntity.ok(Map.of("message", "Income record deleted"));
    }
}
