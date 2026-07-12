const MEMBER_ID = "1";

const labelMap = {
  OPEN: "신청 가능",
  UPCOMING: "신청 예정",
  CLOSED: "신청 마감",
  ALWAYS_OPEN: "상시 신청",
  NEEDS_CONFIRMATION: "확인 필요",
  INACTIVE: "비활성",
  ELIGIBLE: "신청 가능성 높음",
  LIKELY_ELIGIBLE: "가능성 높음",
  UNLIKELY: "신청 어려움",
  UNKNOWN: "판별 불가",
  RAG: "Qdrant RAG",
  MYSQL_FALLBACK: "MySQL 검색",
  RAG_WITH_MYSQL_FALLBACK: "RAG 후 MySQL 보완",
  NEEDS_MORE_INFORMATION: "조건 보완 필요",
  OPENAI: "OpenAI",
  RULE_BASED: "규칙 기반",
  APPLICATION_START: "신청 시작",
  APPLICATION_END: "신청 마감"
};

const api = {
  async request(url, options = {}) {
    const headers = Object.assign({"X-Member-Id": MEMBER_ID}, options.headers || {});
    if (options.body && !headers["Content-Type"]) headers["Content-Type"] = "application/json";
    const res = await fetch(url, Object.assign({}, options, {headers}));
    const text = await res.text();
    let json = null;
    if (text) {
      try {
        json = JSON.parse(text);
      } catch {
        throw new Error(`서버 응답을 해석하지 못했습니다. HTTP ${res.status}`);
      }
    }
    if (!res.ok || (json && json.success === false)) {
      throw new Error((json && json.message) || `요청 실패 HTTP ${res.status}`);
    }
    return json ? json.data : null;
  },
  get(url) { return this.request(url); },
  post(url, body, headers = {}) { return this.request(url, {method: "POST", body: JSON.stringify(body || {}), headers}); },
  del(url) { return this.request(url, {method: "DELETE"}); }
};

document.addEventListener("DOMContentLoaded", () => {
  const page = document.body.dataset.page || "home";
  if (page === "home") initSearch();
  if (page === "detail") loadDetail();
  if (page === "bookmarks") loadBookmarks();
  if (page === "calendar") initCalendar();
  if (page === "dev") initDevConsole();
});

function initSearch() {
  document.querySelectorAll("[data-example]").forEach(button => {
    button.addEventListener("click", () => {
      const query = button.dataset.example;
      document.getElementById("query").value = query;
      runSearch(query, {});
    });
  });
  document.getElementById("searchForm").addEventListener("submit", event => {
    event.preventDefault();
    runSearch(document.getElementById("query").value.trim(), currentSupplemental());
  });
}

async function runSearch(query, supplementalConditions) {
  const loading = byId("loading");
  const error = byId("error");
  const button = byId("searchButton");
  loading.hidden = false;
  error.hidden = true;
  button.disabled = true;
  try {
    const data = await api.post("/api/policies/search", {query, supplementalConditions, page: 0, size: 10});
    renderSearchSummary(data);
    renderDiagnostics(data);
    renderFollowups(data, query);
    renderResults(data.results || [], byId("results"), data);
  } catch (e) {
    error.textContent = e.message;
    error.hidden = false;
  } finally {
    loading.hidden = true;
    button.disabled = false;
  }
}

function renderSearchSummary(data) {
  const el = byId("summary");
  el.hidden = false;
  el.innerHTML = `
    <article class="summary-card wide"><strong>${escapeHtml(data.message)}</strong></article>
    <article class="summary-card"><span>검색 방식</span><strong>${label(data.searchMode)}</strong></article>
    <article class="summary-card"><span>조건 분석</span><strong>${label(data.parserMode)}</strong></article>
    <article class="summary-card"><span>검색 후보</span><strong>${number(data.vectorCandidateCount)}건</strong></article>
    <article class="summary-card"><span>최종 결과</span><strong>${number(data.filteredResultCount)}건</strong></article>
    <article class="summary-card"><span>소요 시간</span><strong>${number(data.elapsedTimeMs)}ms</strong></article>
    ${data.degraded ? `<article class="summary-card warning wide">${escapeHtml(fallbackNotice(data))}</article>` : ""}
  `;
}

