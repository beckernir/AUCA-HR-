package com.auca_hr.AUCA_HR_System.dtos;

import com.auca_hr.AUCA_HR_System.enums.*;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class UserUpdateDTO {

    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullNames;

    private String phoneNumber;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private Gender gender;
    private AcademicRank academicRank;

    @Email(message = "Invalid email format")
    private String email;

    private String nationalId;

    @Size(min = 2, max = 50, message = "Nationality must be between 2 and 50 characters")
    private String nationality;

    private ContractType contractType;
    private MaritalStatus maritalStatus;
    private Religion religion;
    private String photo;

    @Size(min = 2, max = 100, message = "Working position must be between 2 and 100 characters")
    private String workingPosition;

    @DecimalMin(value = "0.0", message = "Salary must be positive")
    private BigDecimal salary;

    @DecimalMin(value = "0.0", message = "Total allowances must be positive")
    private BigDecimal totalAllowances;

    private String bankAccount;
    private String accountNumber;
    private TprLevel tprLevel;
    private String rssbNumber;
    private UserRole role;

    public @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters") String getFullNames() {
        return fullNames;
    }

    public void setFullNames(@Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters") String fullNames) {
        this.fullNames = fullNames;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public @Past(message = "Date of birth must be in the past") LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(@Past(message = "Date of birth must be in the past") LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public AcademicRank getAcademicRank() {
        return academicRank;
    }

    public void setAcademicRank(AcademicRank academicRank) {
        this.academicRank = academicRank;
    }

    public @Email(message = "Invalid email format") String getEmail() {
        return email;
    }

    public void setEmail(@Email(message = "Invalid email format") String email) {
        this.email = email;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public @Size(min = 2, max = 50, message = "Nationality must be between 2 and 50 characters") String getNationality() {
        return nationality;
    }

    public void setNationality(@Size(min = 2, max = 50, message = "Nationality must be between 2 and 50 characters") String nationality) {
        this.nationality = nationality;
    }

    public ContractType getContractType() {
        return contractType;
    }

    public void setContractType(ContractType contractType) {
        this.contractType = contractType;
    }

    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public Religion getReligion() {
        return religion;
    }

    public void setReligion(Religion religion) {
        this.religion = religion;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public @Size(min = 2, max = 100, message = "Working position must be between 2 and 100 characters") String getWorkingPosition() {
        return workingPosition;
    }

    public void setWorkingPosition(@Size(min = 2, max = 100, message = "Working position must be between 2 and 100 characters") String workingPosition) {
        this.workingPosition = workingPosition;
    }

    public @DecimalMin(value = "0.0", message = "Salary must be positive") BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(@DecimalMin(value = "0.0", message = "Salary must be positive") BigDecimal salary) {
        this.salary = salary;
    }

    public @DecimalMin(value = "0.0", message = "Total allowances must be positive") BigDecimal getTotalAllowances() {
        return totalAllowances;
    }

    public void setTotalAllowances(@DecimalMin(value = "0.0", message = "Total allowances must be positive") BigDecimal totalAllowances) {
        this.totalAllowances = totalAllowances;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public TprLevel getTprLevel() {
        return tprLevel;
    }

    public void setTprLevel(TprLevel tprLevel) {
        this.tprLevel = tprLevel;
    }

    public String getRssbNumber() {
        return rssbNumber;
    }

    public void setRssbNumber(String rssbNumber) {
        this.rssbNumber = rssbNumber;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}