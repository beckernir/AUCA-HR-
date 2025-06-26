//package com.auca_hr.AUCA_HR_System.services;
//
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.stereotype.Service;
//import java.io.ByteArrayOutputStream;
//import java.util.List;
//import java.util.Map;
//
//@Service("EXCEL")
//public class ExcelReportGenerator implements ReportGenerator {
//
//    @Override
//    public byte[] generateReport(List<Map<String, Object>> data, String[] headers, String fileName) {
//        try {
//            Workbook workbook = new XSSFWorkbook();
//            Sheet sheet = workbook.createSheet("Report");
//
//            // Create header style
//            CellStyle headerStyle = workbook.createCellStyle();
//            Font headerFont = workbook.createFont();
//            headerFont.setBold(true);
//            headerStyle.setFont(headerFont);
//
//            // Create header row
//            Row headerRow = sheet.createRow(0);
//            for (int i = 0; i < headers.length; i++) {
//                Cell cell = headerRow.createCell(i);
//                cell.setCellValue(headers[i]);
//                cell.setCellStyle(headerStyle);
//            }
//
//            // Create data rows
//            int rowIndex = 1;
//            for (Map<String, Object> rowData : data) {
//                Row row = sheet.createRow(rowIndex++);
//                for (int i = 0; i < headers.length; i++) {
//                    Cell cell = row.createCell(i);
//                    Object value = rowData.get(headers[i]);
//                    if (value != null) {
//                        if (value instanceof Number) {
//                            cell.setCellValue(((Number) value).doubleValue());
//                        } else {
//                            cell.setCellValue(value.toString());
//                        }
//                    }
//                }
//            }
//
//            // Auto-size columns
//            for (int i = 0; i < headers.length; i++) {
//                sheet.autoSizeColumn(i);
//            }
//
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            workbook.write(baos);
//            workbook.close();
//
//            return baos.toByteArray();
//        } catch (Exception e) {
//            throw new RuntimeException("Error generating Excel report", e);
//        }
//    }
//
//    @Override
//    public String getContentType() {
//        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
//    }
//
//    @Override
//    public String getFileExtension() {
//        return ".xlsx";
//    }
//}
