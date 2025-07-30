package com.example.booking.controller;

import com.example.booking.dto.PackageDTO;
import com.example.booking.dto.request.PurchasePackageRequest;
import com.example.booking.dto.UserPackageDTO;
import com.example.booking.dto.response.ErrorResponse;
import com.example.booking.entity.User;
import com.example.booking.service.AuthService;
import com.example.booking.service.PackageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packages")
@PreAuthorize("isAuthenticated()")
public class PackageController {

    private final PackageService packageService;
    private final AuthService authService;

    public PackageController(PackageService packageService, AuthService authService) {
        this.packageService = packageService;
        this.authService = authService;
    }

    @GetMapping("/available")
    public ResponseEntity<List<PackageDTO>> getAvailablePackages(@RequestParam(required = false) Long countryId) {
        if (countryId == null) {
            return ResponseEntity.ok(packageService.getAllPackages()); // Or throw an error, depending on UX
        }
        List<PackageDTO> packages = packageService.getAllAvailablePackages(countryId);
        return ResponseEntity.ok(packages);
    }

    @PostMapping("/purchase")
    public ResponseEntity<?> purchasePackage(@AuthenticationPrincipal UserDetails userDetails,
                                                          @Valid @RequestBody PurchasePackageRequest request) {
        User user = authService.getUserProfile(userDetails.getUsername()); // Get the full User entity
        try {
            UserPackageDTO purchasedPackage = packageService.purchasePackage(user, request);
            return new ResponseEntity<>(purchasedPackage, HttpStatus.CREATED);
        } catch (com.example.booking.exception.ResourceNotFoundException | com.example.booking.exception.BadRequestException e) {
            return new ResponseEntity<>(new ErrorResponse("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/my-packages")
    public ResponseEntity<List<UserPackageDTO>> getMyPackages(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserProfile(userDetails.getUsername());
        List<UserPackageDTO> userPackages = packageService.getUserPackages(user);
        return ResponseEntity.ok(userPackages);
    }

}