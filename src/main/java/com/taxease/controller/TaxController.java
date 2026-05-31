package com.taxease.controller;

import com.taxease.model.TaxReport;
import com.taxease.service.TaxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tax")
public class TaxController {
    
    @Autowired
    private TaxService taxService;
    
    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateTax(@RequestBody Map<String, Object> request) {
        Double income = ((Number) request.get("income")).doubleValue();
        String regime = (String) request.get("regime");
        Double deductions = request.get("deductions") != null ? 
                ((Number) request.get("deductions")).doubleValue() : 0.0;
        
        Map<String, Object> result = taxService.calculateTax(income, regime, deductions);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/report")
    public ResponseEntity<TaxReport> generateTaxReport(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String financialYear = request.get("financialYear");
        String regime = request.get("regime");
        
        TaxReport report = taxService.generateTaxReport(userId, financialYear, regime);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }
    
    @GetMapping("/reports/{userId}")
    public ResponseEntity<List<TaxReport>> getTaxReports(@PathVariable String userId) {
        List<TaxReport> reports = taxService.getTaxReports(userId);
        return ResponseEntity.ok(reports);
    }
    
    @GetMapping("/report/{reportId}/download")
    public ResponseEntity<byte[]> downloadTaxReport(@PathVariable String reportId) {
        byte[] pdfBytes = taxService.downloadTaxReport(reportId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "tax-report.pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
