//package com.auca_hr.AUCA_HR_System.services;
//
//
//import com.itextpdf.text.Document;
//import com.itextpdf.text.Paragraph;
//import com.itextpdf.text.pdf.PdfDocument;
//import com.itextpdf.text.pdf.PdfWriter;
//import org.springframework.stereotype.Service;
//import java.io.ByteArrayOutputStream;
//import java.util.List;
//import java.util.Map;
//
//@Service("PDF")
//public class PDFReportGenerator implements ReportGenerator {
//
//    @Override
//    public byte[] generateReport(List<Map<String, Object>> data, String[] headers, String fileName) {
//        try {
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            PdfWriter writer = new PdfWriter();
//            PdfDocument pdf = new PdfDocument(writer);
//            Document document = new Document(pdf);
//
//            // Add title
//            document.add(new Paragraph(fileName).setFontSize(18).setBold());
//
//            // Create table
//            Table table = new Table(headers.length);
//
//            // Add headers
//            for (String header : headers) {
//                table.addCell(new Cell().add(new Paragraph(header).setBold()));
//            }
//
//            // Add data rows
//            for (Map<String, Object> row : data) {
//                for (String header : headers) {
//                    Object value = row.get(header);
//                    table.addCell(new Cell().add(new Paragraph(value != null ? value.toString() : "")));
//                }
//            }
//
//            document.add(table);
//            document.close();
//
//            return baos.toByteArray();
//        } catch (Exception e) {
//            throw new RuntimeException("Error generating PDF report", e);
//        }
//    }
//
//    @Override
//    public String getContentType() {
//        return "application/pdf";
//    }
//
//    @Override
//    public String getFileExtension() {
//        return ".pdf";
//    }
//}
//
