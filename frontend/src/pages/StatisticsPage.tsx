import { useCallback, useEffect, useMemo, useState } from "react";
import { ApiError } from "../api/client";
import { statisticsApi, type EventStatistics, type ReportStatistics, type StatisticsOverview, type StatisticsQuery, type TimePoint, type UserStatistics } from "../api/statisticsApi";
import { MetricCard } from "../components/common/MetricCard";
import { PageHead } from "../components/common/PageHead";
import { todayISO } from "../utils/date";

type Range = "7" | "30" | "90";
const initialOverview: StatisticsOverview = { from: todayISO, to: todayISO, newUsers: 0, newEvents: 0, receivedReports: 0, verificationRequests: 0 };
const initialUsers: UserStatistics = { registrations: [], currentStatusCounts: {} };
const initialEvents: EventStatistics = { created: [], approved: [], rejected: [] };
const initialReports: ReportStatistics = { received: [], resolved: [] };

function fromDate(days: number) {
  const date = new Date(`${todayISO}T00:00:00+09:00`);
  date.setDate(date.getDate() - days + 1);
  return new Intl.DateTimeFormat("en-CA", { timeZone: "Asia/Seoul", year: "numeric", month: "2-digit", day: "2-digit" }).format(date);
}

function Trend({ title, series }: { title: string; series: { label: string; items: TimePoint[]; className?: string }[] }) {
  const max = Math.max(1, ...series.flatMap((item) => item.items.map((point) => point.count)));
  const periods = series[0]?.items.map((point) => point.period) ?? [];
  return <section className="panel trend-panel"><div className="panel-head"><div><h2>{title}</h2><p>선택한 기간의 실제 DB 집계</p></div><div className="legend">{series.map((item) => <span key={item.label}><i className={item.className ?? ""} />{item.label}</span>)}</div></div><div className="panel-body"><div className="trend-chart" role="img" aria-label={`${title} 막대 그래프`}>
    {periods.length ? periods.map((period, index) => <div className="trend-group" key={period}><div className="trend-bars">{series.map((item) => <span key={item.label} className={item.className ?? ""} style={{ height: `${Math.max(3, ((item.items[index]?.count ?? 0) / max) * 100)}%` }} title={`${item.label} ${item.items[index]?.count ?? 0}건`} />)}</div><small>{period.slice(5).replace("-", ".")}</small></div>) : <div className="empty">집계할 데이터가 없습니다.</div>}
  </div></div></section>;
}

export function StatisticsPage() {
  const [range, setRange] = useState<Range>("30");
  const [granularity, setGranularity] = useState<StatisticsQuery["granularity"]>("DAY");
  const [overview, setOverview] = useState(initialOverview);
  const [users, setUsers] = useState(initialUsers);
  const [events, setEvents] = useState(initialEvents);
  const [reports, setReports] = useState(initialReports);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const query = useMemo<StatisticsQuery>(() => ({ from: fromDate(Number(range)), to: todayISO, granularity }), [granularity, range]);

  const load = useCallback(async () => {
    setLoading(true); setError("");
    try {
      const [overviewResult, userResult, eventResult, reportResult] = await Promise.all([
        statisticsApi.overview(query), statisticsApi.users(query), statisticsApi.events(query), statisticsApi.reports(query),
      ]);
      setOverview(overviewResult); setUsers(userResult); setEvents(eventResult); setReports(reportResult);
    } catch (caught) { setError(caught instanceof ApiError ? caught.message : "통계를 불러오지 못했습니다."); }
    finally { setLoading(false); }
  }, [query]);
  useEffect(() => { void load(); }, [load]);

  const totalUsers = Object.values(users.currentStatusCounts).reduce((sum, count) => sum + count, 0);
  return <>
    <PageHead title="통계" description="사용자·이벤트·신고 데이터를 기간별로 조회하세요." extra={<div className="filters"><select aria-label="조회 기간" value={range} onChange={(event) => setRange(event.target.value as Range)}><option value="7">최근 7일</option><option value="30">최근 30일</option><option value="90">최근 90일</option></select><select aria-label="집계 단위" value={granularity} onChange={(event) => setGranularity(event.target.value as StatisticsQuery["granularity"])}><option value="DAY">일별</option><option value="WEEK">주별</option><option value="MONTH">월별</option></select><button className="button" disabled={loading} onClick={() => void load()}>{loading ? "집계 중…" : "새로고침"}</button></div>} />
    {error && <p className="api-error page-api-error" role="alert">{error}</p>}
    <div className="cards"><MetricCard label="신규 사용자" value={overview.newUsers.toLocaleString()} note={`${overview.from}–${overview.to}`} unit="명" /><MetricCard label="신규 이벤트" value={overview.newEvents.toLocaleString()} note="선택 기간 등록" unit="건" /><MetricCard label="접수 신고" value={overview.receivedReports.toLocaleString()} note="선택 기간 접수" unit="건" /><MetricCard label="학생 인증" value={overview.verificationRequests.toLocaleString()} note="선택 기간 신청" unit="건" /></div>
    <div className="split"><Trend title="사용자 가입 추이" series={[{ label: "가입", items: users.registrations }]} /><section className="panel"><div className="panel-head"><div><h2>현재 사용자 상태</h2><p>전체 계정 기준</p></div></div><div className="panel-body status-summary"><strong>{totalUsers.toLocaleString()}명</strong>{Object.entries(users.currentStatusCounts).length ? Object.entries(users.currentStatusCounts).map(([status, count]) => <div key={status}><span>{status === "ACTIVE" ? "활성" : status === "SUSPENDED" ? "정지" : status === "WITHDRAWN" ? "탈퇴" : status}</span><b>{count.toLocaleString()}명</b></div>) : <p className="sub">사용자 데이터가 없습니다.</p>}</div></section></div>
    <div className="split"><Trend title="이벤트 운영 추이" series={[{ label: "등록", items: events.created }, { label: "승인", items: events.approved, className: "alt" }, { label: "반려", items: events.rejected, className: "danger-bar" }]} /><Trend title="신고 처리 추이" series={[{ label: "접수", items: reports.received }, { label: "처리", items: reports.resolved, className: "alt" }]} /></div>
  </>;
}
