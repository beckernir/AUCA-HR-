package com.auca_hr.AUCA_HR_System.services;

import com.opencsv.CSVWriter;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

@Service("CSV")
public class CSVReportGenerator implements ReportGenerator {

    @Override
    public byte[] generateReport(List<Map<String, Object>> data, String[] headers, String fileName) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(baos);
            CSVWriter writer = new CSVWriter(osw);

            // Write headers
            writer.writeNext(headers);

            // Write data
            for (Map<String, Object> row : data) {
                String[] values = new String[headers.length];
                for (int i = 0; i < headers.length; i++) {
                    Object value = row.get(headers[i]);
                    values[i] = value != null ? value.toString() : "";
                }
                writer.writeNext(values);
            }

            writer.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating CSV report", e);
        }
    }

    @Override
    public String getContentType() {
        return "text/csv";
    }

    @Override
    public String getFileExtension() {
        return ".csv";
    }
}


