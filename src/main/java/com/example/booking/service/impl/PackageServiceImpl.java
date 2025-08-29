package com.example.booking.service.impl;

import com.example.booking.dto.PackageDTO;
import com.example.booking.dto.request.PurchasePackageRequest;
import com.example.booking.dto.UserPackageDTO;
import com.example.booking.entity.Country;
import com.example.booking.entity.Package;
import com.example.booking.entity.PackageStatus;
import com.example.booking.entity.User;
import com.example.booking.entity.UserPackage;
import com.example.booking.exception.BadRequestException;
import com.example.booking.exception.BusinessRuleViolationException;
import com.example.booking.exception.InternalServerErrorException;
import com.example.booking.exception.ResourceNotFoundException;
import com.example.booking.repository.PackageRepository;
import com.example.booking.repository.UserPackageRepository;
import com.example.booking.service.CountryService;
import com.example.booking.service.PackageService;
import com.example.booking.service.PaymentService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PackageServiceImpl implements PackageService {

    private final PackageRepository packageRepository;
    private final UserPackageRepository userPackageRepository;
    private final CountryService countryService;
    private final PaymentService paymentService;

    public PackageServiceImpl(PackageRepository packageRepository,
                              UserPackageRepository userPackageRepository,
                              CountryService countryService,
                              PaymentService paymentService) {
        this.packageRepository = packageRepository;
        this.userPackageRepository = userPackageRepository;
        this.countryService = countryService;
        this.paymentService = paymentService;
    }


    @Cacheable(value = "packages", key = "#id")
    public Package findById(Long id) {
        System.out.println("Fetching Package by ID from DB: " + id);
        return packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found with ID: " + id));
    }


    @Override
    @Cacheable(value = "packages", key = "#countryId + ':byCountry'")
    public List<PackageDTO> getAllAvailablePackages(Long countryId) {
        try{
            Country country = countryService.getCountryById(countryId);
            List<Package> packages = packageRepository.findByCountry(country);
            return packages.stream().map(this::mapToPackageDTO).collect(Collectors.toList());
        } catch (Exception e) {
            if (e instanceof ResourceNotFoundException) {
                throw e;
            }
            throw new InternalServerErrorException("Failed to retrieve all available List");
        }
    }

    @Override
    @Cacheable(value = "packages", key = "'allPackages'")
    public List<PackageDTO> getAllPackages() {
        try{
            return packageRepository.findAll().stream()
                    .map(this::mapToPackageDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            if (e instanceof ResourceNotFoundException) {
                throw e;
            }
            throw new InternalServerErrorException("Failed to retrieve all package List");
        }
    }

    @Override
    @Transactional
    public UserPackageDTO purchasePackage(User user, PurchasePackageRequest request) {
        try{
            Package pack = findById(request.getPackageId());

            // 2. Process Payment (Mocked)
            boolean paymentSuccess = paymentService.chargePayment(user.getId().toString(), pack.getPrice(), "USD"); // Or get currency from country

            if (!paymentSuccess) {
                throw new BusinessRuleViolationException("PAYMENT_FIALED", "Payment failed for package purchase.");
            }

            // 3. Create UserPackage
            UserPackage userPackage = UserPackage.builder()
                    .user(user)
                    .pack(pack)
                    .remainingCredits(pack.getCredits())
                    .expiryDate(LocalDate.now().plusDays(pack.getExpiryDays()))
                    .status(PackageStatus.ACTIVE)
                    .build();

            userPackage = userPackageRepository.save(userPackage);
            return mapToUserPackageDTO(userPackage);
        } catch (Exception e) {
            if (e instanceof ResourceNotFoundException) {
                throw e;
            }
            throw new InternalServerErrorException("Failed to purchasePackage");
        }
    }

    @Override
    public List<UserPackageDTO> getUserPackages(User user) {
        try{
            updateExpiredUserPackagesStatusForUser(user);
            List<UserPackage> userPackages = userPackageRepository.findByUser(user);
            return userPackages.stream()
                    .map(this::mapToUserPackageDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            if (e instanceof ResourceNotFoundException) {
                throw e;
            }
            throw new InternalServerErrorException("Failed to getUserPackages");
        }
    }

    @Transactional
    private void updateExpiredUserPackagesStatusForUser(User user) {
        try{
            List<UserPackage> userActivePackages = userPackageRepository
                    .findByUserAndStatus(user, PackageStatus.ACTIVE);

            userActivePackages.forEach(userPackage -> {
                if (userPackage.getExpiryDate().isBefore(LocalDate.now())) {
                    userPackage.setStatus(PackageStatus.EXPIRED);
                    userPackageRepository.save(userPackage);
                }
            });
        } catch (Exception e) {
            if (e instanceof ResourceNotFoundException) {
                throw e;
            }
            throw new InternalServerErrorException("Failed to updateExpiredUserPackagesStatusForUser");
        }
    }


    private PackageDTO mapToPackageDTO(Package pack) {
        return PackageDTO.builder()
                .id(pack.getId())
                .name(pack.getName())
                .credits(pack.getCredits())
                .price(pack.getPrice())
                .expiryDays(pack.getExpiryDays())
                .createdAt(pack.getCreatedAt())
                .countryId(pack.getCountry() != null ? pack.getCountry().getId() : null)
                .countryName(pack.getCountry() != null ? pack.getCountry().getName() : null)
                .countryCode(pack.getCountry() != null ? pack.getCountry().getCode() : null)
                .build();
    }

    private UserPackageDTO mapToUserPackageDTO(UserPackage userPackage) {
        return UserPackageDTO.builder()
                .id(userPackage.getId())
                .userId(userPackage.getUser().getId())
                .packageId(userPackage.getPack().getId())
                .packageName(userPackage.getPack().getName())
                .remainingCredits(userPackage.getRemainingCredits())
                .expiryDate(userPackage.getExpiryDate())
                .status(determineUserPackageStatus(userPackage))
                .countryId(userPackage.getPack().getCountry().getId())
                .countryName(userPackage.getPack().getCountry().getName())// Dynamic status determination
                .build();
    }

    private PackageStatus determineUserPackageStatus(UserPackage userPackage) {
        if (userPackage.getExpiryDate().isBefore(LocalDate.now())) {
            return PackageStatus.EXPIRED;
        }
        if (userPackage.getRemainingCredits() <= 0) {
            return PackageStatus.USED_UP;
        }
        return PackageStatus.ACTIVE;
    }
}