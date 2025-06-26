//package com.auca_hr.AUCA_HR_System.services;
//
//import com.auca_hr.AUCA_HR_System.dtos.ReportRequest;
//import com.auca_hr.AUCA_HR_System.dtos.ReportResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Service;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//@Service
//public class ReportService {
//
//    @Autowired
//    private ReportDataProvider dataProvider;
//
//    @Autowired
//    @Qualifier("PDF")
//    private ReportGenerator pdfGenerator;
//
//    @Autowired
//    @Qualifier("EXCEL")
//    private ReportGenerator excelGenerator;
//
//    @Autowired
//    @Qualifier("CSV")
//    private ReportGenerator csvGenerator;
//
//    private final String REPORTS_DIRECTORY = "generated-reports/";
//
//    public ReportResponse generateReport(ReportRequest request) {
//        try {
//            // Create reports directory if it doesn't exist
//            File reportsDir = new File(REPORTS_DIRECTORY);
//            if (!reportsDir.exists()) {
//                reportsDir.mkdirs();
//            }
//
//            // Get data and headers
//            List<Map<String, Object>> data = dataProvider.getData(request.getReportType(), request.getParameters());
//            String[] headers = dataProvider.getHeaders(request.getReportType());
//
//            // Generate filename
//            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
//            String reportId = UUID.randomUUID().toString();
//
//            // Get appropriate generator
//            ReportGenerator generator = getReportGenerator(request.getFormat());
//            String fileName = request.getReportType() + "_" + timestamp + generator.getFileExtension();
//
//            // Generate report
//            byte[] reportBytes = generator.generateReport(data, headers, request.getReportType() + " Report");
//
//            // Save to file
//            File reportFile = new File(REPORTS_DIRECTORY + fileName);
//            try (FileOutputStream fos = new FileOutputStream(reportFile)) {
//                fos.write(reportBytes);
//            }
//
//            // Create response
//            ReportResponse response = new ReportResponse(reportId, fileName, "/api/reports/download/" + fileName);
//            response.setFileSizeBytes(reportBytes.length);
//            response.setContentType(generator.getContentType());
//
//            return response;
//        } catch (Exception e) {
//            throw new RuntimeException("Error generating report: " + e.getMessage(), e);
//        }
//    }
//
//    private ReportGenerator getReportGenerator(String format) {
//        switch (format.toUpperCase()) {
//            case "PDF":
//                return pdfGenerator;
//            case "EXCEL":
//            case "XLSX":
//                return excelGenerator;
//            case "CSV":
//                return csvGenerator;
//            default:
//                throw new IllegalArgumentException("Unsupported format: " + format);
//        }
//    }
//
//    public File getReportFile(String fileName) {
//        File file = new File(REPORTS_DIRECTORY + fileName);
//        if (!file.exists()) {
//            throw new RuntimeException("Report file not found: " + fileName);
//        }
//        return file;
//    }
//}