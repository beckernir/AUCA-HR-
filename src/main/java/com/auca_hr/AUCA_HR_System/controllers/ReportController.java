//package com.auca_hr.AUCA_HR_System.controllers;
//
//import com.auca_hr.AUCA_HR_System.dtos.ReportRequest;
//import com.auca_hr.AUCA_HR_System.dtos.ReportResponse;
//import com.auca_hr.AUCA_HR_System.services.ReportService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.FileSystemResource;
//import org.springframework.core.io.Resource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import jakarta.validation.Valid;
//import java.io.File;
//import java.nio.file.Files;
//
//@RestController
//@RequestMapping("/api/reports")
//@CrossOrigin(origins = "*")
//public class ReportController {
//
//    @Autowired
//    private ReportService reportService;
//
//    @PostMapping("/generate")
//    public ResponseEntity<ReportResponse> generateReport(@Valid @RequestBody ReportRequest request) {
//        try {
//            ReportResponse response = reportService.generateReport(request);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//    @GetMapping("/download/{fileName}")
//    public ResponseEntity<Resource> downloadReport(@PathVariable String fileName) {
//        try {
//            File file = reportService.getReportFile(fileName);
//            Resource resource = new FileSystemResource(file);
//
//            String contentType = Files.probeContentType(file.toPath());
//            if (contentType == null) {
//                contentType = "application/octet-stream";
//            }
//
//            return ResponseEntity.ok()
//                    .contentType(MediaType.parseMediaType(contentType))
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
//                    .body(resource);
//        } catch (Exception e) {
//            return ResponseEntity.notFound().build();
//        }
//    }
//
//    @GetMapping("/types")
//    public ResponseEntity<String[]> getReportTypes() {
//        return ResponseEntity.ok(new String[]{"sales", "users", "inventory"});
//    }
//
//    @GetMapping("/formats")
//    public ResponseEntity<String[]> getSupportedFormats() {
//        return ResponseEntity.ok(new String[]{"PDF", "EXCEL", "CSV"});
//    }
//}