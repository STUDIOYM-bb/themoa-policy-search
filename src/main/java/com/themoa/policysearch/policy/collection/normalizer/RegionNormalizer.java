package com.themoa.policysearch.policy.collection.normalizer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class RegionNormalizer {
    private static final Map<String, String> PROVINCES = Map.ofEntries(
            Map.entry("서울", "서울특별시"),
            Map.entry("서울시", "서울특별시"),
            Map.entry("서울특별시", "서울특별시"),
            Map.entry("부산", "부산광역시"),
            Map.entry("부산시", "부산광역시"),
            Map.entry("부산광역시", "부산광역시"),
            Map.entry("대구", "대구광역시"),
            Map.entry("인천", "인천광역시"),
            Map.entry("광주광역시", "광주광역시"),
            Map.entry("대전", "대전광역시"),
            Map.entry("울산", "울산광역시"),
            Map.entry("세종", "세종특별자치시"),
            Map.entry("경기", "경기도"),
            Map.entry("경기도", "경기도"),
            Map.entry("강원", "강원특별자치도"),
            Map.entry("충북", "충청북도"),
            Map.entry("충남", "충청남도"),
            Map.entry("전북", "전북특별자치도"),
            Map.entry("전남", "전라남도"),
            Map.entry("경북", "경상북도"),
            Map.entry("경남", "경상남도"),
            Map.entry("제주", "제주특별자치도")
    );
    private static final Map<String, String> CITY_TO_REGION = Map.ofEntries(
            Map.entry("수원", "경기도 수원시"),
            Map.entry("수원시", "경기도 수원시"),
            Map.entry("성남", "경기도 성남시"),
            Map.entry("성남시", "경기도 성남시"),
            Map.entry("용인", "경기도 용인시"),
            Map.entry("용인시", "경기도 용인시"),
            Map.entry("고양", "경기도 고양시"),
            Map.entry("고양시", "경기도 고양시"),
            Map.entry("화성", "경기도 화성시"),
            Map.entry("화성시", "경기도 화성시")
    );

    public List<String> normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        String text = raw.replaceAll("\\s+", " ").trim();
        if (isAgencyLike(text)) {
            return List.of();
        }
        if (containsAny(text, "전국", "전 국민", "전국민")) {
            return List.of("전국");
        }
        Set<String> regions = new LinkedHashSet<>();
        String province = null;
        for (Map.Entry<String, String> entry : PROVINCES.entrySet()) {
            if (containsToken(text, entry.getKey())) {
                province = entry.getValue();
                regions.add(province);
                break;
            }
        }
        for (Map.Entry<String, String> entry : CITY_TO_REGION.entrySet()) {
            if (containsToken(text, entry.getKey())) {
                String cityRegion = entry.getValue();
                if (province == null || cityRegion.startsWith(province + " ")) {
                    regions.remove(province);
                    regions.add(cityRegion);
                }
            }
        }
        if (text.contains("광주") && !text.contains("광주광역시") && !text.contains("경기도 광주시")) {
            return List.of();
        }
        return new ArrayList<>(regions);
    }

    public boolean matchesStrict(String userRegion, List<String> policyRegions) {
        List<String> userRegions = normalize(userRegion);
        if (userRegions.isEmpty()) {
            return true;
        }
        if (policyRegions == null || policyRegions.isEmpty()) {
            return false;
        }
        if (policyRegions.contains("전국")) {
            return true;
        }
        for (String user : userRegions) {
            if (policyRegions.contains(user)) {
                return true;
            }
            if (user.contains(" ") && policyRegions.contains(user.substring(0, user.indexOf(' ')))) {
                return true;
            }
        }
        return false;
    }

    private boolean isAgencyLike(String text) {
        String lowered = text.toLowerCase(Locale.ROOT);
        if (containsAny(text, "중앙행정기관", "공공기관", "주관기관", "운영기관", "보건복지부", "고용노동부",
                "교육부", "농림축산식품부", "여성가족부", "국토교통부")) {
            return true;
        }
        return lowered.endsWith("부") || lowered.endsWith("처") || lowered.endsWith("청")
                || lowered.endsWith("공단") || lowered.endsWith("공사") || lowered.endsWith("기관");
    }

    private boolean containsAny(String text, String... values) {
        for (String value : values) {
            if (text.contains(value)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsToken(String text, String token) {
        return text.equals(token) || text.contains(token + " ") || text.contains(" " + token)
                || text.contains(token + ",") || text.contains("," + token) || text.contains(token + "/")
                || text.contains("/" + token);
    }
}
