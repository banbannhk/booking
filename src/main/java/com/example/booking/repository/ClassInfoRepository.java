package com.example.booking.repository;

import com.example.booking.entity.ClassInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassInfoRepository extends JpaRepository<ClassInfo, Long> {
}