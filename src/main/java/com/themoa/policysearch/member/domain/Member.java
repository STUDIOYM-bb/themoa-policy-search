package com.themoa.policysearch.member.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "member")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;
    @Column(nullable = false, length = 100)
    private String password;
    @Column(nullable = false, length = 10)
    private String name;
    @Column(nullable = false, length = 100)
    private String email;
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;
    @Column(length = 100)
    private String region;
    @Column(name = "codef_connected_id", length = 100)
    private String codefConnectedId;
    @Column(name = "salary_amount", precision = 14, scale = 2)
    private BigDecimal salaryAmount;

    protected Member() {
    }

    public Member(String loginId) {
        this.loginId = loginId;
        this.password = "{noop}local";
        this.name = "local";
        this.email = loginId + "@local.invalid";
        this.birthDate = LocalDate.of(2000, 1, 1);
    }

    public Integer getId() { return id; }
    public String getLoginId() { return loginId; }
    public String getName() { return name; }
}
