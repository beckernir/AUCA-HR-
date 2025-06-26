package com.auca_hr.AUCA_HR_System.services;

import org.springframework.stereotype.Service;
import java.util.*;
import java.time.LocalDateTime;

@Service
public class SampleReportDataProvider implements ReportDataProvider {

    @Override
    public List<Map<String, Object>> getData(String reportType, Map<String, Object> parameters) {
        List<Map<String, Object>> data = new ArrayList<>();

        switch (reportType.toLowerCase()) {
            case "sales":
                return generateSalesData();
            case "users":
                return generateUserData();
            case "inventory":
                return generateInventoryData();
            default:
                // Generic data structure
                return generateGenericData();
        }
    }

    @Override
    public String[] getHeaders(String reportType) {
        switch (reportType.toLowerCase()) {
            case "sales":
                return new String[]{"Order ID", "Customer", "Product", "Amount", "Date"};
            case "users":
                return new String[]{"User ID", "Name", "Email", "Registration Date", "Status"};
            case "inventory":
                return new String[]{"Product ID", "Name", "Category", "Stock", "Price"};
            default:
                return new String[]{"ID", "Name", "Value", "Date"};
        }
    }

    private List<Map<String, Object>> generateSalesData() {
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("Order ID", "ORD-" + String.format("%04d", i));
            row.put("Customer", "Customer " + i);
            row.put("Product", "Product " + (i % 10 + 1));
            row.put("Amount", 100.0 + (i * 15.5));
            row.put("Date", LocalDateTime.now().minusDays(i % 30));
            data.add(row);
        }
        return data;
    }

    private List<Map<String, Object>> generateUserData() {
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("User ID", "USR-" + String.format("%04d", i));
            row.put("Name", "User " + i);
            row.put("Email", "user" + i + "@example.com");
            row.put("Registration Date", LocalDateTime.now().minusDays(i * 5));
            row.put("Status", i % 3 == 0 ? "Inactive" : "Active");
            data.add(row);
        }
        return data;
    }

    private List<Map<String, Object>> generateInventoryData() {
        List<Map<String, Object>> data = new ArrayList<>();
        String[] categories = {"Electronics", "Clothing", "Books", "Home & Garden"};
        for (int i = 1; i <= 30; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("Product ID", "PRD-" + String.format("%04d", i));
            row.put("Name", "Product " + i);
            row.put("Category", categories[i % categories.length]);
            row.put("Stock", 10 + (i * 5));
            row.put("Price", 25.99 + (i * 2.5));
            data.add(row);
        }
        return data;
    }

    private List<Map<String, Object>> generateGenericData() {
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("ID", i);
            row.put("Name", "Item " + i);
            row.put("Value", i * 10.5);
            row.put("Date", LocalDateTime.now().minusDays(i));
            data.add(row);
        }
        return data;
    }
}