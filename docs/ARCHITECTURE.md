# 아키텍처

이 애플리케이션은 MySQL을 정책 데이터의 최종 원본 저장소로 사용하고, Qdrant는 임베딩된 정책 문서를 검색하기 위한 보조 저장소로만 사용한다.

```text
외부 정책 API
-> 출처별 수집기
-> 원본 응답 저장
-> 정책 정규화
-> MySQL 정책 테이블
-> 임베딩 동기화 대기열
-> Qdrant
-> 자연어 검색
-> Java 신청 가능성 판별
-> 정책 결과 카드
```

Controller는 Service만 호출한다. Collector는 외부 API 응답을 출처별로 해석하고 `PolicyCollectionItem`으로 변환한다. 검색 기능은 외부 정책 API를 직접 호출하지 않는다.

Spring AI 1.1.8은 공식 BOM으로 버전을 통일했다. Spring AI 1.1.8 릴리스 공지에서 기준 Spring Boot 버전이 3.5.15로 올라갔음을 확인했고, 이 프로젝트는 같은 3.5 계열의 Spring Boot 3.5.16을 사용한다. Qdrant Vector Store는 Spring AI 방식으로 구성하며 컬렉션명은 설정값으로 관리하고, 벡터 차원은 코드에 고정하지 않는다.
