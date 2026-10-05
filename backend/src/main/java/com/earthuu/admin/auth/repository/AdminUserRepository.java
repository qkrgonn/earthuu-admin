package com.earthuu.admin.auth.repository;

import com.earthuu.admin.auth.entity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface AdminUserRepository extends JpaRepository<AdminUser, UUID>, JpaSpecificationExecutor<AdminUser> {
}