function fallbackNotice(data) {
  const reason = data.fallbackReason || "";
  if (reason.includes("Qdrant 검색 후보가 없어")) {
    return "Qdrant 연결은 가능하지만 현재 검색어에 대한 벡터 후보가 없어 MySQL 검색으로 보완했습니다. 임베딩 PENDING이 남아 있다면 개발 확인에서 PENDING 임베딩 처리를 먼저 실행하세요.";
  }
  if (reason.includes("필터 후 결과가 부족")) {
    return "Qdrant 검색 후 조건 필터를 통과한 결과가 부족해 MySQL 검색으로 보완했습니다.";
  }
  if (reason.includes("Qdrant 검색 실패")) {
    return `Qdrant 검색 호출이 실패해 MySQL 검색으로 전환했습니다. ${reason}`;
  }
  return `MySQL 검색으로 보완했습니다. ${reason}`;
}

function renderDiagnostics(data) {
  const el = byId("diagnostics");
  el.hidden = false;
  const c = data.interpretedConditions || {};
  el.innerHTML = `
    <details>
      <summary>검색 과정 보기</summary>
      <div class="diagnostic-grid">
        ${kv("원본 검색어", data.originalQuery)}
        ${kv("조건 분석 방식", label(data.parserMode))}
        ${kv("OpenAI fallback", data.parserFallback ? data.parserFallbackReason : "사용하지 않음")}
        ${kv("지역", c.region)}
        ${kv("나이", c.age ? `${c.age}세` : c.ageGroup)}
        ${kv("대상", join(c.targetGroups))}
        ${kv("취업 상태", employmentLabel(c.employmentStatus))}
        ${kv("학생 여부", studentLabel(c.studentStatus))}
        ${kv("소득", c.incomeCondition)}
        ${kv("주거", c.housingStatus)}
        ${kv("가구", c.householdStatus)}
        ${kv("관심 분야", c.category)}
        ${kv("키워드", join(c.keywords))}
        ${kv("부족한 조건", join(data.missingConditions))}
        ${kv("RAG 호출 여부", data.ragAttempted ? "호출함" : "호출하지 않음")}
        ${kv("Qdrant 성공 여부", data.ragSucceeded ? "성공" : "성공하지 않음")}
        ${kv("Qdrant 후보 수", `${number(data.vectorCandidateCount)}건`)}
        ${kv("MySQL 조회 후보 수", `${number(data.databaseCandidateCount)}건`)}
        ${kv("조건 필터 후 결과 수", `${number(data.filteredResultCount)}건`)}
        ${kv("Fallback", data.degraded ? data.fallbackReason : "사용하지 않음")}
      </div>
      <button class="json-toggle" type="button">JSON 보기</button>
      <pre class="json-block" hidden>${escapeHtml(JSON.stringify(data, null, 2))}</pre>
    </details>
  `;
  el.querySelector(".json-toggle").addEventListener("click", event => {
    const pre = event.target.nextElementSibling;
    pre.hidden = !pre.hidden;
  });
}

function renderFollowups(data, query) {
  const el = byId("followups");
  if (!data.followUpQuestions || data.followUpQuestions.length === 0) {
    el.hidden = true;
    return;
  }
  el.hidden = false;
  const c = data.interpretedConditions || {};
  el.innerHTML = `
    <h2>조건을 보완하면 더 정확해집니다</h2>
    <p class="muted">${escapeHtml(data.followUpQuestions.join(" "))}</p>
    <div class="field-grid">
      ${inputField("지역", "suppRegion", c.region || "", "예: 경기도 수원시")}
      ${inputField("나이", "suppAge", c.age || "", "예: 27")}
      ${selectField("취업 상태", "suppEmployment", c.employmentStatus, [["", "선택"], ["EMPLOYED", "재직 중"], ["JOB_SEEKER", "구직 중"], ["UNEMPLOYED", "무직"], ["FREELANCER", "프리랜서"]])}
      ${selectField("학생 여부", "suppStudent", boolValue(c.studentStatus), [["", "선택"], ["true", "대학생/휴학생/졸업생"], ["false", "해당 없음"]])}
      ${inputField("소득", "suppIncome", c.incomeCondition || "", "예: 중위소득 100% 이하")}
      ${selectField("주거", "suppHousing", c.housingStatus, [["", "선택"], ["월세", "월세"], ["전세", "전세"], ["자가", "자가"], ["무주택", "무주택"], ["잘 모르겠음", "잘 모르겠음"]])}
      ${selectField("가구", "suppHousehold", c.householdStatus, [["", "선택"], ["1인 가구", "1인 가구"], ["신혼부부", "신혼부부"], ["한부모", "한부모"], ["해당 없음", "해당 없음"]])}
      ${selectField("관심 분야", "suppCategory", c.category, [["", "선택"], ["생활지원", "생활지원"], ["주거", "주거"], ["일자리", "일자리"], ["교육", "교육"], ["금융", "금융"], ["창업", "창업"], ["복지", "복지"]])}
    </div>
    <button id="refineBtn" type="button">조건 적용</button>
  `;
  byId("refineBtn").onclick = () => runSearch(query, currentSupplemental());
}

