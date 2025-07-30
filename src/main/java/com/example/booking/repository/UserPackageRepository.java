package com.example.booking.repository;

import com.example.booking.entity.User;
import com.example.booking.entity.UserPackage;
import com.example.booking.entity.PackageStatus;
import com.example.booking.entity.Country; // Import Country
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserPackageRepository extends JpaRepository<UserPackage, Long> {
    List<UserPackage> findByUser(User user);
    List<UserPackage> findByUserAndStatus(User user, PackageStatus status);
    List<UserPackage> findByStatus(PackageStatus status);
    List<UserPackage> findByUserAndPack_Country(User user, Country country);
}