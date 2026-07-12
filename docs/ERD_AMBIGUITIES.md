# ERD 및 API 모호 사항

- `product_bookmark.product_id`와 `product_recommendation.product_id`는 여러 금융상품 테이블로 이어지는 점선 관계를 가진다. 하나의 FK가 예금/적금, 대출, 연금저축 상품 테이블을 동시에 참조할 수 없어 단일 FK를 적용하지 않았다.
- ERD의 일부 Domain 표기는 SQL 타입이라기보다 설명값에 가깝다. 판독 가능한 물리 타입은 그대로 사용했고, Domain 표기는 `VARCHAR`, `BOOLEAN`, `DECIMAL`, `DATE`, `DATETIME`, `JSON` 등으로 매핑했다.
- 행정안전부 공공서비스 상세 조회 Swagger 작업 경로는 공개 HTML에서 직접 보이지 않았다. Collector의 경로는 설정과 코드에서 교체 가능하게 두고 공식 data.go.kr 페이지를 문서에 남겼다.
- 지자체복지서비스 목록/상세 엔드포인트 경로는 공개 HTML에서 직접 보이지 않았다. 수집기 경로는 설정과 코드에서 교체 가능하게 두고 공식 data.go.kr 페이지를 문서에 남겼다.