function currentSupplemental() {
  return {
    region: valueOf("suppRegion"),
    age: valueOf("suppAge"),
    employmentStatus: valueOf("suppEmployment"),
    studentStatus: valueOf("suppStudent"),
    incomeStatus: valueOf("suppIncome"),
    housingStatus: valueOf("suppHousing"),
    householdStatus: valueOf("suppHousehold"),
    category: valueOf("suppCategory")
  };
}

function renderResults(results, container, searchData = null) {
  if (!results.length) {
    const noData = searchData && Number(searchData.databaseCandidateCount || 0) === 0 && Number(searchData.vectorCandidateCount || 0) === 0;
    container.innerHTML = noData ? `<article class="empty-state">
      <h2>아직 검색할 정책 데이터가 없습니다</h2>
      <p>정책 검색을 사용하려면 먼저 외부 정책 API에서 데이터를 수집하고, 임베딩을 처리해야 합니다.</p>
      <div class="actions">
        <a href="/dev-console">개발 확인에서 수집 시작</a>
      </div>
    </article>` : `<article class="empty-state">표시할 정책이 없습니다.</article>`;
    return;
  }
  container.innerHTML = results.map(cardHtml).join("");
  container.querySelectorAll("[data-bookmark-toggle]").forEach(button => {
    button.addEventListener("click", () => toggleBookmark(button));
  });
}

function cardHtml(item) {
  return `<article class="policy-card">
    <div class="card-heading">
      <h2>${escapeHtml(item.policyName)}</h2>
      <div class="meta">
        <span class="badge">${escapeHtml(item.category)}</span>
        <span class="badge">${label(item.applicationStatus)}</span>
        <span class="badge ${item.eligibilityStatus === "UNLIKELY" ? "danger" : "warn"}">${label(item.eligibilityStatus)}</span>
      </div>
    </div>
    <div class="policy-fields">
      ${field("출처", join(item.sources))}
      ${field("주관 기관", join(item.organizations))}
      ${field("지역", join(item.regionNames))}
      ${field("지원 대상", item.targetSummary)}
      ${field("지원 내용", item.supportSummary)}
      ${field("신청 기간", item.applicationPeriod)}
      ${field("추천 이유", item.recommendationReason)}
      ${item.semanticScore == null ? "" : field("의미 유사도", Number(item.semanticScore).toFixed(3))}
      ${field("일치 조건", join(item.matchedConditions))}
      ${field("부족 조건", join(item.missingConditions))}
      ${field("불일치 조건", join(item.unmatchedConditions))}
    </div>
    <div class="actions">
      <a href="/policies/${item.policyId}">상세 보기</a>
      <button type="button" data-bookmark-toggle="${item.policyId}" data-bookmarked="${item.bookmarked}">${item.bookmarked ? "관심 정책 해제" : "관심 정책 저장"}</button>
    </div>
  </article>`;
}

async function toggleBookmark(button) {
  const policyId = button.dataset.bookmarkToggle;
  const bookmarked = button.dataset.bookmarked === "true";
  button.disabled = true;
  try {
    if (bookmarked) {
      await api.del(`/api/policies/${policyId}/bookmarks`);
      button.dataset.bookmarked = "false";
      button.textContent = "관심 정책 저장";
      const page = document.body.dataset.page;
      if (page === "bookmarks") button.closest(".policy-card").remove();
    } else {
      await api.post(`/api/policies/${policyId}/bookmarks`, {});
      button.dataset.bookmarked = "true";
      button.textContent = "관심 정책 해제";
    }
  } catch (e) {
    alert(e.message);
  } finally {
    button.disabled = false;
  }
}

