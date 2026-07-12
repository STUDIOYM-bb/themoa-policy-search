-- 기관명 또는 비지역 문자열이 region_code / region display name으로 저장된 후보를 찾는다.
-- 삭제하지 않고 영향 범위만 확인한다.

SELECT
    r.id AS region_id,
    r.region_code,
    r.province,
    r.city,
    r.region_level,
    COUNT(pr.id) AS policy_region_count
FROM region_code r
LEFT JOIN policy_region pr ON pr.region_id = r.id
WHERE
    r.province IN ('중앙행정기관', '공공기관', '보건복지부', '고용노동부', '교육부', '농림축산식품부')
    OR r.province LIKE '%주관기관%'
    OR r.province LIKE '%운영기관%'
    OR r.province LIKE '%공단'
    OR r.province LIKE '%공사'
    OR r.province LIKE '%기관'
    OR r.province LIKE '%부'
    OR r.province LIKE '%처'
    OR r.province LIKE '%청'
    OR (
        r.province NOT IN (
            '전국', '서울특별시', '부산광역시', '대구광역시', '인천광역시', '광주광역시',
            '대전광역시', '울산광역시', '세종특별자치시', '경기도', '강원특별자치도',
            '충청북도', '충청남도', '전북특별자치도', '전라남도', '경상북도', '경상남도',
            '제주특별자치도'
        )
        AND r.city IS NULL
    )
GROUP BY r.id, r.region_code, r.province, r.city, r.region_level
ORDER BY policy_region_count DESC, r.province, r.city;
