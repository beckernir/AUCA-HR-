package com.auca_hr.AUCA_HR_System.services;

import com.auca_hr.AUCA_HR_System.entities.LeaveRequest;
import com.auca_hr.AUCA_HR_System.enums.LeaveStatus;
import com.auca_hr.AUCA_HR_System.repositories.LeaveRequestRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LeaveReportService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    private static final String[] HEADERS = {
            "ID", "Lecturer Name", "Leave Type", "Start Date", "End Date", "Status","Actions"
    };

    // Generate PDF report for all leave requests
    public byte[] generatePdfReport(String search, Integer orderColumn, String orderDirection) throws IOException {
        List<LeaveRequest> leaveRequests = getFilteredAndSortedLeaveRequests(search, orderColumn, orderDirection);
        return createPdfReport(leaveRequests, "Leave Requests Report");
    }

    // Generate CSV report for all leave requests
    public byte[] generateCsvReport(String search, Integer orderColumn, String orderDirection) throws IOException {
        List<LeaveRequest> leaveRequests = getFilteredAndSortedLeaveRequests(search, orderColumn, orderDirection);
        return createCsvReport(leaveRequests);
    }

    // Generate Excel report for all leave requests
    public byte[] generateExcelReport(String search, Integer orderColumn, String orderDirection) throws IOException {
        List<LeaveRequest> leaveRequests = getFilteredAndSortedLeaveRequests(search, orderColumn, orderDirection);
        return createExcelReport(leaveRequests, "Leave Requests Report");
    }

    // Generate PDF report for single leave request
    public byte[] generateSingleLeaveRequestPdfReport(Long leaveRequestId) throws IOException {
        Optional<LeaveRequest> leaveRequest = leaveRequestRepository.findById(leaveRequestId);
        if (leaveRequest.isPresent()) {
            List<LeaveRequest> leaveRequests = List.of(leaveRequest.get());
            return createPdfReport(leaveRequests, "Leave Request Report - " + leaveRequestId);
        }
        throw new RuntimeException("Leave request not found with ID: " + leaveRequestId);
    }

    // Generate CSV report for single leave request
    public byte[] generateSingleLeaveRequestCsvReport(Long leaveRequestId) throws IOException {
        Optional<LeaveRequest> leaveRequest = leaveRequestRepository.findById(leaveRequestId);
        if (leaveRequest.isPresent()) {
            List<LeaveRequest> leaveRequests = List.of(leaveRequest.get());
            return createCsvReport(leaveRequests);
        }
        throw new RuntimeException("Leave request not found with ID: " + leaveRequestId);
    }

    // Generate Excel report for single leave request
    public byte[] generateSingleLeaveRequestExcelReport(Long leaveRequestId) throws IOException {
        Optional<LeaveRequest> leaveRequest = leaveRequestRepository.findById(leaveRequestId);
        if (leaveRequest.isPresent()) {
            List<LeaveRequest> leaveRequests = List.of(leaveRequest.get());
            return createExcelReport(leaveRequests, "Leave Request Report - " + leaveRequestId);
        }
        throw new RuntimeException("Leave request not found with ID: " + leaveRequestId);
    }

    // Helper method to get filtered and sorted leave requests
    private List<LeaveRequest> getFilteredAndSortedLeaveRequests(String search, Integer orderColumn, String orderDirection) {
        Specification<LeaveRequest> spec = (root, query, criteriaBuilder) -> {
            if (search == null || search.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String searchPattern = "%" + search.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            // Adjust these field names based on your LeaveRequest entity structure
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("lecturerName")), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("leaveType")), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("status")), searchPattern));

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.unsorted();
        if (orderColumn != null && orderDirection != null) {
            String sortField = getSortField(orderColumn);
            sort = "desc".equalsIgnoreCase(orderDirection) ?
                    Sort.by(sortField).descending() :
                    Sort.by(sortField).ascending();
        }

        return leaveRequestRepository.findAll(spec, sort);
    }

    // Map column index to field name
    private String getSortField(Integer columnIndex) {
        return switch (columnIndex) {
            case 0 -> "id";
            case 1 -> "lecturerName";
            case 2 -> "leaveType";
            case 3 -> "startDate";
            case 4 -> "endDate";
            case 5 -> "status";
            default -> "id";
        };
    }

    // Create PDF report
    private byte[] createPdfReport(List<LeaveRequest> leaveRequests, String title) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            // Add title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph titleParagraph = new Paragraph(title, titleFont);
            titleParagraph.setAlignment(Element.ALIGN_CENTER);
            titleParagraph.setSpacingAfter(20);
            document.add(titleParagraph);

            // Add generation date
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Paragraph dateParagraph = new Paragraph("Generated on: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), dateFont);
            dateParagraph.setAlignment(Element.ALIGN_RIGHT);
            dateParagraph.setSpacingAfter(20);
            document.add(dateParagraph);

            // Create table (excluding Actions column for PDF)
            PdfPTable table = new PdfPTable(HEADERS.length - 1);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            // Add headers (excluding Actions)
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            for (int i = 0; i < HEADERS.length - 1; i++) {
                PdfPCell cell = new PdfPCell(new Phrase(HEADERS[i], headerFont));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Add data rows
            Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            for (LeaveRequest leaveRequest : leaveRequests) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(leaveRequest.getId()), dataFont)));
                table.addCell(new PdfPCell(new Phrase(leaveRequest.getLecturer().getFullNames(), dataFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(leaveRequest.getLeaveType()), dataFont)));
                table.addCell(new PdfPCell(new Phrase(
                        leaveRequest.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), dataFont)));
                table.addCell(new PdfPCell(new Phrase(
                        leaveRequest.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), dataFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(leaveRequest.getStatus()), dataFont)));
            }

            document.add(table);

            // Add summary statistics
            document.add(new Paragraph("\n"));
            Font summaryFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            document.add(new Paragraph("Summary Statistics", summaryFont));

            Font statsFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            document.add(new Paragraph("Total Leave Requests: " + leaveRequests.size(), statsFont));

            // Count by status
            long pendingCount = leaveRequests.stream().filter(lr -> LeaveStatus.PENDING.equals(lr.getStatus())).count();
            long approvedCount = leaveRequests.stream().filter(lr -> LeaveStatus.APPROVED.equals(lr.getStatus())).count();
            long rejectedCount = leaveRequests.stream().filter(lr -> LeaveStatus.REJECTED.equals(lr.getStatus())).count();

            document.add(new Paragraph("Pending Requests: " + pendingCount, statsFont));
            document.add(new Paragraph("Approved Requests: " + approvedCount, statsFont));
            document.add(new Paragraph("Rejected Requests: " + rejectedCount, statsFont));

            document.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new IOException("Error creating PDF report", e);
        }
    }

    // Create CSV report
    private byte[] createCsvReport(List<LeaveRequest> leaveRequests) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             StringWriter stringWriter = new StringWriter();
             CSVPrinter csvPrinter = new CSVPrinter(stringWriter, CSVFormat.DEFAULT.withHeader(HEADERS))) {

            for (LeaveRequest leaveRequest : leaveRequests) {
                csvPrinter.printRecord(
                        leaveRequest.getId(),
                        leaveRequest.getLecturer().getFullNames(),
                        leaveRequest.getLeaveType(),
                        leaveRequest.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        leaveRequest.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        leaveRequest.getStatus(),
                        "N/A" // Actions column not applicable for CSV
                );
            }

            csvPrinter.flush();
            return stringWriter.toString().getBytes();
        }
    }

    // Create Excel report
    private byte[] createExcelReport(List<LeaveRequest> leaveRequests, String title) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Leave Requests Report");

            // Create title row
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(title);

            // Create title style
            CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            // Create date row
            Row dateRow = sheet.createRow(1);
            Cell dateCell = dateRow.createCell(0);
            dateCell.setCellValue("Generated on: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // Create header row
            Row headerRow = sheet.createRow(3);
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < HEADERS.length - 1; i++) { // Exclude Actions column
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Add data rows
            int rowNum = 4;
            for (LeaveRequest leaveRequest : leaveRequests) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(leaveRequest.getId());
                row.createCell(1).setCellValue(leaveRequest.getLecturer().getFullNames());
                row.createCell(2).setCellValue(leaveRequest.getLeaveType().ordinal());
                row.createCell(3).setCellValue(leaveRequest.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                row.createCell(4).setCellValue(leaveRequest.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                row.createCell(5).setCellValue(leaveRequest.getStatus().ordinal());
            }

            // Auto-size columns
            for (int i = 0; i < HEADERS.length - 1; i++) {
                sheet.autoSizeColumn(i);
            }

            // Add summary sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            Row summaryTitleRow = summarySheet.createRow(0);
            Cell summaryTitleCell = summaryTitleRow.createCell(0);
            summaryTitleCell.setCellValue("Leave Requests Summary");
            summaryTitleCell.setCellStyle(titleStyle);

            Row totalRow = summarySheet.createRow(2);
            totalRow.createCell(0).setCellValue("Total Leave Requests:");
            totalRow.createCell(1).setCellValue(leaveRequests.size());

            // Count by status
            long pendingCount = leaveRequests.stream().filter(lr -> LeaveStatus.PENDING.equals(lr.getStatus())).count();
            long approvedCount = leaveRequests.stream().filter(lr -> LeaveStatus.APPROVED.equals(lr.getStatus())).count();
            long rejectedCount = leaveRequests.stream().filter(lr -> LeaveStatus.REJECTED.equals(lr.getStatus())).count();

            Row pendingRow = summarySheet.createRow(3);
            pendingRow.createCell(0).setCellValue("Pending Requests:");
            pendingRow.createCell(1).setCellValue(pendingCount);

            Row approvedRow = summarySheet.createRow(4);
            approvedRow.createCell(0).setCellValue("Approved Requests:");
            approvedRow.createCell(1).setCellValue(approvedCount);

            Row rejectedRow = summarySheet.createRow(5);
            rejectedRow.createCell(0).setCellValue("Rejected Requests:");
            rejectedRow.createCell(1).setCellValue(rejectedCount);

            // Auto-size summary columns
            summarySheet.autoSizeColumn(0);
            summarySheet.autoSizeColumn(1);

            workbook.write(baos);
            return baos.toByteArray();
        }
    }
}