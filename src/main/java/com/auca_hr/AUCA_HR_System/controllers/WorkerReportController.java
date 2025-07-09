package com.auca_hr.AUCA_HR_System.controllers;

import com.auca_hr.AUCA_HR_System.services.UserReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/workers/reports")
public class WorkerReportController {

    @Autowired
    private UserReportService workerReportService;

    // Generate PDF report for all workers
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> generatePdfReport(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer orderColumn,
            @RequestParam(required = false) String orderDirection) {
        try {
            byte[] pdfReport = workerReportService.generatePdfReport(search, orderColumn, orderDirection);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    "workers_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".pdf");

            return new ResponseEntity<>(pdfReport, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Generate CSV report for all workers
    @GetMapping("/csv")
    public ResponseEntity<byte[]> generateCsvReport(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer orderColumn,
            @RequestParam(required = false) String orderDirection) {
        try {
            byte[] csvReport = workerReportService.generateCsvReport(search, orderColumn, orderDirection);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment",
                    "workers_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".csv");

            return new ResponseEntity<>(csvReport, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Generate Excel report for all workers
    @GetMapping("/excel")
    public ResponseEntity<byte[]> generateExcelReport(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer orderColumn,
            @RequestParam(required = false) String orderDirection) {
        try {
            byte[] excelReport = workerReportService.generateExcelReport(search, orderColumn, orderDirection);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment",
                    "workers_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".xlsx");

            return new ResponseEntity<>(excelReport, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

//    // Generate PDF report for single worker
//    @GetMapping("/{workerId}/pdf")
//    public ResponseEntity<byte[]> generateSingleWorkerPdfReport(@PathVariable Long workerId) {
//        try {
//            byte[] pdfReport = workerReportService.generateSingleWorkerPdfReport(workerId);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_PDF);
//            headers.setContentDispositionFormData("attachment",
//                    "worker_" + workerId + "_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".pdf");
//
//            return new ResponseEntity<>(pdfReport, headers, HttpStatus.OK);
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    // Generate CSV report for single worker
//    @GetMapping("/{workerId}/csv")
//    public ResponseEntity<byte[]> generateSingleWorkerCsvReport(@PathVariable Long workerId) {
//        try {
//            byte[] csvReport = workerReportService.generateSingleWorkerCsvReport(workerId);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.parseMediaType("text/csv"));
//            headers.setContentDispositionFormData("attachment",
//                    "worker_" + workerId + "_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".csv");
//
//            return new ResponseEntity<>(csvReport, headers, HttpStatus.OK);
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    // Generate Excel report for single worker
//    @GetMapping("/{workerId}/excel")
//    public ResponseEntity<byte[]> generateSingleWorkerExcelReport(@PathVariable Long workerId) {
//        try {
//            byte[] excelReport = workerReportService.generateSingleWorkerExcelReport(workerId);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
//            headers.setContentDispositionFormData("attachment",
//                    "worker_" + workerId + "_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".xlsx");
//
//            return new ResponseEntity<>(excelReport, headers, HttpStatus.OK);
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
}