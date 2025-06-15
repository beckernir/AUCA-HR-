package com.auca_hr.AUCA_HR_System.dtos;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
