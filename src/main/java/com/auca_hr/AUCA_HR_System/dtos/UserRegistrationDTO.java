package com.auca_hr.AUCA_HR_System.dtos;

import com.auca_hr.AUCA_HR_System.enums.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class UserRegistrationDTO {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullNames;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Gender gender;

    private AcademicRank academicRank;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "National ID is required")
    private String nationalId;

    @NotBlank(message = "Nationality is required")
    @Size(min = 2, max = 50, message = "Nationality must be between 2 and 50 characters")
    private String nationality;

    @NotNull(message = "Contract type is required")
    private ContractType contractType;

    private MaritalStatus maritalStatus;
    private Religion religion;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private String photo;

    @NotBlank(message = "Working position is required")
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

    private List<WorkExperienceDTO> WorkExperienceDTO;

    private List<EducationDTO> education ;

    @NotNull(message = "User role is required")
    private UserRole role;

    public @NotBlank(message = "Full name is required") @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters") String getFullNames() {
        return fullNames;
    }

    public void setFullNames(@NotBlank(message = "Full name is required") @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters") String fullNames) {
        this.fullNames = fullNames;
    }

    public @NotBlank(message = "Phone number is required") String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(@NotBlank(message = "Phone number is required") String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public @NotNull(message = "Date of birth is required") @Past(message = "Date of birth must be in the past") LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(@NotNull(message = "Date of birth is required") @Past(message = "Date of birth must be in the past") LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public @NotNull(message = "Gender is required") Gender getGender() {
        return gender;
    }

    public void setGender(@NotNull(message = "Gender is required") Gender gender) {
        this.gender = gender;
    }

    public AcademicRank getAcademicRank() {
        return academicRank;
    }

    public void setAcademicRank(AcademicRank academicRank) {
        this.academicRank = academicRank;
    }

    public @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email) {
        this.email = email;
    }

    public @NotBlank(message = "National ID is required") String getNationalId() {
        return nationalId;
    }

    public void setNationalId(@NotBlank(message = "National ID is required") String nationalId) {
        this.nationalId = nationalId;
    }

    public @NotBlank(message = "Nationality is required") @Size(min = 2, max = 50, message = "Nationality must be between 2 and 50 characters") String getNationality() {
        return nationality;
    }

    public void setNationality(@NotBlank(message = "Nationality is required") @Size(min = 2, max = 50, message = "Nationality must be between 2 and 50 characters") String nationality) {
        this.nationality = nationality;
    }

    public @NotNull(message = "Contract type is required") ContractType getContractType() {
        return contractType;
    }

    public void setContractType(@NotNull(message = "Contract type is required") ContractType contractType) {
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

    public @Size(min = 8, message = "Password must be at least 8 characters") String getPassword() {
        return password;
    }

    public void setPassword(@Size(min = 8, message = "Password must be at least 8 characters") String password) {
        this.password = password;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public @NotBlank(message = "Working position is required") @Size(min = 2, max = 100, message = "Working position must be between 2 and 100 characters") String getWorkingPosition() {
        return workingPosition;
    }

    public void setWorkingPosition(@NotBlank(message = "Working position is required") @Size(min = 2, max = 100, message = "Working position must be between 2 and 100 characters") String workingPosition) {
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

    public @NotNull(message = "User role is required") UserRole getRole() {
        return role;
    }

    public void setRole(@NotNull(message = "User role is required") UserRole role) {
        this.role = role;
    }

    public List<WorkExperienceDTO> getWorkExperienceDTO() {
        return WorkExperienceDTO;
    }

    public void setWorkExperienceDTO(List<WorkExperienceDTO> workExperienceDTO) {
        WorkExperienceDTO = workExperienceDTO;
    }

    public List<EducationDTO> getEducation() {
        return education;
    }

    public void setEducation(List<EducationDTO> education) {
        this.education = education;
    }
}