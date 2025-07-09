package com.auca_hr.AUCA_HR_System.controllers;

import com.auca_hr.AUCA_HR_System.services.LeaveReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/leave-reports")
@CrossOrigin(origins = "*")
public class LeaveReportController {

    @Autowired
    private LeaveReportService reportService;

    // Export all leaves to PDF
    @GetMapping("/pdf")
    public ResponseEntity<InputStreamResource> exportAllLeavesToPdf(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer orderColumn,
            @RequestParam(required = false) String orderDirection) throws IOException {

        byte[] pdfData = reportService.generatePdfReport(search, orderColumn, orderDirection);
        ByteArrayInputStream bis = new ByteArrayInputStream(pdfData);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=leaves_report_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    // Export all leaves to CSV
    @GetMapping("/csv")
    public ResponseEntity<InputStreamResource> exportAllLeavesToCsv(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer orderColumn,
            @RequestParam(required = false) String orderDirection) throws IOException {

        byte[] csvData = reportService.generateCsvReport(search, orderColumn, orderDirection);
        ByteArrayInputStream bis = new ByteArrayInputStream(csvData);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=leaves_report_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(bis));
    }

    // Export all leaves to Excel
    @GetMapping("/excel")
    public ResponseEntity<InputStreamResource> exportAllLeavesToExcel(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer orderColumn,
            @RequestParam(required = false) String orderDirection) throws IOException {

        byte[] excelData = reportService.generateExcelReport(search, orderColumn, orderDirection);
        ByteArrayInputStream bis = new ByteArrayInputStream(excelData);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=leaves_report_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(bis));
    }
}