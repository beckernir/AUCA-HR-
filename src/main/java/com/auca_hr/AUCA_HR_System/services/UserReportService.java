package com.auca_hr.AUCA_HR_System.services;

import com.auca_hr.AUCA_HR_System.entities.User;
import com.auca_hr.AUCA_HR_System.repositories.UserRepository;
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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserReportService {

    @Autowired
    private UserRepository userRepository;

    private static final String[] HEADERS = {
            "ID", "Full Names", "Phone", "NID", "Position", "Degree", "Actions"
    };

    // Generate PDF report for all Users
    public byte[] generatePdfReport(String search, Integer orderColumn, String orderDirection) throws IOException {
        List<User> Users = getFilteredAndSortedUsers(search, orderColumn, orderDirection);
        return createPdfReport(Users, "Users Report");
    }

    // Generate CSV report for all Users
    public byte[] generateCsvReport(String search, Integer orderColumn, String orderDirection) throws IOException {
        List<User> Users = getFilteredAndSortedUsers(search, orderColumn, orderDirection);
        return createCsvReport(Users);
    }

    // Generate Excel report for all Users
    public byte[] generateExcelReport(String search, Integer orderColumn, String orderDirection) throws IOException {
        List<User> Users = getFilteredAndSortedUsers(search, orderColumn, orderDirection);
        return createExcelReport(Users, "Users Report");
    }

    // Generate PDF report for single User
    public byte[] generateSingleUserPdfReport(Long UserId) throws IOException {
        Optional<User> User = userRepository.findById(UserId);
        if (User.isPresent()) {
            List<User> Users = List.of(User.get());
            return createPdfReport(Users, "User Report - " + UserId);
        }
        throw new RuntimeException("User not found with ID: " + UserId);
    }

    // Generate CSV report for single User
    public byte[] generateSingleUserCsvReport(Long UserId) throws IOException {
        Optional<User> User = userRepository.findById(UserId);
        if (User.isPresent()) {
            List<User> Users = List.of(User.get());
            return createCsvReport(Users);
        }
        throw new RuntimeException("User not found with ID: " + UserId);
    }

    // Generate Excel report for single User
    public byte[] generateSingleUserExcelReport(Long userId) throws IOException {
        Optional<User> User = userRepository.findById(userId);
        if (User.isPresent()) {
            List<User> Users = List.of(User.get());
            return createExcelReport(Users, "User Report - " + userId);
        }
        throw new RuntimeException("User not found with ID: " + userId);
    }

    // Helper method to get filtered and sorted Users
    private List<User> getFilteredAndSortedUsers(String search, Integer orderColumn, String orderDirection) {
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            if (search == null || search.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String searchPattern = "%" + search.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            // Adjust these field names based on your User entity structure
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("fullNames")), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("phone")), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("nid")), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("position")), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("degree")), searchPattern));

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.unsorted();
        if (orderColumn != null && orderDirection != null) {
            String sortField = getSortField(orderColumn);
            sort = "desc".equalsIgnoreCase(orderDirection) ?
                    Sort.by(sortField).descending() :
                    Sort.by(sortField).ascending();
        }

        return userRepository.findAll(spec, sort);
    }

    // Map column index to field name
    private String getSortField(Integer columnIndex) {
        return switch (columnIndex) {
            case 0 -> "id";
            case 1 -> "fullNames";
            case 2 -> "phone";
            case 3 -> "nid";
            case 4 -> "position";
            case 5 -> "degree";
            default -> "id";
        };
    }

    // Create PDF report
    private byte[] createPdfReport(List<User> Users, String title) throws IOException {
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

            // Set column widths
            float[] columnWidths = {1f, 3f, 2f, 2f, 2f, 2f};
            table.setWidths(columnWidths);

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
            for (User User : Users) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(User.getId()), dataFont)));
                table.addCell(new PdfPCell(new Phrase(User.getFullNames() != null ? User.getFullNames() : "N/A", dataFont)));
                table.addCell(new PdfPCell(new Phrase(User.getPhoneNumber() != null ? User.getPhoneNumber() : "N/A", dataFont)));
                table.addCell(new PdfPCell(new Phrase(User.getNationality() != null ? User.getNationality() : "N/A", dataFont)));
                table.addCell(new PdfPCell(new Phrase(User.getWorkingPosition() != null ? User.getWorkingPosition() : "N/A", dataFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(User.getAcademicRank() != null ? User.getAcademicRank() : "N/A"), dataFont)));
            }

            document.add(table);

            // Add summary statistics
            document.add(new Paragraph("\n"));
            Font summaryFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            document.add(new Paragraph("Summary Statistics", summaryFont));

            Font statsFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            document.add(new Paragraph("Total Users: " + Users.size(), statsFont));

            // Count by position
            Map<String, Long> positionCounts = Users.stream()
                    .collect(Collectors.groupingBy(
                            w -> w.getWorkingPosition() != null ? w.getWorkingPosition() : "Unknown",
                            Collectors.counting()
                    ));

            document.add(new Paragraph("Users by Position:", statsFont));
            for (Map.Entry<String, Long> entry : positionCounts.entrySet()) {
                document.add(new Paragraph("  " + entry.getKey() + ": " + entry.getValue(), statsFont));
            }

