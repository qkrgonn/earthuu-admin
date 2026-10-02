import type { Channel, Period } from "../../types";
import { shortDate } from "../../utils/date";
import { chartItems, getStats } from "../../utils/stats";

export function Charts({ period, channel }: { period: Period; channel: Channel }) {
  const { items, visits } = getStats(period, channel);
  const displayItems = chartItems(items);
  const app = items.reduce((total, item) => total + item.app, 0);
  const web = items.reduce((total, item) => total + item.web, 0);
  const percent = channel === "web" ? 0 : channel === "app" ? 100 : Math.round((app / (app + web)) * 100);
  const range = `${shortDate(items[0]?.date)}–${shortDate(items.at(-1)?.date)}`;

  return <div className="split">
    <section className="panel">
      <div className="panel-head">
        <div><h2>이용 추이</h2><p>방문 횟수 · {period === "30" ? "5일 평균" : "일별"} · {range}</p></div>
        <div className="legend"><span><i />웹</span><span><i className="alt" />앱</span></div>
      </div>
      <div className="panel-body">
        <div className="chart" role="img" aria-label="선택 기간의 일별 웹과 앱 방문 횟수 예시">
          <div className="axis"><span>200</span><span>150</span><span>100</span><span>50</span><span>0</span></div>
          <div className="bars">{displayItems.map((item) => <div className="bar-group" key={item.date}>
            {channel !== "app" && <div className="bar" title={`웹 ${Math.round(item.web)}회`} style={{ height: `${item.web / 2}%` }} />}
            {channel !== "web" && <div className="bar alt" title={`앱 ${Math.round(item.app)}회`} style={{ height: `${item.app / 2}%` }} />}
            <span>{item.date.length > 8 ? item.date.slice(5).replace("-", "/") : item.date}</span>
          </div>)}</div>
        </div>
      </div>
    </section>
    <section className="panel">
      <div className="panel-head"><div><h2>접속 채널</h2><p>선택 기간의 방문 횟수 기준</p></div></div>
      <div className="panel-body channel">
        <div className="donut" style={{ background: `conic-gradient(var(--blue) 0 ${percent}%, #cbd8ff ${percent}% 100%)` }}>
          <div className="donut-inner"><small>전체 방문</small><b>{visits.toLocaleString()}</b></div>
        </div>
        <div className="channel-list"><div>앱 <b>{percent}%</b></div><div>웹 <b>{100 - percent}%</b></div><small className="sub">예시 집계</small></div>
      </div>
    </section>
  </div>;
}
