const $ = (s) => document.querySelector(s),
  esc = (s) =>
    String(s ?? "").replace(
      /[&<>"']/g,
      (c) =>
        ({
          "&": "&amp;",
          "<": "&lt;",
          ">": "&gt;",
          '"': "&quot;",
          "'": "&#39;",
        })[c],
    );
const kstDate = new Intl.DateTimeFormat("en-CA", {
  timeZone: "Asia/Seoul",
  year: "numeric",
  month: "2-digit",
  day: "2-digit",
});
const todayISO = kstDate.format(new Date());
const todayLabel = new Intl.DateTimeFormat("ko-KR", {
  timeZone: "Asia/Seoul",
  month: "long",
  day: "numeric",
  weekday: "long",
}).format(new Date());
const shortDate = (s) => s?.slice(5).replace("-", ".") || "—";
const activeIn = (days) =>
  Math.round(
    metrics.slice(-days).reduce((n, d) => n + d.web + d.app, 0) * 0.48,
  );
const icons = {
  dashboard:
    '<rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5"/>',
  review: '<path d="M9 3h6v4H9zM7 5H5v16h14V5h-2M8 12h8M8 16h5"/>',
  events:
    '<rect x="3" y="5" width="18" height="16" rx="2"/><path d="M7 3v4m10-4v4M3 11h18"/>',
  reports: '<path d="M12 3 3 7v5c0 5 9 9 9 9s9-4 9-9V7zM12 8v5m0 3v1"/>',
  stats: '<path d="M4 3v17h17M8 16v-5m5 5V7m5 9V4"/>',
};
const icon = (k) =>
  `<span class="ico"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${icons[k] || icons.events}</svg></span>`;
const pages = {
  dashboard: "대시보드",
  review: "이벤트 심사",
  events: "이벤트 관리",
  reports: "신고 관리",
  stats: "통계",
  login: "관리자 로그인",
};
const statuses = {
  unresolved: "미처리 전체",
  pending: "심사신청",
  reviewing: "심사 중",
  approved: "승인완료",
  rejected: "불가 판정",
  open: "모집 중",
  ended: "종료",
  cancelled: "폐기",
  received: "접수",
  investigating: "검토 중",
  resolved: "처리 완료",
  confirmed: "참여 확정",
  declined: "반려",
  applied: "승인 대기",
  suspended: "운영 중단",
  not_open: "모집 전",
};
const badge = (s) =>
  `<span class="badge ${["pending", "received", "applied"].includes(s) ? "wait" : ["reviewing", "investigating"].includes(s) ? "review" : ["approved", "confirmed", "resolved"].includes(s) ? "ok" : ["rejected", "declined", "suspended"].includes(s) ? "no" : ""}">${statuses[s] || esc(s)}</span>`;
let events = [
  {
    id: "EVT-1024",
    title: "한강에서 만나는 글로벌 피크닉",
    host: "김지우",
    school: "연세대학교",
    cat: "문화 교류",
    date: "2026-10-04",
    submitted: "2026-09-27",
    venue: "여의도 한강공원",
    status: "pending",
    life: "open",
    capacity: 20,
  },
  {
    id: "EVT-1025",
    title: "한국어 × 영어, 언어 교환의 밤",
    host: "박서준",
    school: "고려대학교",
    cat: "언어 교환",
    date: "2026-10-05",
    submitted: "2026-09-28",
    venue: "안암 캠퍼스 라운지",
    status: "pending",
    life: "open",
    capacity: 16,
  },
  {
    id: "EVT-1026",
    title: "처음 만나는 서울, 골목 산책",
    host: "Emma Wilson",
    school: "서울대학교",
    cat: "로컬 탐방",
    date: "2026-10-08",
    submitted: "2026-09-29",
    venue: "안국역 3번 출구",
    status: "reviewing",
    life: "open",
    capacity: 12,
  },
  {
    id: "EVT-1027",
    title: "세계의 식탁 · 함께 만드는 저녁",
    host: "이수민",
    school: "성균관대학교",
    cat: "음식",
    date: "2026-10-09",
    submitted: "2026-09-29",
    venue: "혜화 공유 주방",
    status: "pending",
    life: "open",
    capacity: 10,
  },
  {
    id: "EVT-1019",
    title: "주말 배드민턴, 같이 한 게임!",
    host: "최유진",
    school: "한양대학교",
    cat: "스포츠",
    date: "2026-10-03",
    submitted: "2026-09-23",
    venue: "왕십리 실내체육관",
    status: "approved",
    life: "open",
    capacity: 16,
  },
  {
    id: "EVT-1018",
    title: "북촌 필름 사진 산책",
    host: "Alex Chen",
    school: "홍익대학교",
    cat: "로컬 탐방",
    date: "2026-10-02",
    submitted: "2026-09-22",
    venue: "북촌 한옥마을",
    status: "approved",
    life: "open",
    capacity: 12,
  },
  {
    id: "EVT-1015",
    title: "외국인 유학생 유료 취업 설명회",
    host: "정민호",
    school: "서강대학교",
    cat: "커리어",
    date: "2026-10-02",
    submitted: "2026-09-20",
    venue: "신촌 스터디룸",
    status: "rejected",
    life: "open",
    capacity: 30,
    reason: "교류 목적보다 외부 유료 서비스 홍보가 주목적입니다.",
  },
  {
    id: "EVT-1012",
    title: "남산 야경과 함께하는 산책",
    host: "김하늘",
    school: "이화여자대학교",
    cat: "로컬 탐방",
    date: "2026-09-28",
    submitted: "2026-09-18",
    venue: "남산도서관",
    status: "approved",
    life: "cancelled",
    capacity: 15,
    cancelReason: "호스트 개인 사정으로 행사 취소",
  },
  {
    id: "EVT-1008",
    title: "캠퍼스 보드게임 클럽",
    host: "박지민",
    school: "연세대학교",
    cat: "문화 교류",
    date: "2026-09-25",
    submitted: "2026-09-16",
    venue: "신촌 학생회관",
    status: "approved",
    life: "ended",
    capacity: 20,
  },
].map((e) => ({
  ...e,
  life: e.status === "approved" ? e.life : "not_open",
  version: 1,
  history: [
    { action: "심사신청", at: e.submitted + " 10:00", actor: e.host },
    ...(e.status !== "pending"
      ? [{ action: "심사 시작", at: e.submitted + " 14:00", actor: "김관리" }]
      : []),
    ...(["approved", "rejected"].includes(e.status)
      ? [
          {
            action: statuses[e.status],
            at: e.submitted + " 15:00",
            actor: "김관리",
          },
        ]
      : []),
  ],
}));
let reports = [
  {
    id: "RPT-0031",
    type: "event",
    target: "EVT-1019",
    title: "주말 배드민턴, 같이 한 게임!",
    reason: "모집 내용과 다른 비용 요구",
    description: "모집 글에 없는 추가 대관료를 개인 메시지로 요청받았습니다.",
    evidence: "예시 증빙: “참여 확정 후 별도 대관료를 보내주세요.”",
    status: "received",
    date: "2026-09-30",
    history: [],
  },
  {
    id: "RPT-0030",
    type: "host",
    target: "EVT-1018",
    title: "Alex Chen",
    reason: "부적절한 메시지",
    description: "참가 신청과 무관한 개인 연락이 반복적으로 왔습니다.",
    evidence: "예시 증빙: 반복된 개인 연락 내역",
    status: "investigating",
    date: "2026-09-29",
    history: [{ action: "검토 시작", at: "2026-09-29 16:00", actor: "김관리" }],
  },
  {
    id: "RPT-0029",
    type: "event",
    target: "EVT-1018",
    title: "북촌 필름 사진 산책",
    reason: "이벤트 정보 오류",
    description: "상세 장소와 지도에 표시된 위치가 다릅니다.",
    evidence: "예시 증빙: 장소 안내 화면",
    status: "received",
    date: "2026-09-28",
    history: [],
  },
  {
    id: "RPT-0027",
    type: "host",
    target: "EVT-1008",
    title: "박지민",
    reason: "불친절한 응대",
    description: "신청 결과 안내가 지연되었습니다.",
    evidence: "예시 증빙: 신청 날짜 기록",
    status: "resolved",
    date: "2026-09-26",
    resolution: "조치 불필요",
    note: "안내 완료 사실을 확인했습니다.",
    history: [],
  },
];
const participantsByEvent = {
  "EVT-1019": [
    { name: "김민지", group: "한국학생", status: "confirmed" },
    { name: "Sophie Martin", group: "국제학생", status: "applied" },
  ],
  "EVT-1018": [
    { name: "이준호", group: "한국학생", status: "confirmed" },
    { name: "Daniel Kim", group: "국제학생", status: "declined" },
  ],
  "EVT-1008": [{ name: "박민서", group: "한국학생", status: "confirmed" }],
};
let route = location.hash.slice(1) || "login",
  tab = "all",
  query = "",
  sort = "old",
  filter = "all",
  from = "",
  to = "",
  logged = false,
  period = "7",
  channel = "all";
try {
  logged = sessionStorage.getItem("earthuu-demo-session") === "active";
} catch {}
function setSession(active) {
  logged = active;
  try {
    if (active) sessionStorage.setItem("earthuu-demo-session", "active");
    else sessionStorage.removeItem("earthuu-demo-session");
  } catch {}
}
function logout() {
  setSession(false);
  $("#detail").close();
  go("login");
  notify("로그아웃했습니다.");
}
const metricEnd = new Date(`${todayISO}T12:00:00+09:00`);
let metrics = Array.from({ length: 30 }, (_, i) => {
  const date = new Date(metricEnd);
  date.setDate(date.getDate() - (29 - i));
  return {
    date: kstDate.format(date),
    web: 38 + ((i * 17) % 73),
    app: 61 + ((i * 23) % 97),
    joins: 3 + ((i * 7) % 15),
  };
});
const now = () =>
  new Date().toLocaleString("sv-SE", { timeZone: "Asia/Seoul" }).slice(0, 16);
function notify(t) {
  $("#toast").textContent = t;
  $("#toast").style.display = "block";
  clearTimeout(window.toastTimer);
  window.toastTimer = setTimeout(
    () => ($("#toast").style.display = "none"),
    3000,
  );
}
function runOnce(button, action) {
  if (!button || button.dataset.busy === "true") return;
  button.dataset.busy = "true";
  button.disabled = true;
  button.setAttribute("aria-busy", "true");
  action();
}
function syncRoute() {
  try {
    window.history.replaceState(null, "", "#" + route);
  } catch {
    if (location.hash !== "#" + route) location.hash = route;
  }
}
function go(p, t = "all") {
  route = logged && pages[p] ? p : "login";
  tab = t;
  query = "";
  filter =
    route === "reports" && ["event", "host"].includes(t) ? "unresolved" : "all";
  from = "";
  to = "";
  syncRoute();
  render();
}
window.addEventListener("hashchange", () => {
  route = location.hash.slice(1) || "dashboard";
  if (!pages[route]) route = "dashboard";
  render();
});
function head(title, sub, extra = "") {
  return `<div class="page-head"><div><h1>${title}</h1><p class="sub">${sub}</p></div>${extra}</div>`;
}
function periodControl() {
  return `<div class="filters"><select aria-label="조회 기간" id="period"><option value="7" ${period === "7" ? "selected" : ""}>최근 7일</option><option value="30" ${period === "30" ? "selected" : ""}>최근 30일</option><option value="1" ${period === "1" ? "selected" : ""}>오늘</option></select><select aria-label="접속 채널" id="channel"><option value="all">전체 채널</option><option value="web" ${channel === "web" ? "selected" : ""}>웹</option><option value="app" ${channel === "app" ? "selected" : ""}>앱</option></select></div>`;
}
function metric(label, value, note, cls = "", dest = "", t = "all") {
  return `<${dest ? "button" : "div"} class="metric ${cls}" ${dest ? `data-go="${dest}" data-tab="${t}"` : ""}><div class="metric-top">${label}<span>${dest ? "" : ""}</span></div><span class="value">${value}<small>${label.includes("건") || label.includes("신고") || label.includes("심사") ? "건" : "명"}</small></span><div class="metric-note">${note}</div></${dest ? "button" : "div"}>`;
}
function statsData() {
  const a = metrics.slice(-Number(period));
  let visits = a.reduce(
    (n, d) => n + (channel === "all" ? d.web + d.app : d[channel]),
    0,
  );
  return {
    a,
    visits,
    users: Math.round(visits * 0.71),
    active: Math.round(visits * 0.48),
    joins: a.reduce((n, d) => n + d.joins, 0),
  };
}
function chart() {
  let { a } = statsData();
  if (a.length > 10)
    a = Array.from({ length: 6 }, (_, i) => ({
      date: `${i * 5 + 1}–${i * 5 + 5}`,
      web: a.slice(i * 5, i * 5 + 5).reduce((n, d) => n + d.web, 0) / 5,
      app: a.slice(i * 5, i * 5 + 5).reduce((n, d) => n + d.app, 0) / 5,
    }));
  return `<div class="chart" role="img" aria-label="선택 기간의 일별 웹과 앱 방문 횟수 예시"><div class="axis"><span>200</span><span>150</span><span>100</span><span>50</span><span>0</span></div><div class="bars">${a.map((d) => `<div class="bar-group">${channel !== "app" ? `<div class="bar" title="웹 ${Math.round(d.web)}회" style="height:${d.web / 2}%"></div>` : ""}${channel !== "web" ? `<div class="bar alt" title="앱 ${Math.round(d.app)}회" style="height:${d.app / 2}%"></div>` : ""}<span>${d.date.length > 8 ? d.date.slice(5).replace("-", "/") : d.date}</span></div>`).join("")}</div></div>`;
}
function charts() {
  const { a, visits } = statsData();
  let app = a.reduce((n, d) => n + d.app, 0),
    web = a.reduce((n, d) => n + d.web, 0),
    pct =
      channel === "web"
        ? 0
        : channel === "app"
          ? 100
          : Math.round((app / (app + web)) * 100),
    range = `${shortDate(a[0]?.date)}–${shortDate(a.at(-1)?.date)}`;
  return `<div class="split"><section class="panel"><div class="panel-head"><div><h2>이용 추이</h2><p>방문 횟수 · ${period === "30" ? "5일 평균" : "일별"} · ${range}</p></div><div class="legend"><span><i></i>웹</span><span><i class="alt"></i>앱</span></div></div><div class="panel-body">${chart()}</div></section><section class="panel"><div class="panel-head"><div><h2>접속 채널</h2><p>선택 기간의 방문 횟수 기준</p></div></div><div class="panel-body channel"><div class="donut" style="background:conic-gradient(var(--blue) 0 ${pct}%,#cbd8ff ${pct}% 100%)"><div class="donut-inner"><small>전체 방문</small><b>${visits.toLocaleString()}</b></div></div><div class="channel-list"><div>앱 <b>${pct}%</b></div><div>웹 <b>${100 - pct}%</b></div><small class="sub">예시 집계</small></div></div></section></div>`;
}
function eventRows(list, compact = false) {
  return `<div class="table-wrap"><table><thead><tr><th>이벤트</th><th>호스트</th>${compact ? "" : "<th>개최일</th>"}<th>신청일</th><th>심사 상태</th>${!compact && route === "events" ? "<th>운영 상태</th>" : ""}</tr></thead><tbody>${list.map((e) => `<tr><td><button class="event-link" data-event="${esc(e.id)}">${esc(e.title)}</button><small>${esc(e.id)} · ${esc(e.cat)}</small></td><td>${esc(e.host)}<small>${esc(e.school)}</small></td>${compact ? "" : `<td>${esc(e.date.replaceAll("-", "."))}<small>${esc(e.venue)}</small></td>`}<td>${esc(shortDate(e.submitted))}<small>${esc(e.submitted.slice(0, 4))}</small></td><td>${badge(e.status)}</td>${!compact && route === "events" ? `<td>${e.status === "approved" ? badge(e.life) : "—"}</td>` : ""}</tr>`).join("") || '<tr><td colspan="6" class="empty">조건에 맞는 이벤트가 없습니다.</td></tr>'}</tbody></table></div>`;
}
function dashboard() {
  const s = statsData(),
    pending = events.filter((e) => e.status === "pending"),
    review = events.filter((e) => e.status === "reviewing"),
    openReports = reports.filter((r) => r.status !== "resolved");
  return (
    head(
      "대시보드",
      "오늘의 운영 현황을 확인하세요.",
      `<span class="sub">${esc(todayLabel)}</span>`,
    ) +
    `<div class="section-label">처리할 업무<small>현재 기준 · 기간 필터와 무관</small></div><div class="cards">${metric("심사신청", pending.length, "검토를 기다리는 이벤트", "blue", "review", "pending")}${metric("심사 중", review.length, "진행 중인 심사 확인", "", "review", "reviewing")}${metric("이벤트 신고", openReports.filter((r) => r.type === "event").length, "미처리 신고", "", "reports", "event")}${metric("호스트 신고", openReports.filter((r) => r.type === "host").length, "미처리 신고", "", "reports", "host")}</div><div class="section-label"><span>서비스 이용 현황</span>${periodControl()}</div><div class="cards">${metric("방문 사용자", s.users.toLocaleString(), "기간 내 중복 제외 · 예시")}${metric("활성 사용자", s.active.toLocaleString(), "조회·신청 활동 기준 · 예시")}${metric("신규 가입자", s.joins, "선택 기간 내 가입 · 전체 채널")}${metric("전체 가입자", "2,486", "현재 기준 · 전체 채널")}</div>${charts()}<div class="split"><section class="panel"><div class="panel-head"><div><h2>심사를 기다리고 있어요 <span class="sub">${pending.length}</span></h2><p>오래 기다린 신청부터 표시합니다.</p></div><button class="link" data-go="review">전체 보기</button></div>${eventRows(pending.slice(0, 3), true)}</section><section class="panel"><div class="panel-head"><h2>미처리 신고 <span class="sub">${openReports.length}</span></h2><button class="link" data-go="reports">전체 보기</button></div>${
      openReports
        .slice(0, 3)
        .map(
          (r) =>
            `<div class="report-item"><span class="report-icon">!</span><div class="text"><button class="event-link" data-report="${esc(r.id)}">${esc(r.reason)}</button><p>${r.type === "event" ? "이벤트" : "호스트"} · ${esc(r.title)}</p></div>${badge(r.status)}</div>`,
        )
        .join("") || '<div class="empty">미처리 신고가 없습니다.</div>'
    }</section></div>`
  );
}
function tabs(items) {
  return `<div class="tabs">${items.map(([v, l]) => `<button data-tab-select="${v}" class="${tab === v ? "active" : ""}" aria-pressed="${tab === v}">${l}</button>`).join("")}</div>`;
}
function toolbar(report = false) {
  return `<div class="toolbar"><input id="search" aria-label="${report ? "신고" : "이벤트"} 검색" placeholder="${report ? "신고 ID, 대상, 사유 검색" : "이벤트명, ID, 호스트 검색"}" value="${esc(query)}"><select id="state-filter" aria-label="${report ? "처리" : "운영"} 상태"><option value="all">${report ? "모든 처리 상태" : "모든 운영 상태"}</option>${(report ? ["unresolved", "received", "investigating", "resolved"] : ["not_open", "open", "ended", "cancelled", "suspended"]).map((s) => `<option value="${s}" ${filter === s ? "selected" : ""}>${statuses[s]}</option>`).join("")}</select><select id="sort" aria-label="정렬"><option value="old" ${sort === "old" ? "selected" : ""}>${report ? "접수" : "신청"} 오래된 순</option><option value="new" ${sort === "new" ? "selected" : ""}>${report ? "접수" : "신청"} 최신순</option>${report ? "" : '<option value="event" ' + (sort === "event" ? "selected" : "") + ">개최 임박순</option>"}</select><input type="date" id="from" aria-label="${report ? "접수" : "신청"} 시작일" value="${from}"><input type="date" id="to" aria-label="${report ? "접수" : "신청"} 종료일" value="${to}"><button class="button" id="reset-filter">초기화</button></div>`;
}
function eventsPage() {
  const review = route === "review";
  let list = events.filter((e) =>
    review
      ? ["pending", "reviewing"].includes(e.status)
      : ["approved", "rejected"].includes(e.status),
  );
  if (tab !== "all")
    list = list.filter((e) =>
      tab === "cancelled" ? e.life === "cancelled" : e.status === tab,
    );
  list = list.filter(
    (e) =>
      (e.title + e.id + e.host).toLowerCase().includes(query.toLowerCase()) &&
      (filter === "all" || e.life === filter) &&
      (!from || e.submitted >= from) &&
      (!to || e.submitted <= to),
  );
  list.sort((a, b) =>
    sort === "event"
      ? a.date.localeCompare(b.date)
      : sort === "new"
        ? b.submitted.localeCompare(a.submitted)
        : a.submitted.localeCompare(b.submitted),
  );
  return (
    head(
      pages[route],
      review
        ? "등록된 이벤트를 확인하고 심사 결과를 결정하세요."
        : "판정 결과와 운영 현황을 함께 확인하세요.",
    ) +
    `<section class="panel">${tabs(
      review
        ? [
            ["all", "전체"],
            ["pending", "심사신청"],
            ["reviewing", "심사 중"],
          ]
        : [
            ["all", "전체"],
            ["approved", "승인완료"],
            ["rejected", "불가 판정"],
            ["cancelled", "폐기"],
          ],
    )}${toolbar()}<div class="count">총 <b>${list.length}</b>개 이벤트 · 날짜 필터는 신청일 기준</div>${eventRows(list)}</section>`
  );
}
function reportsPage() {
  let list = reports.filter(
    (r) =>
      (tab === "all" || r.type === tab) &&
      (filter === "all" ||
        (filter === "unresolved"
          ? r.status !== "resolved"
          : r.status === filter)) &&
      (r.id + r.title + r.reason).toLowerCase().includes(query.toLowerCase()) &&
      (!from || r.date >= from) &&
      (!to || r.date <= to),
  );
  list.sort((a, b) =>
    sort === "new"
      ? b.date.localeCompare(a.date)
      : a.date.localeCompare(b.date),
  );
  return (
    head("신고 관리", "접수된 신고를 검토하고 처리 결과를 기록하세요.") +
    `<section class="panel">${tabs([
      ["all", "전체 신고"],
      ["event", "이벤트 신고"],
      ["host", "호스트 신고"],
    ])}${toolbar(true)}<div class="count">총 <b>${list.length}</b>건 · 날짜 필터는 접수일 기준</div><div class="table-wrap"><table><thead><tr><th>신고 내용</th><th>신고 대상</th><th>접수일</th><th>처리 상태</th></tr></thead><tbody>${list.map((r) => `<tr><td><button class="event-link" data-report="${esc(r.id)}">${esc(r.reason)}</button><small>${esc(r.id)}</small></td><td>${esc(r.title)}<small>${r.type === "event" ? "이벤트" : "호스트"}</small></td><td>${esc(r.date)}</td><td>${badge(r.status)}</td></tr>`).join("") || '<tr><td colspan="4" class="empty">조건에 맞는 신고가 없습니다.</td></tr>'}</tbody></table></div></section>`
  );
}
function statsPage() {
  const s = statsData(),
    week = metrics.slice(-7),
    month = metrics.slice(-30);
  return (
    head(
      "통계",
      "기간과 채널별 서비스 이용 현황을 확인하세요.",
      periodControl(),
    ) +
    `<div class="cards">${metric("방문 사용자", s.users.toLocaleString(), "선택 기간의 순 방문자 · 예시")}${metric("활성 사용자", s.active.toLocaleString(), "조회 또는 신청한 사용자 · 예시")}${metric("신규 가입자", s.joins, "선택 기간 · 전체 채널")}${metric("전체 가입자", "2,486", "현재 기준 · 전체 채널")}</div>${charts()}<div class="split"><section class="panel"><div class="panel-head"><h2>사용자 활동</h2><span class="sub">전체 채널</span></div><div class="panel-body"><div class="cards" style="grid-template-columns:repeat(3,1fr)">${metric("일간 활성", activeIn(1).toLocaleString(), shortDate(todayISO))}${metric("주간 활성", activeIn(7).toLocaleString(), `${shortDate(week[0]?.date)}–${shortDate(week.at(-1)?.date)}`)}${metric("월간 활성", activeIn(30).toLocaleString(), `${shortDate(month[0]?.date)}–${shortDate(month.at(-1)?.date)}`)}</div><div class="insight">활성 사용자: 예시에서는 이벤트 조회 또는 참가 신청을 한 사용자로 정의합니다.</div></div></section><section class="panel"><div class="panel-head"><h2>이용량</h2></div><div class="panel-body"><div class="section-label">방문 횟수 <b>${s.visits.toLocaleString()}회</b></div><div class="channel-progress"><span style="width:68%"></span></div><div class="section-label">페이지 조회 수 <b>${(s.visits * 3).toLocaleString()}회</b></div><p class="sub">사용자 수는 중복을 제외하고, 방문과 조회는 반복 활동을 포함합니다.</p></div></section></div>`
  );
}
function loginPage() {
  return `<div class="login"><div class="brand"><span class="logo">e</span>earthuu</div><h1>관리자 로그인</h1><p class="sub">운영 워크스페이스에 접속하세요.</p><form id="login-form"><label for="email">이메일</label><input id="email" type="email" value="admin@earthuu.demo" required autocomplete="username"><label for="password">비밀번호</label><input id="password" type="password" value="earthuu-demo" required autocomplete="current-password"><div id="login-error" class="error" role="alert"></div><button class="primary">데모 계정으로 로그인</button></form><button class="quiet" id="forgot" style="margin-top:12px">비밀번호 재설정</button><div class="login-note">시제품 전용 계정입니다.<br>admin@earthuu.demo / earthuu-demo<br>실제 계정 정보는 입력하지 마세요.</div></div>`;
}
function bind() {
  document.querySelectorAll("#nav a").forEach(
    (a) =>
      (a.onclick = (e) => {
        e.preventDefault();
        go(a.getAttribute("href").slice(1));
      }),
  );
  document
    .querySelectorAll("[data-go]")
    .forEach(
      (b) => (b.onclick = () => go(b.dataset.go, b.dataset.tab || "all")),
    );
  document
    .querySelectorAll("[data-event]")
    .forEach((b) => (b.onclick = () => showEvent(b.dataset.event)));
  document
    .querySelectorAll("[data-report]")
    .forEach((b) => (b.onclick = () => showReport(b.dataset.report)));
  document.querySelectorAll("[data-tab-select]").forEach(
    (b) =>
      (b.onclick = () => {
        tab = b.dataset.tabSelect;
        render();
      }),
  );
  ["sort", "state-filter", "from", "to", "period", "channel"].forEach((id) => {
    if ($("#" + id))
      $("#" + id).onchange = (e) => {
        const v = e.target.value;
        if (id === "sort") sort = v;
        if (id === "state-filter") filter = v;
        if (id === "from") from = v;
        if (id === "to") to = v;
        if (id === "period") period = v;
        if (id === "channel") channel = v;
        render();
      };
  });
  if ($("#search"))
    $("#search").oninput = (e) => {
      query = e.target.value;
      const pos = e.target.selectionStart;
      render();
      $("#search").focus();
      $("#search").setSelectionRange(pos, pos);
    };
  if ($("#reset-filter"))
    $("#reset-filter").onclick = () => {
      query = "";
      filter = "all";
      from = "";
      to = "";
      sort = "old";
      render();
    };
  if ($("#login-form"))
    $("#login-form").onsubmit = (e) => {
      e.preventDefault();
      if (
        $("#email").value === "admin@earthuu.demo" &&
        $("#password").value === "earthuu-demo"
      ) {
        setSession(true);
        go("dashboard");
        notify("데모 계정으로 로그인했습니다.");
      } else
        $("#login-error").textContent = "표시된 데모 계정으로 로그인해 주세요.";
    };
  if ($("#forgot"))
    $("#forgot").onclick = () => {
      openDialog(
        `<div class="detail-head"><h2>비밀번호 재설정</h2><button class="quiet" data-close>닫기</button></div><div class="detail-body"><p>실제 이메일 발송은 연결되지 않았습니다. 데모 비밀번호는 <b>earthuu-demo</b>입니다.</p></div>`,
      );
    };
}
function render() {
  if (!logged) {
    route = "login";
    syncRoute();
  }
  document.body.classList.toggle("signed-out", !logged);
  $("#logout").hidden = !logged;
  $("#current-date").textContent = todayISO.replaceAll("-", ".");
  $("#nav").innerHTML = Object.entries(pages)
    .filter(([k]) => k !== "login")
    .map(
      ([k, v]) =>
        `<a href="#${k}" class="${route === k ? "active" : ""}" ${route === k ? 'aria-current="page"' : ""}>${icon(k)}${v}${k === "review" ? `<span class="nav-count">${events.filter((e) => ["pending", "reviewing"].includes(e.status)).length}</span>` : ""}</a>`,
    )
    .join("");
  $("#crumb").textContent = pages[route];
  $("#main").innerHTML =
    route === "dashboard"
      ? dashboard()
      : route === "review" || route === "events"
        ? eventsPage()
        : route === "reports"
          ? reportsPage()
          : route === "stats"
            ? statsPage()
            : loginPage();
  $("#logout").textContent = logged ? "로그아웃" : "로그인";
  bind();
}
function openDialog(html) {
  $("#detail-content").innerHTML = html;
  if (!$("#detail").open) $("#detail").showModal();
  document
    .querySelectorAll("[data-close]")
    .forEach((b) => (b.onclick = () => $("#detail").close()));
  document
    .querySelectorAll("[data-event]")
    .forEach((b) => (b.onclick = () => showEvent(b.dataset.event)));
}
function renderHistory(items) {
  return (
    items
      .map(
        (h) =>
          `<div class="history">${esc(h.action)}${h.reason ? " · " + esc(h.reason) : ""}<small>${esc(h.at)} · ${esc(h.actor)}</small></div>`,
      )
      .join("") || '<p class="sub">처리 이력이 없습니다.</p>'
  );
}
function showEvent(id) {
  const e = events.find((e) => e.id === id);
  if (!e || !logged) return;
  const participants = participantsByEvent[id] || [];
  openDialog(
    `<div class="detail-head"><div>${badge(e.status)} ${e.status === "approved" ? badge(e.life) : ""}<h2>${esc(e.title)}</h2><p class="sub">${esc(e.id)} · 원문 버전 ${esc(e.version)}</p></div><button class="quiet" data-close aria-label="상세 닫기">닫기 ✕</button></div><div class="detail-body"><div class="detail-grid">${[
      ["호스트", e.host + " · " + e.school],
      ["카테고리", e.cat],
      ["개최일", e.date + " 14:00–17:00"],
      ["장소", e.venue],
      [
        "모집 정원",
        e.capacity + "명 · 한국학생 / 국제학생 각 " + e.capacity / 2 + "명",
      ],
      ["심사신청일", e.submitted],
    ]
      .map(
        ([a, b]) =>
          `<div class="field"><small>${a}</small><b>${esc(b)}</b></div>`,
      )
      .join(
        "",
      )}</div><div class="detail-section"><h3>이벤트 소개 · 등록 원문</h3><p class="description">${esc(e.title)}에 함께할 친구들을 모집합니다. 한국학생과 국제학생이 소규모 팀을 이루어 대화하고 서로의 문화를 알아가는 모임입니다.\n\n진행: 서로 소개하기 → 함께하는 활동 → 자유 교류\n언어: 한국어 / 영어 · 준비물: 편한 복장</p></div>${e.status === "rejected" ? `<div class="insight">불가 사유: ${esc(e.reason)}</div>` : ""}${e.life === "cancelled" ? `<div class="insight">호스트 폐기 기록 · ${esc(e.cancelReason)}<br>기존 신청자 처리·알림 정책은 미정입니다.</div>` : ""}${e.status === "approved" ? `<div class="detail-section"><h3>참가 신청 현황 · ${participants.length}명</h3><p class="sub">참여 승인·반려는 호스트가 처리합니다.</p><div class="table-wrap"><table><thead><tr><th>신청자</th><th>구분</th><th>상태</th></tr></thead><tbody>${participants.map((p) => `<tr><td>${esc(p.name)}</td><td>${esc(p.group)}</td><td>${badge(p.status)}</td></tr>`).join("") || '<tr><td colspan="3" class="empty">아직 참가 신청자가 없습니다. 승인 후부터 참가자를 모집합니다.</td></tr>'}</tbody></table></div></div>` : ""}<div class="detail-section"><h3>심사 및 운영 이력</h3>${renderHistory(e.history)}</div>${e.status === "pending" ? '<div class="insight">심사를 시작하면 호스트의 글 수정이 잠깁니다.</div><div class="detail-actions"><button class="primary" id="start-review">심사 시작</button></div>' : e.status === "reviewing" ? '<div class="detail-section"><label for="reject-reason">불가 사유 · 반려 시 필수</label><input id="reject-reason" maxlength="150" placeholder="호스트에게 보여줄 사유 한 줄" style="width:100%"><p id="decision-error" class="error" role="alert"></p><div class="detail-actions"><button class="danger" id="reject-event">불가 판정</button><button class="primary" id="approve-event">승인</button></div></div>' : '<div class="insight">심사 시작 이후 원문을 수정할 수 없습니다. 승인 후 변경은 폐기 후 신규 심사가 필요합니다.</div>'}</div>`,
  );
  if ($("#start-review"))
    $("#start-review").onclick = () =>
      runOnce($("#start-review"), () => transition(e, "reviewing"));
  if ($("#approve-event"))
    $("#approve-event").onclick = () => confirmDecision(e, "approved");
  if ($("#reject-event"))
    $("#reject-event").onclick = () => {
      const reason = $("#reject-reason").value.trim();
      if (!reason) {
        $("#decision-error").textContent = "불가 사유를 입력해 주세요.";
        return;
      }
      confirmDecision(e, "rejected", reason);
    };
}
function confirmDecision(e, status, reason = "") {
  openDialog(
    `<div class="detail-head"><h2>${status === "approved" ? "이벤트를 승인할까요?" : "불가 판정을 확정할까요?"}</h2><button class="quiet" data-close>닫기</button></div><div class="detail-body"><p>${esc(e.title)}</p><p class="sub" style="margin-top:12px">${status === "approved" ? "승인하면 학생에게 공개되고 참가 신청이 가능해집니다." : "호스트에게 표시할 사유: " + esc(reason)}</p><div class="detail-actions"><button class="button" id="back-detail">돌아가기</button><button class="primary" id="confirm-decision">${status === "approved" ? "승인 확정" : "불가 판정 확정"}</button></div></div>`,
  );
  $("#back-detail").onclick = () => showEvent(e.id);
  $("#confirm-decision").onclick = () =>
    runOnce($("#confirm-decision"), () => transition(e, status, reason));
}
function transition(e, status, reason = "") {
  if (
    (status === "reviewing" && e.status !== "pending") ||
    (["approved", "rejected"].includes(status) && e.status !== "reviewing")
  )
    return;
  e.status = status;
  if (status === "approved") {
    e.life = "open";
    participantsByEvent[e.id] = [];
  }
  if (reason) e.reason = reason;
  e.history.push({
    action: status === "reviewing" ? "심사 시작" : statuses[status],
    at: now(),
    actor: "김관리",
    reason,
  });
  render();
  showEvent(e.id);
  notify(
    status === "reviewing"
      ? "심사를 시작했습니다."
      : statuses[status] + " 처리했습니다.",
  );
}
function showReport(id) {
  const r = reports.find((r) => r.id === id);
  if (!r || !logged) return;
  const e = events.find((e) => e.id === r.target),
    relatedEvent = e
      ? `<button class="event-link" data-event="${esc(e.id)}">${esc(e.title)}</button>`
      : '<span class="sub">연결된 이벤트 없음</span>';
  openDialog(
    `<div class="detail-head"><div>${badge(r.status)}<h2>${esc(r.reason)}</h2><p class="sub">${esc(r.id)} · ${esc(r.date)} 접수</p></div><button class="quiet" data-close>닫기 ✕</button></div><div class="detail-body"><div class="detail-grid"><div class="field"><small>신고 대상</small><b>${r.type === "event" ? "이벤트" : "호스트"} · ${esc(r.title)}</b></div><div class="field"><small>관련 이벤트</small>${relatedEvent}</div></div><div class="detail-section"><h3>신고 내용</h3><p class="description">${esc(r.description)}</p></div><div class="detail-section"><h3>증빙</h3><p class="sub">${esc(r.evidence)}</p></div><div class="detail-section"><h3>처리 이력</h3>${renderHistory(r.history)}</div>${r.status === "received" ? '<div class="detail-actions"><button class="primary" id="start-report">검토 시작</button></div>' : r.status === "investigating" ? `<div class="detail-section"><label for="resolution">조치 결정</label><select id="resolution"><option value="조치 불필요">조치 불필요</option>${e ? '<option value="운영 중단">관련 이벤트 운영 중단 · 데모</option>' : ""}</select><label for="resolution-note">처리 사유 · 필수</label><textarea id="resolution-note" placeholder="확인한 내용과 판단 사유를 입력하세요."></textarea><p class="sub">운영 중단은 관련 이벤트가 있을 때만 선택할 수 있습니다.</p><p class="error" id="report-error" role="alert"></p><div class="detail-actions"><button class="primary" id="resolve-report">처리 결과 확인</button></div></div>` : `<div class="insight">${esc(r.resolution)} · ${esc(r.note)}</div>`}</div>`,
  );
  if ($("#start-report"))
    $("#start-report").onclick = () =>
      runOnce($("#start-report"), () => {
        r.status = "investigating";
        r.history.push({ action: "검토 시작", at: now(), actor: "김관리" });
        render();
        showReport(id);
      });
  if ($("#resolve-report"))
    $("#resolve-report").onclick = () => {
      const note = $("#resolution-note").value.trim(),
        res = $("#resolution").value;
      if (!note) {
        $("#report-error").textContent = "처리 사유를 입력해 주세요.";
        return;
      }
      openDialog(
        `<div class="detail-head"><h2>신고 처리를 완료할까요?</h2><button class="quiet" data-close>닫기</button></div><div class="detail-body"><p><b>${esc(res)}</b></p><p class="description">${esc(note)}</p><div class="detail-actions"><button class="button" id="cancel-resolution">돌아가기</button><button class="primary" id="confirm-resolution">처리 완료</button></div></div>`,
      );
      $("#cancel-resolution").onclick = () => showReport(id);
      $("#confirm-resolution").onclick = () =>
        runOnce($("#confirm-resolution"), () => {
          r.status = "resolved";
          r.resolution = res;
          r.note = note;
          r.history.push({
            action: res,
            reason: note,
            at: now(),
            actor: "김관리",
          });
          if (res === "운영 중단" && e) {
            e.life = "suspended";
            e.history.push({
              action: "신고 대응 · 운영 중단",
              reason: note,
              at: now(),
              actor: "김관리",
            });
          }
          render();
          showReport(id);
          notify("신고 처리 결과를 기록했습니다.");
        });
    };
}
$("#logout").onclick = logout;
$("#account").onclick = () => notify("김관리 · 최고관리자 데모 계정");
const themeButton = document.createElement("button");
themeButton.className = "button";
themeButton.id = "theme";
themeButton.setAttribute("aria-label", "다크모드 전환");
$(".header-right").prepend(themeButton);
let dark = false;
try {
  dark = localStorage.getItem("earthuu-theme") === "dark";
} catch {}
function theme() {
  document.documentElement.dataset.theme = dark ? "dark" : "light";
  themeButton.textContent = dark ? "☀ 라이트" : "☾ 다크";
  themeButton.setAttribute("aria-pressed", String(dark));
  themeButton.setAttribute(
    "aria-label",
    dark ? "라이트모드 전환" : "다크모드 전환",
  );
}
themeButton.onclick = () => {
  dark = !dark;
  theme();
  try {
    localStorage.setItem("earthuu-theme", dark ? "dark" : "light");
  } catch {}
};
theme();
render();
if (document.modelContext?.registerTool) {
  try {
    Promise.resolve(
      document.modelContext.registerTool({
        name: "view_admin_section",
        description:
          "Navigate the Earthuu prototype to an administrator section; does not approve or reject any record.",
        inputSchema: {
          type: "object",
          properties: {
            section: {
              type: "string",
              enum: ["dashboard", "review", "events", "reports", "stats"],
            },
          },
          required: ["section"],
          additionalProperties: false,
        },
        annotations: { readOnlyHint: true },
        execute: async (input) => {
          if (!logged) throw new Error("Demo login required");
          if (
            !["dashboard", "review", "events", "reports", "stats"].includes(
              input?.section,
            )
          )
            throw new Error("Invalid section");
          route = input.section;
          tab = "all";
          query = "";
          filter = "all";
          from = "";
          to = "";
          location.hash = route;
          render();
          return { section: route, title: pages[route] };
        },
      }),
    ).catch(() => {});
  } catch {}
}
