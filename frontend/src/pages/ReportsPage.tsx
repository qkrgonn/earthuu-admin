import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { FilterToolbar, initialFilters, type FilterState } from "../components/common/FilterToolbar";
import { PageHead } from "../components/common/PageHead";
import { StatusBadge } from "../components/common/StatusBadge";
import { Tabs } from "../components/common/Tabs";
import { useAdmin } from "../store/AdminContext";

export function ReportsPage() {
  const { reports, openReport } = useAdmin();
  const [params] = useSearchParams();
  const queryTab = params.get("tab") ?? "all";
  const [tab, setTab] = useState(queryTab);
  const [filters, setFilters] = useState<FilterState>({ ...initialFilters, filter: ["event", "host"].includes(queryTab) ? "unresolved" : "all" });

  useEffect(() => {
    const nextTab = params.get("tab") ?? "all";
    setTab(nextTab);
    setFilters({ ...initialFilters, filter: ["event", "host"].includes(nextTab) ? "unresolved" : "all" });
  }, [params]);

  const list = useMemo(() => [...reports].filter((report) =>
    (tab === "all" || report.type === tab)
    && (filters.filter === "all" || (filters.filter === "unresolved" ? report.status !== "resolved" : report.status === filters.filter))
    && `${report.id}${report.title}${report.reason}`.toLowerCase().includes(filters.query.toLowerCase())
    && (!filters.from || report.date >= filters.from)
    && (!filters.to || report.date <= filters.to))
    .sort((first, second) => filters.sort === "new" ? second.date.localeCompare(first.date) : first.date.localeCompare(second.date)), [filters, reports, tab]);

  return <>
    <PageHead title="신고 관리" description="접수된 신고를 검토하고 처리 결과를 기록하세요." />
    <section className="panel">
      <Tabs items={[{ value: "all", label: "전체 신고" }, { value: "event", label: "이벤트 신고" }, { value: "host", label: "호스트 신고" }]} value={tab} onChange={setTab} />
      <FilterToolbar report value={filters} onChange={setFilters} onReset={() => setFilters(initialFilters)} />
      <div className="count">총 <b>{list.length}</b>건 · 날짜 필터는 접수일 기준</div>
      <div className="table-wrap"><table>
        <thead><tr><th>신고 내용</th><th>신고 대상</th><th>접수일</th><th>처리 상태</th></tr></thead>
        <tbody>{list.length ? list.map((report) => <tr key={report.id}>
          <td><button className="event-link" onClick={() => openReport(report.id)}>{report.reason}</button><small>{report.id}</small></td>
          <td>{report.title}<small>{report.type === "event" ? "이벤트" : "호스트"}</small></td>
          <td>{report.date}</td><td><StatusBadge status={report.status} /></td>
        </tr>) : <tr><td colSpan={4} className="empty">조건에 맞는 신고가 없습니다.</td></tr>}</tbody>
      </table></div>
    </section>
  </>;
}