async function loadDetail() {
  const id = location.pathname.split("/").pop();
  const detail = await api.get(`/api/policies/${id}`);
  byId("detail").innerHTML = `<h1>${escapeHtml(detail.policyName)}</h1>
    <p class="notice">${escapeHtml(detail.notice)}</p>
    <div class="detail-grid">
      ${field("주관 기관", detail.organization)}
      ${field("출처", join(detail.sources))}
      ${field("지역", join(detail.regionNames))}
      ${field("분야", detail.category)}
      ${field("지원 내용", detail.supportContent)}
      ${field("지원 대상", detail.targetSummary)}
      ${field("신청 기간", detail.applicationPeriod)}
      ${field("신청 상태", label(detail.applicationStatus))}
      ${field("신청 방법", detail.applicationMethod)}
      ${field("필요 서류", detail.requiredDocuments)}
      ${field("문의처", detail.contact)}
    </div>
    <div class="actions">
      ${detail.officialUrl ? `<a href="${escapeAttr(detail.officialUrl)}" rel="noopener noreferrer" target="_blank">공식 링크</a>` : ""}
      <button type="button" data-bookmark-toggle="${detail.policyId}" data-bookmarked="false">관심 정책 저장</button>
    </div>`;
  byId("detail").querySelector("[data-bookmark-toggle]").addEventListener("click", event => toggleBookmark(event.target));
}

async function loadBookmarks() {
  const error = byId("error");
  try {
    const data = await api.get("/api/bookmarks");
    renderResults(data, byId("results"));
  } catch (e) {
    error.textContent = e.message;
    error.hidden = false;
  }
}

let calendarCursor = new Date();

function initCalendar() {
  byId("prevMonth").onclick = () => { calendarCursor = new Date(calendarCursor.getFullYear(), calendarCursor.getMonth() - 1, 1); loadCalendar(); };
  byId("nextMonth").onclick = () => { calendarCursor = new Date(calendarCursor.getFullYear(), calendarCursor.getMonth() + 1, 1); loadCalendar(); };
  byId("todayMonth").onclick = () => { calendarCursor = new Date(); loadCalendar(); };
  loadCalendar();
}

async function loadCalendar() {
  const y = calendarCursor.getFullYear();
  const m = calendarCursor.getMonth();
  byId("calendarTitle").textContent = `${y}년 ${m + 1}월 정책 캘린더`;
  const first = new Date(y, m, 1);
  const last = new Date(y, m + 1, 0);
  const gridStart = new Date(first);
  gridStart.setDate(first.getDate() - ((first.getDay() + 6) % 7));
  const gridEnd = new Date(last);
  gridEnd.setDate(last.getDate() + (6 - ((last.getDay() + 6) % 7)));
  const events = await api.get(`/api/calendar?from=${dateKey(gridStart)}&to=${dateKey(gridEnd)}`);
  const byDate = events.reduce((acc, event) => {
    (acc[event.eventDate] ||= []).push(event);
    return acc;
  }, {});
  const days = [];
  for (let d = new Date(gridStart); d <= gridEnd; d.setDate(d.getDate() + 1)) {
    const key = dateKey(d);
    days.push(`<div class="month-day ${d.getMonth() === m ? "" : "muted-day"}">
      <strong>${d.getDate()}</strong>
      ${(byDate[key] || []).map(event => `<button type="button" class="calendar-event" onclick="location.href='/policies/${event.policyId}'">${label(event.eventType)} · ${escapeHtml(event.title)}</button>`).join("")}
    </div>`);
  }
  byId("calendar").innerHTML = `<div class="weekdays">${["월", "화", "수", "목", "금", "토", "일"].map(day => `<span>${day}</span>`).join("")}</div><div class="month-grid">${days.join("")}</div>`;
}

async function initDevConsole() {
  const adminInput = byId("adminKey");
  adminInput.value = sessionStorage.getItem("adminKey") || "";
  adminInput.addEventListener("input", () => sessionStorage.setItem("adminKey", adminInput.value));
  byId("refreshDevStatus").onclick = loadDevStatus;
  document.querySelectorAll("[data-admin-action]").forEach(button => {
    button.addEventListener("click", () => runAdminAction(button));
  });
  document.querySelectorAll("[data-probe-source]").forEach(button => {
    button.addEventListener("click", () => runSourceProbe(button));
  });
  byId("vectorSearchForm").addEventListener("submit", event => {
    event.preventDefault();
    runVectorSearch();
  });
  await loadDevStatus();
  await loadCollectionRuns();
}

