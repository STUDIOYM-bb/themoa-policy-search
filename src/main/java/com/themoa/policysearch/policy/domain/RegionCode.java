package com.themoa.policysearch.policy.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "region_code")
public class RegionCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private RegionCode parent;
    @Column(name = "region_code", nullable = false, unique = true, length = 30)
    private String regionCode;
    @Column(nullable = false, length = 50)
    private String province;
    @Column(length = 50)
    private String city;
    @Column(name = "region_level", nullable = false, length = 30)
    private String regionLevel;

    protected RegionCode() {
    }

    public RegionCode(String regionCode, String province, String city, String regionLevel) {
        this.regionCode = regionCode;
        this.province = province;
        this.city = city;
        this.regionLevel = regionLevel;
    }

    public Integer getId() { return id; }
    public String getRegionCode() { return regionCode; }
    public String getProvince() { return province; }
    public String getCity() { return city; }

    public String displayName() {
        if ("전국".equals(province)) {
            return "전국";
        }
        return city == null || city.isBlank() ? province : province + " " + city;
    }
}
