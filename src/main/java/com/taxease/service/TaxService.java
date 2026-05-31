package com.taxease.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.taxease.model.Income;
import com.taxease.model.Investment;
import com.taxease.model.TaxReport;
import com.taxease.model.User;
import com.taxease.repository.IncomeRepository;
import com.taxease.repository.InvestmentRepository;
import com.taxease.repository.TaxReportRepository;
import com.taxease.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaxService {
    
    @Autowired
    private TaxReportRepository taxReportRepository;
    
    @Autowired
    private IncomeRepository incomeRepository;
    
    @Autowired
    private InvestmentRepository investmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public Map<String, Object> calculateTax(Double income, String regime, Double deductions) {
        double taxableIncome = income - (deductions != null ? deductions : 0);
        double taxAmount = calculateTaxAmount(taxableIncome, regime);
        
        Map<String, Object> result = new HashMap<>();
        result.put("income", income);
        result.put("deductions", deductions != null ? deductions : 0);
        result.put("taxableIncome", taxableIncome);
        result.put("taxAmount", taxAmount);
        result.put("regime", regime);
        result.put("netIncome", income - taxAmount);
        
        return result;
    }
    
    private double calculateTaxAmount(double income, String regime) {
        if ("new".equalsIgnoreCase(regime)) {
            if (income <= 300000) return 0;
            if (income <= 600000) return (income - 300000) * 0.05;
            if (income <= 900000) return 15000 + (income - 600000) * 0.10;
            if (income <= 1200000) return 45000 + (income - 900000) * 0.15;
            if (income <= 1500000) return 90000 + (income - 1200000) * 0.20;
            return 150000 + (income - 1500000) * 0.30;
        } else {
            if (income <= 250000) return 0;
            if (income <= 500000) return (income - 250000) * 0.05;
            if (income <= 1000000) return 12500 + (income - 500000) * 0.20;
            return 112500 + (income - 1000000) * 0.30;
        }
    }
    
    public TaxReport generateTaxReport(String userId, String financialYear, String regime) {
        // Get income data (assumes at least one income record for the given user/year)
        List<Income> incomes = incomeRepository.findByUserIdAndFinancialYear(userId, financialYear);
        if (incomes.isEmpty()) {
            throw new RuntimeException("Income data not found");
        }
        Income incomeData = incomes.get(0);
        
        // Get investments
        List<Investment> investments = investmentRepository.findByUserIdAndFinancialYear(userId, financialYear);
        double totalDeductions = investments.stream()
                .mapToDouble(Investment::getAmount)
                .sum();
        totalDeductions = Math.min(totalDeductions, 150000);
        
        // Calculate tax
        double taxableIncome = incomeData.getTotalIncome() - totalDeductions;
        double taxAmount = calculateTaxAmount(taxableIncome, regime);
        
        // Create report
        TaxReport report = new TaxReport();
        report.setUserId(userId);
        report.setFinancialYear(financialYear);
        report.setTotalIncome(incomeData.getTotalIncome());
        report.setTotalDeductions(totalDeductions);
        report.setTaxableIncome(taxableIncome);
        report.setTaxAmount(taxAmount);
        report.setRegime(regime);
        
        TaxReport.TaxBreakdown breakdown = new TaxReport.TaxBreakdown();
        breakdown.setIncome(incomeData.getTotalIncome());
        breakdown.setTax(taxAmount);
        breakdown.setHealthInsurance(350.0);
        report.setTaxBreakdown(breakdown);
        
        report.setInvestments(investments);
        
        return taxReportRepository.save(report);
    }
    
    public List<TaxReport> getTaxReports(String userId) {
        return taxReportRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    @SuppressWarnings("null")
    public byte[] downloadTaxReport(String reportId) {
        TaxReport report = taxReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        
        User user = userRepository.findById(report.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            // Title
            document.add(new Paragraph("Tax Report")
                    .setFontSize(24)
                    .setBold());
            
            // User details
            document.add(new Paragraph("Financial Year: " + report.getFinancialYear()));
            document.add(new Paragraph("Taxpayer: " + user.getName()));
            if (user.getPan() != null) {
                document.add(new Paragraph("PAN: " + user.getPan()));
            }
            
            // Income details
            document.add(new Paragraph("\nIncome Details").setFontSize(16).setBold());
            document.add(new Paragraph("Total Income: ₹" + String.format("%,.2f", report.getTotalIncome())));
            document.add(new Paragraph("Total Deductions: ₹" + String.format("%,.2f", report.getTotalDeductions())));
            document.add(new Paragraph("Taxable Income: ₹" + String.format("%,.2f", report.getTaxableIncome())));
            
            // Tax calculation
            document.add(new Paragraph("\nTax Calculation").setFontSize(16).setBold());
            document.add(new Paragraph("Tax Regime: " + (report.getRegime().equals("new") ? "New Regime" : "Old Regime")));
            document.add(new Paragraph("Tax Amount: ₹" + String.format("%,.2f", report.getTaxAmount())));
            document.add(new Paragraph("Net Income: ₹" + String.format("%,.2f", report.getTotalIncome() - report.getTaxAmount())));
            
            // Investments
            if (report.getInvestments() != null && !report.getInvestments().isEmpty()) {
                document.add(new Paragraph("\nInvestments").setFontSize(16).setBold());
                int i = 1;
                for (Investment inv : report.getInvestments()) {
                    document.add(new Paragraph(i + ". " + inv.getType() + " - ₹" + 
                            String.format("%,.2f", inv.getAmount()) + " (Section " + inv.getSection().getValue() + ")"));
                    i++;
                }
            }
            
            // Footer
            document.add(new Paragraph("\n\nThis is a system-generated report.")
                    .setFontSize(10));
            document.add(new Paragraph("TaxEase - Your Tax Planning Companion")
                    .setFontSize(10));
            
            document.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage());
        }
    }
}
