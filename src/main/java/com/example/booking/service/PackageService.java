package com.example.booking.service;

import com.example.booking.entity.Package;
import java.util.List;

public interface PackageService {

    List<Package> getAvailablePackages(Long country);

    List<Package> getUserPackages(Long userId);

    Package buyPackage(Long userId, Long packageId);

}
