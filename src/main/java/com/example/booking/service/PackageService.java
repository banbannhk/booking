package com.example.booking.service;

import com.example.booking.dto.PackageDTO;
import com.example.booking.dto.request.PurchasePackageRequest;
import com.example.booking.dto.UserPackageDTO;
import com.example.booking.entity.User;

import java.util.List;

public interface PackageService {

    List<PackageDTO> getAllAvailablePackages(Long countryId);
    List<PackageDTO> getAllPackages();
    UserPackageDTO purchasePackage(User user, PurchasePackageRequest request);
    List<UserPackageDTO> getUserPackages(User user);

}