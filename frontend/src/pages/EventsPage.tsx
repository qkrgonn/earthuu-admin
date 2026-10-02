import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { FilterToolbar, initialFilters, type FilterState } from "../components/common/FilterToolbar";
import { PageHead } from "../components/common/PageHead";
import { Tabs } from "../components/common/Tabs";
import { EventTable } from "../components/events/EventTable";
import { useAdmin } from "../store/AdminContext";

export function EventsPage({ mode }: { mode: "review" | "management" }) {
  const { events, eventsLoading, eventError, refreshEvents } = useAdmin();
  const [params] = useSearchParams();
  const [tab, setTab] = useState(params.get("tab") ?? "all");
  const [filters, setFilters] = useState<FilterState>(initialFilters);
  const reviewing = mode === "review";

  useEffect(() => {
    setTab(params.get("tab") ?? "all");
    setFilters(initialFilters);
  }, [mode, params]);

  const list = useMemo(() => {
    let result = events.filter((event) => reviewing
      ? ["pending", "reviewing"].includes(event.status)
      : ["approved", "rejected"].includes(event.status));
    if (tab !== "all") result = result.filter((event) => tab === "cancelled" ? event.life === "cancelled" : event.status === tab);
    result = result.filter((event) =>
      `${event.title}${event.id}${event.host}`.toLowerCase().includes(filters.query.toLowerCase())
      && (filters.filter === "all" || event.life === filters.filter)
      && (!filters.from || event.submitted >= filters.from)
      && (!filters.to || event.submitted <= filters.to));
    return [...result].sort((first, second) => filters.sort === "event"
      ? first.date.localeCompare(second.date)
      : filters.sort === "new"
        ? second.submitted.localeCompare(first.submitted)
        : first.submitted.localeCompare(second.submitted));
  }, [events, filters, reviewing, tab]);

  const tabs = reviewing
    ? [{ value: "all", label: "전체" }, { value: "pending", label: "심사신청" }, { value: "reviewing", label: "심사 중" }]
    : [{ value: "all", label: "전체" }, { value: "approved", label: "승인완료" }, { value: "rejected", label: "불가 판정" }, { value: "cancelled", label: "폐기" }];

  return <>
    <PageHead
      title={reviewing ? "이벤트 심사" : "이벤트 관리"}
      description={reviewing ? "등록된 이벤트를 확인하고 심사 결과를 결정하세요." : "판정 결과와 운영 현황을 함께 확인하세요."}
      extra={<button className="button" disabled={eventsLoading} onClick={() => void refreshEvents()}>{eventsLoading ? "불러오는 중…" : "새로고침"}</button>}
    />
    <section className="panel">
      <Tabs items={tabs} value={tab} onChange={setTab} />
      <FilterToolbar value={filters} onChange={setFilters} onReset={() => setFilters(initialFilters)} />
      {eventError && <p className="api-error" role="alert">{eventError}</p>}
      <div className="count">총 <b>{list.length}</b>개 이벤트 · 날짜 필터는 신청일 기준</div>
      {eventsLoading && !events.length ? <div className="empty">이벤트를 불러오고 있습니다.</div> : <EventTable events={list} />}
    </section>
  </>;
}