async function loadDevStatus() {
  const status = await api.get("/api/dev/status");
  byId("devStatus").innerHTML = `
    ${kv("Spring Boot", status.application)}
    ${kv("MySQL", status.database)}
    ${kv("Qdrant", status.qdrant)}
    ${kv("OpenAI API Key", status.openAiConfigured ? "설정됨" : "미설정")}
    ${kv("RAG 활성", status.ragEnabled ? "활성" : "비활성")}
    ${kv("VectorStore", status.vectorStoreAvailable ? "사용 가능" : "사용 불가")}
    ${kv("Collection", status.collectionName)}
    ${kv("전체 정책 수", `${number(status.policyCount)}건`)}
    ${kv("활성 정책 수", `${number(status.activePolicyCount)}건`)}
    ${kv("임베딩 PENDING", `${number(status.embedding.pending)}건`)}
    ${kv("임베딩 SYNCED", `${number(status.embedding.synced)}건`)}
    ${kv("임베딩 FAILED", `${number(status.embedding.failed)}건`)}
  `;
}

async function loadCollectionRuns() {
  const rows = await api.get("/api/dev/collection-runs?limit=20");
  byId("collectionRuns").innerHTML = table(["출처", "상태", "수신", "신규", "갱신", "제외", "실패", "오류", "시작"],
    rows.map(row => [row.source, row.status, row.receivedCount, row.insertedCount, row.updatedCount, row.skippedCount, row.failedCount, row.representativeError, row.startedAt]));
}

async function runAdminAction(button) {
  const key = byId("adminKey").value.trim();
  const result = byId("adminResult");
  if (!key) {
    result.hidden = false;
    result.textContent = "관리자 키를 입력하세요.";
    return;
  }
  button.disabled = true;
  result.hidden = false;
  result.textContent = "작업 실행 중입니다.";
  try {
    const data = await api.post(button.dataset.adminAction, {}, {"X-Admin-Key": key});
    result.innerHTML = renderAdminActionResult(data);
    await loadDevStatus();
    await loadCollectionRuns();
  } catch (e) {
    result.textContent = e.message;
  } finally {
    button.disabled = false;
  }
}

function renderAdminActionResult(data) {
  if (Array.isArray(data)) {
    return collectionResultTable(data);
  }
  if (typeof data === "object" && data) {
    if ("source" in data) {
      return collectionResultTable([data]);
    }
    if ("activePolicyCount" in data || "pendingCountAfter" in data) {
      return table(["항목", "값"], [
        ["활성 정책", number(data.activePolicyCount)],
        ["신규 대기열", number(data.newlyQueuedCount)],
        ["재등록", number(data.requeuedCount)],
        ["변경 없음", number(data.unchangedCount)],
        ["비활성 처리", number(data.deactivatedCount)],
        ["등록 실패", number(data.failedCount)],
        ["PENDING", number(data.pendingCountAfter)],
        ["대표 오류", data.representativeError]
      ]);
    }
    if ("processedCount" in data || "remainingPendingCount" in data) {
      return table(["항목", "값"], [
        ["전체 대상", number(data.totalTargetCount)],
        ["처리 완료", number(data.processedCount)],
        ["성공", number(data.successCount)],
        ["실패", number(data.failedCount)],
        ["남은 PENDING", number(data.remainingPendingCount)],
        ["현재 배치", number(data.currentBatch)],
        ["소요 시간", `${number(data.elapsedTimeMs)}ms`]
      ]);
    }
    return `<pre class="json-block">${escapeHtml(JSON.stringify(data, null, 2))}</pre>`;
  }
  return `작업 완료: ${escapeHtml(data)}`;
}

async function runSourceProbe(button) {
  const key = byId("adminKey").value.trim();
  const result = byId("probeResult");
  if (!key) {
    result.innerHTML = `<p class="empty-state">관리자 키를 입력하세요.</p>`;
    return;
  }
  button.disabled = true;
  result.innerHTML = `<p class="empty-state">연결 진단 중입니다.</p>`;
  try {
    const data = await api.post(`/api/dev/policy-sources/${button.dataset.probeSource}/probe`, {}, {"X-Admin-Key": key});
    result.innerHTML = table(["항목", "값"], [
      ["출처", data.source],
      ["실제 호출 여부", data.actuallyCalled ? "예" : "아니오"],
      ["마스킹된 URL", data.maskedRequestUrl],
      ["HTTP 상태", data.httpStatus],
      ["Content-Type", data.contentType],
      ["응답 형식", data.responseType || responseFormat(data.contentType, data.responsePreview)],
      ["목록 노드 확인 여부", data.listNodeFound ? "예" : "아니오"],
      ["전체 건수", data.totalCount],
      ["파싱된 건수", data.parsedCount],
      ["첫 정책 ID", data.firstPolicyId],
      ["첫 번째 정책명", data.firstPolicyName],
      ["오류 코드", data.errorCode],
      ["오류 메시지", data.errorMessage],
      ["응답 미리보기", data.responsePreview]
    ]);
  } catch (e) {
    result.innerHTML = `<p class="empty-state">${escapeHtml(e.message)}</p>`;
  } finally {
    button.disabled = false;
  }
}

