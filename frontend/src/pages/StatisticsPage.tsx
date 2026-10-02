import { useState } from "react";
import { MetricCard } from "../components/common/MetricCard";
import { PageHead } from "../components/common/PageHead";
import { Charts } from "../components/stats/Charts";
import { PeriodControl } from "../components/stats/PeriodControl";
import { metrics } from "../mock/data";
import type { Channel, Period } from "../types";
import { shortDate, todayISO } from "../utils/date";
import { activeIn, getStats } from "../utils/stats";

export function StatisticsPage() {
  const [period, setPeriod] = useState<Period>("7");
  const [channel, setChannel] = useState<Channel>("all");
  const stats = getStats(period, channel);
  const week = metrics.slice(-7);
  const month = metrics.slice(-30);

  return <>
    <PageHead title="통계" description="기간과 채널별 서비스 이용 현황을 확인하세요." extra={<PeriodControl period={period} channel={channel} onPeriod={setPeriod} onChannel={setChannel} />} />
    <div className="cards">
      <MetricCard label="방문 사용자" value={stats.users.toLocaleString()} note="선택 기간의 순 방문자 · 예시" />
      <MetricCard label="활성 사용자" value={stats.active.toLocaleString()} note="조회 또는 신청한 사용자 · 예시" />
      <MetricCard label="신규 가입자" value={stats.joins} note="선택 기간 · 전체 채널" />
      <MetricCard label="전체 가입자" value="2,486" note="현재 기준 · 전체 채널" />
    </div>
    <Charts period={period} channel={channel} />
    <div className="split">
      <section className="panel">
        <div className="panel-head"><h2>사용자 활동</h2><span className="sub">전체 채널</span></div>
        <div className="panel-body">
          <div className="cards activity-cards">
            <MetricCard label="일간 활성" value={activeIn(1).toLocaleString()} note={shortDate(todayISO)} />
            <MetricCard label="주간 활성" value={activeIn(7).toLocaleString()} note={`${shortDate(week[0]?.date)}–${shortDate(week.at(-1)?.date)}`} />
            <MetricCard label="월간 활성" value={activeIn(30).toLocaleString()} note={`${shortDate(month[0]?.date)}–${shortDate(month.at(-1)?.date)}`} />
          </div>
          <div className="insight">활성 사용자: 예시에서는 이벤트 조회 또는 참가 신청을 한 사용자로 정의합니다.</div>
        </div>
      </section>
      <section className="panel"><div className="panel-head"><h2>이용량</h2></div><div className="panel-body">
        <div className="section-label">방문 횟수 <b>{stats.visits.toLocaleString()}회</b></div><div className="channel-progress"><span style={{ width: "68%" }} /></div>
        <div className="section-label">페이지 조회 수 <b>{(stats.visits * 3).toLocaleString()}회</b></div>
        <p className="sub">사용자 수는 중복을 제외하고, 방문과 조회는 반복 활동을 포함합니다.</p>
      </div></section>
    </div>
  </>;
}
