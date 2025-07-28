package com.example.booking.controller;

import com.example.booking.entity.Package;
import com.example.booking.service.PackageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packages")
@SecurityRequirement(name = "bearerAuth")
public class PackageController {

    @Autowired
    private PackageService packageService;

    @GetMapping("/available")
    public ResponseEntity<List<Package>> getAvailablePackages(@RequestParam Long country) {
        return ResponseEntity.ok(packageService.getAvailablePackages(country));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Package>> getUserPackages(@PathVariable Long userId) {
        return ResponseEntity.ok(packageService.getUserPackages(userId));
    }

    @PostMapping("/buy")
    public ResponseEntity<Package> buyPackage(@RequestParam Long userId, @RequestParam Long packageId) {
        return ResponseEntity.ok(packageService.buyPackage(userId, packageId));
    }
}
