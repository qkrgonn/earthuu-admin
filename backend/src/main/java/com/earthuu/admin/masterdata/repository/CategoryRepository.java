package com.earthuu.admin.masterdata.repository;

import com.earthuu.admin.masterdata.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    boolean existsByCodeIgnoreCase(String code);
    Optional<Category> findByCodeIgnoreCase(String code);
}
