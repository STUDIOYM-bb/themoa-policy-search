# API 필드 매핑

확인한 공식 문서:

- 온통청년 OPEN API: https://www.youthcenter.go.kr/cmnFooter/openapiIntro/oaiDoc
- 행정안전부 대한민국 공공서비스 혜택 정보: https://www.data.go.kr/data/15113968/openapi.do
- 한국사회보장정보원 지자체복지서비스: https://www.data.go.kr/data/15108347/openapi.do
- 한국사회보장정보원 중앙부처복지서비스: https://www.data.go.kr/data/15090532/openapi.do

| API | 호출 URL | HTTP 방식 | 인증키 파라미터 | 페이지 파라미터 | 페이지 크기 파라미터 | 응답 형식 | 목록 필드 | 전체 건수 필드 | 상세 조회 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 온통청년 | `https://www.youthcenter.go.kr/opi/youthPlcyList.do` | GET | `openApiVlak` | `pageIndex` | `display` | JSON | 응답 구조에 따라 탐색 | 응답 구조에 따라 탐색 | 공개 페이지에서 별도 상세 API 미확인 |
| 공공서비스 혜택 | `http://apis.data.go.kr/...` | GET | `serviceKey` | `pageNo` | `numOfRows` | JSON/XML | `items.item` | `totalCount` | Swagger 확인 필요 |
| 지자체복지서비스 | `http://apis.data.go.kr/...` | GET | `serviceKey` | `pageNo` | `numOfRows` | XML | `items.item` | `totalCount` | 목록/상세 작업 존재 |
| 중앙부처복지서비스 | `http://apis.data.go.kr/B554287/NationalWelfareInformationsV001/NationalWelfarelistV001` | GET | `serviceKey` | `pageNo` | `numOfRows` | XML | `items.item` | `totalCount` | `NationalWelfaredetailedV001` |

수집기는 JSON과 XML을 모두 파싱하고, 출처별 응답 구조 차이를 견딜 수 있도록 필드명을 후보 목록으로 탐색한다. data.go.kr 공개 상세 페이지에서 API 유형과 응답 형식은 확인했지만, 행정안전부와 지자체 API의 세부 작업 경로는 활용 승인 후 Swagger 화면 확인이 필요할 수 있다. 모호한 항목은 `docs/ERD_AMBIGUITIES.md`에 기록했다.

내부 공통 필드:

- 출처, 출처별 정책 ID, 정책명, 기관명, 관리기관명
- 지역명, 지역 코드, 분야, 대상 그룹
- 최소/최대 나이, 취업 조건, 학생 조건, 소득 조건, 주거 조건, 가구 조건
- 선정 기준, 지원 내용, 신청 방법, 필요 서류
- 신청 시작일, 신청 마감일, 신청 기간 원문, 상시 신청 여부
- 공식 URL, 문의처, 키워드, 출처 갱신일, 수집일, 원본 데이터 ID
