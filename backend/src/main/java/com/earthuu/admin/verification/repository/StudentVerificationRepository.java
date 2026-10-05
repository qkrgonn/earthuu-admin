package com.earthuu.admin.verification.repository;

import com.earthuu.admin.verification.entity.StudentVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface StudentVerificationRepository extends JpaRepository<StudentVerification, UUID>,
        JpaSpecificationExecutor<StudentVerification> {
}
