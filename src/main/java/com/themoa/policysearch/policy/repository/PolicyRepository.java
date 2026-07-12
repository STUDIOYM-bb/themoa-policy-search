package com.themoa.policysearch.policy.repository;

import com.themoa.policysearch.policy.domain.Policy;
import com.themoa.policysearch.policy.domain.PolicySource;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PolicyRepository extends JpaRepository<Policy, Integer> {
    long countByActiveTrue();

    @EntityGraph(attributePaths = {"condition", "regions", "regions.region"})
    List<Policy> findAllByActiveTrue();

    Optional<Policy> findBySourceTypeAndSourcePolicyId(PolicySource sourceType, String sourcePolicyId);

    @EntityGraph(attributePaths = {"condition", "regions", "regions.region"})
    List<Policy> findAllBySourceTypeIn(Collection<PolicySource> sourceTypes);

    @EntityGraph(attributePaths = {"condition", "regions", "regions.region"})
    @Query("select distinct p from Policy p left join p.regions pr left join pr.region r where p.id in :ids")
    List<Policy> findAllDetailedByIdIn(@Param("ids") Collection<Integer> ids);

    @EntityGraph(attributePaths = {"condition", "regions", "regions.region"})
    @Query("""
        select distinct p from Policy p
        left join p.regions pr left join pr.region r
        where p.active = true
          and (:keyword is null or lower(p.title) like lower(concat('%', :keyword, '%'))
            or lower(p.summary) like lower(concat('%', :keyword, '%'))
            or lower(p.agencyName) like lower(concat('%', :keyword, '%')))
        """)
    Page<Policy> fallbackSearch(@Param("keyword") String keyword, Pageable pageable);
}
