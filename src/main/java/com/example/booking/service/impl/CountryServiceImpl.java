package com.example.booking.service.impl;

import com.example.booking.entity.Country;
import com.example.booking.exception.ResourceNotFoundException;
import com.example.booking.repository.CountryRepository;
import com.example.booking.service.CountryService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CountryServiceImpl implements CountryService {

    private final CountryRepository countryRepository;

    public CountryServiceImpl(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    @Cacheable(value = "countries", key = "#id")
    @Override
    public Country getCountryById(Long id) {
        System.out.println("Fetching Country from DB: " + id);
        return countryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Country not found with ID: " + id));
    }

}