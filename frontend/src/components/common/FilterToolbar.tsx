import { STATUS_LABELS } from "../../constants/status";

export interface FilterState {
  query: string;
  filter: string;
  sort: string;
  from: string;
  to: string;
}

export const initialFilters: FilterState = { query: "", filter: "all", sort: "old", from: "", to: "" };

export function FilterToolbar({ report = false, value, onChange, onReset }: {
  report?: boolean;
  value: FilterState;
  onChange: (value: FilterState) => void;
  onReset: () => void;
}) {
  const states = report
    ? ["unresolved", "received", "investigating", "resolved"]
    : ["not_open", "open", "ended", "cancelled", "suspended"];
  const set = (key: keyof FilterState, next: string) => onChange({ ...value, [key]: next });
  return <div className="toolbar">
    <input
      aria-label={`${report ? "신고" : "이벤트"} 검색`}
      placeholder={report ? "신고 ID, 대상, 사유 검색" : "이벤트명, ID, 호스트 검색"}
      value={value.query}
      onChange={(event) => set("query", event.target.value)}
    />
    <select aria-label={`${report ? "처리" : "운영"} 상태`} value={value.filter} onChange={(event) => set("filter", event.target.value)}>
      <option value="all">{report ? "모든 처리 상태" : "모든 운영 상태"}</option>
      {states.map((state) => <option value={state} key={state}>{STATUS_LABELS[state]}</option>)}
    </select>
    <select aria-label="정렬" value={value.sort} onChange={(event) => set("sort", event.target.value)}>
      <option value="old">{report ? "접수" : "신청"} 오래된 순</option>
      <option value="new">{report ? "접수" : "신청"} 최신순</option>
      {!report && <option value="event">개최 임박순</option>}
    </select>
    <input type="date" aria-label={`${report ? "접수" : "신청"} 시작일`} value={value.from} onChange={(event) => set("from", event.target.value)} />
    <input type="date" aria-label={`${report ? "접수" : "신청"} 종료일`} value={value.to} onChange={(event) => set("to", event.target.value)} />
    <button className="button" onClick={onReset}>초기화</button>
  </div>;
}
