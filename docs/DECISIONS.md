# 결정 사항

- 프로젝트는 기존 저장소 루트에 직접 생성했다. 기존 `.git`, remote, branch, IntelliJ 설정은 삭제하거나 초기화하지 않았다.
- `final_ERD.png`를 스키마의 최우선 기준으로 삼았다. ERD에 원본 수집과 임베딩 동기화 테이블이 없어서 정책 운영 테이블은 V2 마이그레이션에 추가했다.
- `product_bookmark.product_id`와 `product_recommendation.product_id`는 여러 금융상품 테이블을 가리킬 수 있는 식별자다. ERD에 공통 상품 상위 테이블이 없으므로 MySQL 단일 FK를 강제하지 않고, 회원 FK와 필수 상품 ID 컬럼을 유지했다.
- data.go.kr 공개 페이지에서 API 유형, 응답 형식, 작업 존재 여부는 확인했지만 정확한 Swagger 경로와 필드명은 활용 승인 또는 로그인 후 확인이 필요할 수 있다. 따라서 출처별 Client와 유연한 파서를 사용하고 원본 응답을 보존한다.
- RAG는 선택 기능이다. `RAG_ENABLED=false`이면 OpenAI 또는 Qdrant 인증 정보 없이도 애플리케이션이 기동되고 MySQL 대체 검색을 사용한다.
- LLM 출력은 최종 신청 가능성 판단 근거로 사용하지 않는다. 최종 판별은 Java Evaluator가 정책 조건과 사용자 조건을 비교해 계산한다.
