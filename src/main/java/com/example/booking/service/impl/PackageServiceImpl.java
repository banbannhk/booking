package com.example.booking.service.impl;

import com.example.booking.entity.Package;
import com.example.booking.entity.User;
import com.example.booking.repository.PackageRepository;
import com.example.booking.repository.UserRepository;
import com.example.booking.service.PackageService;
import jakarta.transaction.Transactional;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class PackageServiceImpl implements PackageService {

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public List<Package> getAvailablePackages(Long country) {
        return packageRepository.findByCountryId(country);
    }

    @Override
    public List<Package> getUserPackages(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return new ArrayList<>();
        //        return packageRepository.findByUserId(user.getId()); // Adjust depending on your package-user mapping
    }

    @Override
    public Package buyPackage(Long userId, Long packageId) {
        String lockKey = "buy_package_lock_" + userId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(10, 5, TimeUnit.SECONDS)) {
                // Logic to add package to user and deduct payment (mocked)
                // Save package purchase info, update user credits, etc.
                Package pkg = packageRepository.findById(packageId).orElseThrow(() -> new RuntimeException("Package not found"));
                // Assume you create a UserPackage or similar entity to track ownership

                // Mock payment success
                return pkg;
            } else {
                throw new RuntimeException("Could not acquire lock for package purchase");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Lock interrupted");
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }
}
