package com.earthuu.admin.verification.repository;

import com.earthuu.admin.event.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VerificationUniversityRepository extends JpaRepository<University, UUID> {
}
