package com.auca_hr.AUCA_HR_System.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationDTO {

    private Long id;

    @NotBlank(message = "Institution name is required")
    @Size(min = 2, max = 150, message = "Institution name must be between 2 and 150 characters")
    private String institution;

    @NotBlank(message = "Department is required")
    @Size(min = 2, max = 100, message = "Department must be between 2 and 100 characters")
    private String department;

    @NotBlank(message = "Program is required")
    @Size(min = 2, max = 100, message = "Program must be between 2 and 100 characters")
    private String program;

    @NotBlank(message = "Period is required")
    @Size(max = 50, message = "Period must not exceed 50 characters")
    private String period;

}