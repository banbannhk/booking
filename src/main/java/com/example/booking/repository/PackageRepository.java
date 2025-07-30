package com.example.booking.repository;

import com.example.booking.entity.Country;
import com.example.booking.entity.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {
    // Find all packages available in a specific country
    List<Package> findByCountry(Country country);

    // Find all packages regardless of country (for admin or global view)
    List<Package> findAll();

}