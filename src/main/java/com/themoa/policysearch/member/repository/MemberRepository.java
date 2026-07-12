package com.themoa.policysearch.member.repository;

import com.themoa.policysearch.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Integer> {
}
