package com.auca_hr.AUCA_HR_System.repositories;


import com.auca_hr.AUCA_HR_System.entities.User;
import com.auca_hr.AUCA_HR_System.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.role = 'HR'")
    List<User> findAllActiveHRUsers();

    Optional<User> findByNationalId(String nationalId);

    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByNationalId(String nationalId);

    boolean existsByPhoneNumber(String phoneNumber);

    List<User> findByRole(UserRole role);

    List<User> findByFullNamesContainingIgnoreCase(String username);


}
