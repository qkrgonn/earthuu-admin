import type { Channel, Period } from "../../types";

export function PeriodControl({ period, channel, onPeriod, onChannel }: {
  period: Period;
  channel: Channel;
  onPeriod: (period: Period) => void;
  onChannel: (channel: Channel) => void;
}) {
  return <div className="filters">
    <select aria-label="조회 기간" value={period} onChange={(event) => onPeriod(event.target.value as Period)}>
      <option value="7">최근 7일</option><option value="30">최근 30일</option><option value="1">오늘</option>
    </select>
    <select aria-label="접속 채널" value={channel} onChange={(event) => onChannel(event.target.value as Channel)}>
      <option value="all">전체 채널</option><option value="web">웹</option><option value="app">앱</option>
    </select>
  </div>;
}
