import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { MetricCard } from "../components/common/MetricCard";
import { PageHead } from "../components/common/PageHead";
import { StatusBadge } from "../components/common/StatusBadge";
import { EventTable } from "../components/events/EventTable";
import { Charts } from "../components/stats/Charts";
import { PeriodControl } from "../components/stats/PeriodControl";
import { useAdmin } from "../store/AdminContext";
import type { Channel, Period } from "../types";
import { todayLabel } from "../utils/date";
import { getStats } from "../utils/stats";

export function DashboardPage() {
  const { events, reports, openReport } = useAdmin();
  const navigate = useNavigate();
  const [period, setPeriod] = useState<Period>("7");
  const [channel, setChannel] = useState<Channel>("all");
  const stats = getStats(period, channel);
  const pending = events.filter((event) => event.status === "pending");
  const reviewing = events.filter((event) => event.status === "reviewing");
  const openReports = reports.filter((report) => report.status !== "resolved");
  const go = (path: string, tab?: string) => navigate(tab ? `${path}?tab=${tab}` : path);

  return <>
    <PageHead title="대시보드" description="오늘의 운영 현황을 확인하세요." extra={<span className="sub">{todayLabel}</span>} />
    <div className="section-label">처리할 업무<small>현재 기준 · 기간 필터와 무관</small></div>
    <div className="cards">
      <MetricCard label="심사신청" value={pending.length} note="검토를 기다리는 이벤트" blue onClick={() => go("/review", "pending")} />
      <MetricCard label="심사 중" value={reviewing.length} note="진행 중인 심사 확인" onClick={() => go("/review", "reviewing")} />
      <MetricCard label="이벤트 신고" value={openReports.filter((report) => report.type === "event").length} note="미처리 신고" onClick={() => go("/reports", "event")} />
      <MetricCard label="호스트 신고" value={openReports.filter((report) => report.type === "host").length} note="미처리 신고" onClick={() => go("/reports", "host")} />
    </div>
    <div className="section-label section-with-filters"><span>서비스 이용 현황</span><PeriodControl period={period} channel={channel} onPeriod={setPeriod} onChannel={setChannel} /></div>
    <div className="cards">
      <MetricCard label="방문 사용자" value={stats.users.toLocaleString()} note="기간 내 중복 제외 · 예시" />
      <MetricCard label="활성 사용자" value={stats.active.toLocaleString()} note="조회·신청 활동 기준 · 예시" />
      <MetricCard label="신규 가입자" value={stats.joins} note="선택 기간 내 가입 · 전체 채널" />
      <MetricCard label="전체 가입자" value="2,486" note="현재 기준 · 전체 채널" />
    </div>
    <Charts period={period} channel={channel} />
    <div className="split">
      <section className="panel">
        <div className="panel-head"><div><h2>심사를 기다리고 있어요 <span className="sub">{pending.length}</span></h2><p>오래 기다린 신청부터 표시합니다.</p></div><button className="link" onClick={() => go("/review")}>전체 보기</button></div>
        <EventTable events={pending.slice(0, 3)} compact />
      </section>
      <section className="panel">
        <div className="panel-head"><h2>미처리 신고 <span className="sub">{openReports.length}</span></h2><button className="link" onClick={() => go("/reports")}>전체 보기</button></div>
        {openReports.length ? openReports.slice(0, 3).map((report) => <div className="report-item" key={report.id}>
          <span className="report-icon">!</span><div className="text"><button className="event-link" onClick={() => openReport(report.id)}>{report.reason}</button><p>{report.type === "event" ? "이벤트" : "호스트"} · {report.title}</p></div><StatusBadge status={report.status} />
        </div>) : <div className="empty">미처리 신고가 없습니다.</div>}
      </section>
    </div>
  </>;
}
