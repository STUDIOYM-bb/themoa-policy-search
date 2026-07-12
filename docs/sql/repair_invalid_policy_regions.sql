-- 실행 전 docs/sql/find_invalid_region_codes.sql 결과를 확인한다.
-- 이 스크립트는 자동 실행용이 아니다. 영향 건수 확인 후 트랜잭션 안에서 수동 적용한다.

START TRANSACTION;

-- 1. 삭제 후보 관계 수를 먼저 확인한다.
SELECT COUNT(*) AS delete_candidate_count
FROM policy_region pr
JOIN region_code r ON r.id = pr.region_id
WHERE
    r.province IN ('중앙행정기관', '공공기관', '보건복지부', '고용노동부', '교육부', '농림축산식품부')
    OR r.province LIKE '%주관기관%'
    OR r.province LIKE '%운영기관%'
    OR r.province LIKE '%공단'
    OR r.province LIKE '%공사'
    OR r.province LIKE '%기관'
    OR r.province LIKE '%부'
    OR r.province LIKE '%처'
    OR r.province LIKE '%청';

-- 2. 위 건수가 예상 범위인지 확인한 뒤에만 주석을 해제한다.
-- DELETE pr
-- FROM policy_region pr
-- JOIN region_code r ON r.id = pr.region_id
-- WHERE
--     r.province IN ('중앙행정기관', '공공기관', '보건복지부', '고용노동부', '교육부', '농림축산식품부')
--     OR r.province LIKE '%주관기관%'
--     OR r.province LIKE '%운영기관%'
--     OR r.province LIKE '%공단'
--     OR r.province LIKE '%공사'
--     OR r.province LIKE '%기관'
--     OR r.province LIKE '%부'
--     OR r.province LIKE '%처'
--     OR r.province LIKE '%청';

-- 3. 중앙부처/공공서비스 정책 중 지역 관계가 사라진 활성 정책은 재수집 후 전국으로 갱신되도록 한다.
-- 코드 수정 후 재수집을 우선 사용한다.

ROLLBACK;
