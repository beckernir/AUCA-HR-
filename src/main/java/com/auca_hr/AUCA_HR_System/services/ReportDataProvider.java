package com.auca_hr.AUCA_HR_System.services;

import java.util.List;
import java.util.Map;

public interface ReportDataProvider {
    List<Map<String, Object>> getData(String reportType, Map<String, Object> parameters);
    String[] getHeaders(String reportType);
}