//            // Count by degree
//            Map<String, Long> degreeCounts = Users.stream()
//                    .collect(Collectors.groupingBy(
//                            w -> w.getAcademicRank() != null ? w.getAcademicRank() : "Unknown",
//                            Collectors.counting()
//                    ));

//            document.add(new Paragraph("Users by Degree:", statsFont));
//            for (Map.Entry<String, Long> entry : degreeCounts.entrySet()) {
//                document.add(new Paragraph("  " + entry.getKey() + ": " + entry.getValue(), statsFont));
//            }

            document.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new IOException("Error creating PDF report", e);
        }
    }

    // Create CSV report
    private byte[] createCsvReport(List<User> users) throws IOException {
        try (StringWriter stringWriter = new StringWriter();
             CSVPrinter csvPrinter = new CSVPrinter(stringWriter, CSVFormat.DEFAULT.withHeader(HEADERS))) {

            for (User user : users) {
                csvPrinter.printRecord(
                        user.getId(),
                        user.getFullNames() != null ? user.getFullNames() : "N/A",
                        user.getPhoneNumber() != null ? user.getPhoneNumber() : "N/A",
                        user.getNationality() != null ? user.getNationality() : "N/A",
                        user.getWorkingPosition() != null ? user.getWorkingPosition() : "N/A",
                        user.getAcademicRank() != null ? user.getAcademicRank() : "N/A",
                        "N/A" // Actions column not applicable for CSV
                );
            }

            csvPrinter.flush();
            return stringWriter.toString().getBytes();
        }
    }

    // Create Excel report
    private byte[] createExcelReport(List<User> Users, String title) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Users Report");

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
            for (User User : Users) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(User.getId());
                row.createCell(1).setCellValue(User.getFullNames() != null ? User.getFullNames() : "N/A");
                row.createCell(2).setCellValue(User.getPhoneNumber() != null ? User.getPhoneNumber() : "N/A");
                row.createCell(3).setCellValue(User.getNationality() != null ? User.getNationality() : "N/A");
                row.createCell(4).setCellValue(User.getWorkingPosition() != null ? User.getWorkingPosition() : "N/A");
                row.createCell(5).setCellValue(String.valueOf(User.getAcademicRank() != null ? User.getAcademicRank() : "N/A"));
            }

            // Auto-size columns
            for (int i = 0; i < HEADERS.length - 1; i++) {
                sheet.autoSizeColumn(i);
            }

            // Add summary sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            Row summaryTitleRow = summarySheet.createRow(0);
            Cell summaryTitleCell = summaryTitleRow.createCell(0);
            summaryTitleCell.setCellValue("Users Summary");
            summaryTitleCell.setCellStyle(titleStyle);

            Row totalRow = summarySheet.createRow(2);
            totalRow.createCell(0).setCellValue("Total Users:");
            totalRow.createCell(1).setCellValue(Users.size());

            // Count by position
            Map<String, Long> positionCounts = Users.stream()
                    .collect(Collectors.groupingBy(
                            w -> w.getWorkingPosition() != null ? w.getWorkingPosition() : "Unknown",
                            Collectors.counting()
                    ));

            int summaryRowNum = 4;
            Row positionHeaderRow = summarySheet.createRow(summaryRowNum++);
            positionHeaderRow.createCell(0).setCellValue("Users by Position:");

            for (Map.Entry<String, Long> entry : positionCounts.entrySet()) {
                Row positionRow = summarySheet.createRow(summaryRowNum++);
                positionRow.createCell(0).setCellValue(entry.getKey() + ":");
                positionRow.createCell(1).setCellValue(entry.getValue());
            }

//            // Count by degree
//            Map<String, Long> degreeCounts = Users.stream()
//                    .collect(Collectors.groupingBy(
//                            w -> w.getAcademicRank() != null ? w.getAcademicRank() : "Unknown",
//                            Collectors.counting()
//                    ));

            summaryRowNum += 2;
            Row degreeHeaderRow = summarySheet.createRow(summaryRowNum++);
            degreeHeaderRow.createCell(0).setCellValue("Users by Degree:");

//            for (Map.Entry<String, Long> entry : degreeCounts.entrySet()) {
//                Row degreeRow = summarySheet.createRow(summaryRowNum++);
//                degreeRow.createCell(0).setCellValue(entry.getKey() + ":");
//                degreeRow.createCell(1).setCellValue(entry.getValue());
//            }

            // Auto-size summary columns
            summarySheet.autoSizeColumn(0);
            summarySheet.autoSizeColumn(1);

            workbook.write(baos);
            return baos.toByteArray();
        }
    }
}