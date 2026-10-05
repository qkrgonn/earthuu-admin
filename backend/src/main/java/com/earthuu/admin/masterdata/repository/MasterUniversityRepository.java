package com.earthuu.admin.masterdata.repository;

import com.earthuu.admin.event.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MasterUniversityRepository extends JpaRepository<University, UUID> {
    boolean existsByNameKoIgnoreCase(String nameKo);
}
