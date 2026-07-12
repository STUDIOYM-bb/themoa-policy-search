package com.themoa.policysearch.member.service;

import com.themoa.policysearch.common.exception.BadRequestException;
import com.themoa.policysearch.member.domain.Member;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!local & !test")
public class NoopCurrentMemberProvider implements CurrentMemberProvider {
    @Override
    public Member currentMember() {
        throw new BadRequestException("인증이 필요합니다.");
    }
}
