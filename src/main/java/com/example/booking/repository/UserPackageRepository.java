package com.example.booking.repository;

import com.example.booking.entity.PackageStatus;
import com.example.booking.entity.UserPackage;
import com.example.booking.entity.User;
import com.example.booking.entity.Package;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPackageRepository extends JpaRepository<UserPackage, Long> {

    List<UserPackage> findByUser(User user);

    Optional<UserPackage> findByUserAndPackAndStatus(User user, Package pack, PackageStatus status);

    List<UserPackage> findByUserAndStatus(User user, PackageStatus status);
}
