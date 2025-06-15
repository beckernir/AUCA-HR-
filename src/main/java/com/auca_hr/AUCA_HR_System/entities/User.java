package com.auca_hr.AUCA_HR_System.entities;

import com.auca_hr.AUCA_HR_System.enums.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users_auca", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "nationalId"),
        @UniqueConstraint(columnNames = "phoneNumber")
})
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String fullNames;

    @NotBlank(message = "Phone number is required")
    @Column(nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private AcademicRank academicRank;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "National ID is required")
    @Column(nullable = false, unique = true, length = 20)
    private String nationalId;

    @NotBlank(message = "Nationality is required")
    @Size(min = 2, max = 50, message = "Nationality must be between 2 and 50 characters")
    @Column(nullable = false, length = 50)
    private String nationality;

    @NotNull(message = "Contract type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractType contractType;

    @Enumerated(EnumType.STRING)
    private MaritalStatus maritalStatus;

    @Enumerated(EnumType.STRING)
    private Religion religion;

    @Size(min = 8, message = "Password must be at least 8 characters")
    @Column(nullable = false)
    private String password;

    @Lob
    private String photo;

    @NotBlank(message = "Working position is required")
    @Size(min = 2, max = 100, message = "Working position must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String workingPosition;

    @DecimalMin(value = "0.0", message = "Salary must be positive")
    @Digits(integer = 10, fraction = 2, message = "Invalid salary format")
    @Column(precision = 12, scale = 2)
    private BigDecimal salary;

    @DecimalMin(value = "0.0", message = "Total allowances must be positive")
    @Digits(integer = 10, fraction = 2, message = "Invalid allowances format")
    @Column(precision = 12, scale = 2)
    private BigDecimal totalAllowances;

    @Size(max = 100, message = "Bank account name too long")
    @Column(length = 100)
    private String bankAccount;

    @Column(length = 20)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private TprLevel tprLevel;

    @Column(length = 15)
    private String rssbNumber;

    @NotNull(message = "User role is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private Boolean active = true;

    // Relationships for work experience and education
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<WorkExperience> workExperience = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Education> education = new ArrayList<>();

    // Additional fields for UserDetails implementation
    @Column(name = "account_non_expired")
    private Boolean accountNonExpired = true;

    @Column(name = "account_non_locked")
    private Boolean accountNonLocked = true;

    @Column(name = "credentials_non_expired")
    private Boolean credentialsNonExpired = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Calculated field for age
    @Transient
    public Integer getAge() {
        if (dateOfBirth == null) return null;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    // UserDetails implementation methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired != null ? accountNonExpired : true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked != null ? accountNonLocked : true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired != null ? credentialsNonExpired : true;
    }

    @Override
    public boolean isEnabled() {
        return active != null ? active : true;
    }

    // Lifecycle methods
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public void setPassword(@Size(min = 8, message = "Password must be at least 8 characters") String password) {
        this.password = password;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public  @Size(min = 2, max = 100, message = "Working position must be between 2 and 100 characters") String getWorkingPosition() {
        return workingPosition;
    }

    public void setWorkingPosition(@Size(min = 2, max = 100, message = "Working position must be between 2 and 100 characters") String workingPosition) {
        this.workingPosition = workingPosition;
    }

    public @DecimalMin(value = "0.0", message = "Salary must be positive") @Digits(integer = 10, fraction = 2, message = "Invalid salary format") BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(@DecimalMin(value = "0.0", message = "Salary must be positive") @Digits(integer = 10, fraction = 2, message = "Invalid salary format") BigDecimal salary) {
        this.salary = salary;
    }

    public @DecimalMin(value = "0.0", message = "Total allowances must be positive") @Digits(integer = 10, fraction = 2, message = "Invalid allowances format") BigDecimal getTotalAllowances() {
        return totalAllowances;
    }

    public void setTotalAllowances(@DecimalMin(value = "0.0", message = "Total allowances must be positive") @Digits(integer = 10, fraction = 2, message = "Invalid allowances format") BigDecimal totalAllowances) {
        this.totalAllowances = totalAllowances;
    }

    public @Size(max = 100, message = "Bank account name too long") String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(@Size(max = 100, message = "Bank account name too long") String bankAccount) {
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getAccountNonExpired() {
        return accountNonExpired;
    }

    public void setAccountNonExpired(Boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public Boolean getAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(Boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public Boolean getCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    public void setCredentialsNonExpired(Boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<WorkExperience> getWorkExperience() {
        return workExperience;
    }

    public void setWorkExperience(List<WorkExperience> workExperience) {
        this.workExperience = workExperience;
    }

    public List<Education> getEducation() {
        return education;
    }

    public void setEducation(List<Education> education) {
        this.education = education;
    }

    //    public List<WorkExperience> getWorkExperience() {
//        return workExperience;
//    }
//
//    public void setWorkExperience(List<WorkExperience> workExperience) {
//        this.workExperience = workExperience;
//        // Set the user reference for each work experience
//        if (workExperience != null) {
//            workExperience.forEach(we -> we.setUser(this));
//        }
//    }
//
//    public List<Education> getEducation() {
//        return education;
//    }
//
//    public void setEducation(List<Education> education) {
//        this.education = education;
//        // Set the user reference for each education
//        if (education != null) {
//            education.forEach(e -> e.setUser(this));
//        }
//    }
}