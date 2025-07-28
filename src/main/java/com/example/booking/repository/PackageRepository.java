package com.example.booking.repository;

import com.example.booking.entity.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PackageRepository extends JpaRepository<Package, Long> {

    // List available packages for a country that are not expired
    List<Package> findByCountryId(Long countryId);

    // Packages purchased by user
    @Query("SELECT up.pack FROM UserPackage up WHERE up.user.id = :userId")
    List<Package> findPurchasedPackagesByUserId(@Param("userId") Long userId);

//    List<Package> findByUserId(Long id);
}
