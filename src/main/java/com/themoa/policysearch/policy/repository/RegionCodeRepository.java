package com.themoa.policysearch.policy.repository;

import com.themoa.policysearch.policy.domain.RegionCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionCodeRepository extends JpaRepository<RegionCode, Integer> {
    Optional<RegionCode> findByRegionCode(String regionCode);
    List<RegionCode> findByProvinceAndCity(String province, String city);
}
