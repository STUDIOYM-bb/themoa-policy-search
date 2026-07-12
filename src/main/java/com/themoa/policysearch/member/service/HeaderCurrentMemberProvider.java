package com.themoa.policysearch.member.service;

import com.themoa.policysearch.common.exception.BadRequestException;
import com.themoa.policysearch.member.domain.Member;
import com.themoa.policysearch.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile({"local", "test"})
public class HeaderCurrentMemberProvider implements CurrentMemberProvider {
    private final HttpServletRequest request;
    private final MemberRepository memberRepository;

    public HeaderCurrentMemberProvider(HttpServletRequest request, MemberRepository memberRepository) {
        this.request = request;
        this.memberRepository = memberRepository;
    }

    @Override
    @Transactional
    public Member currentMember() {
        String header = request.getHeader("X-Member-Id");
        if (header == null || header.isBlank()) {
            return memberRepository.findById(1).orElseGet(() -> memberRepository.save(new Member("local-member")));
        }
        try {
            return memberRepository.findById(Integer.parseInt(header))
                    .orElseThrow(() -> new BadRequestException("존재하지 않는 회원입니다."));
        } catch (NumberFormatException ex) {
            throw new BadRequestException("X-Member-Id 헤더 형식이 올바르지 않습니다.");
        }
    }
}
