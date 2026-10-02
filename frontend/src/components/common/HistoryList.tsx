import type { HistoryItem } from "../../types";

export function HistoryList({ items }: { items: HistoryItem[] }) {
  if (!items.length) return <p className="sub">처리 이력이 없습니다.</p>;
  return <>{items.map((item, index) => <div className="history" key={`${item.at}-${index}`}>
    {item.action}{item.reason ? ` · ${item.reason}` : ""}<small>{item.at} · {item.actor}</small>
  </div>)}</>;
}
