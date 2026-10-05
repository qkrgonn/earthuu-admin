package com.earthuu.admin.masterdata.repository;

import com.earthuu.admin.masterdata.entity.UniversityDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface UniversityDomainRepository extends JpaRepository<UniversityDomain, String> {
    List<UniversityDomain> findByUniversityIdOrderByDomain(UUID universityId);
}