function collectionResultTable(rows) {
  return `<div class="collection-result-wrapper">${table(["출처", "상태", "요청", "수신", "신규", "갱신", "제외", "실패", "오류"],
    rows.map(row => [row.source, row.failedCount > 0 ? "FAILED" : "SUCCESS", row.apiRequestCount, row.receivedCount,
      row.insertedCount, row.updatedCount, row.skippedCount, row.failedCount, row.representativeError]))}</div>`;
}

function responseFormat(contentType, preview) {
  const value = `${contentType || ""} ${preview || ""}`.trim().toLowerCase();
  if (value.includes("json") || value.startsWith("{") || value.startsWith("[")) return "JSON";
  if (value.includes("xml") || value.startsWith("<")) return "XML/HTML";
  return "";
}

async function runVectorSearch() {
  const data = await api.post("/api/dev/vector-search", {query: byId("vectorQuery").value.trim(), topK: 10});
  byId("vectorResult").innerHTML = table(["순위", "policyId", "정책명", "score", "출처", "분야", "지역", "Qdrant Document ID", "문서 일부"],
    data.map((row, index) => [index + 1, row.policyId, row.policyName, row.score == null ? "" : Number(row.score).toFixed(3), row.source, row.category, join(row.regionNames), row.documentId, row.contentSnippet]));
}

function field(labelText, value) {
  return `<p><strong>${escapeHtml(labelText)}</strong><span>${escapeHtml(value || "정보가 제공되지 않았습니다.")}</span></p>`;
}

function kv(key, value) {
  return `<p><span>${escapeHtml(key)}</span><strong>${escapeHtml(value || "정보 없음")}</strong></p>`;
}

function inputField(labelText, id, value, placeholder) {
  return `<label class="field-label">${escapeHtml(labelText)}<input id="${id}" value="${escapeAttr(value)}" placeholder="${escapeAttr(placeholder)}"></label>`;
}

function selectField(labelText, id, selected, options) {
  return `<label class="field-label">${escapeHtml(labelText)}<select id="${id}">${options.map(([value, text]) => `<option value="${escapeAttr(value)}" ${String(selected ?? "") === value ? "selected" : ""}>${escapeHtml(text)}</option>`).join("")}</select></label>`;
}

function table(headers, rows) {
  if (!rows.length) return `<p class="empty-state">표시할 데이터가 없습니다.</p>`;
  return `<table class="collection-result-table"><thead><tr>${headers.map(h => `<th>${escapeHtml(h)}</th>`).join("")}</tr></thead><tbody>${rows.map(row => `<tr>${row.map(cell => `<td>${escapeHtml(cell ?? "")}</td>`).join("")}</tr>`).join("")}</tbody></table>`;
}

function label(value) { return labelMap[value] || value || "확인 필요"; }
function join(values) { return Array.isArray(values) && values.length ? values.join(", ") : ""; }
function number(value) { return Number(value || 0).toLocaleString("ko-KR"); }
function boolValue(value) { return value === true ? "true" : value === false ? "false" : ""; }
function studentLabel(value) { return value === true ? "학생" : value === false ? "해당 없음" : ""; }
function employmentLabel(value) {
  return {EMPLOYED: "재직 중", UNEMPLOYED: "무직", JOB_SEEKER: "구직 중", FREELANCER: "프리랜서"}[value] || value || "";
}
function valueOf(id) { const el = byId(id); return el && el.value ? el.value.trim() : null; }
function byId(id) { return document.getElementById(id); }
function dateKey(date) {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
}
function escapeHtml(value) {
  return String(value ?? "").replace(/[&<>"']/g, ch => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[ch]));
}
function escapeAttr(value) {
  const url = String(value ?? "");
  return escapeHtml(url);
}
