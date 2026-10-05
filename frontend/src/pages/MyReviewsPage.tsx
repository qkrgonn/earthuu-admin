import { useEffect, useState } from "react";
import { eventApi, type ApiReviewedEvent } from "../api/eventApi";
import { PageHead } from "../components/common/PageHead";
import { StatusBadge } from "../components/common/StatusBadge";
import { useAdmin } from "../store/AdminContext";

const reviewedAt = (value: string) => new Date(value).toLocaleString("ko-KR", {
  timeZone: "Asia/Seoul",
  year: "numeric",
  month: "2-digit",
  day: "2-digit",
  hour: "2-digit",
  minute: "2-digit",
});

export function MyReviewsPage() {
  const { openEvent, openReport, reports, session } = useAdmin();
  const [events, setEvents] = useState<ApiReviewedEvent[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const load = async () => {
    setLoading(true);
    setError("");
    try {
      setEvents(await eventApi.reviewedByMe());
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "심사 내역을 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { void load(); }, []);

  const approvedCount = events.filter((event) => event.decision === "APPROVED").length;
  const rejectedCount = events.filter((event) => event.decision === "REJECTED").length;
  const handledReports = reports.filter((report) => report.status === "resolved");

  return <>
    <PageHead
      title="내 관리자 활동"
      description="계정 권한, 이벤트 심사와 신고 처리 이력을 한곳에서 확인합니다."
    />
    <div className="activity-overview">
      <section className="panel activity-account" aria-labelledby="account-heading">
        <div className="activity-card-label">ACCOUNT</div>
        <div className="activity-account-head">
          <span className="account-avatar" aria-hidden="true">A</span>
          <div>
            <h2 id="account-heading">관리자</h2>
            <p>{session?.email ?? "계정 정보를 확인할 수 없습니다."}</p>
          </div>
        </div>
        <dl className="activity-account-meta">
          <div><dt>권한</dt><dd><span className="role-badge">ADMIN</span></dd></div>
          <div><dt>계정 ID</dt><dd title={session?.id}>{session?.id ?? "—"}</dd></div>
        </dl>
      </section>

      <section className="panel activity-permissions" aria-labelledby="permission-heading">
        <div className="activity-section-head">
          <div><div className="activity-card-label">PERMISSIONS</div><h2 id="permission-heading">권한 및 접근 범위</h2></div>
          <span className="access-badge"><i />정상</span>
        </div>
        <div className="permission-grid">
          {["관리자 API 접근", "이벤트 조회", "심사 시작", "승인 및 반려"].map((permission) =>
            <div key={permission}><span aria-hidden="true">✓</span>{permission}</div>)}
        </div>
      </section>
    </div>

    <section className="panel activity-history">
      <div className="activity-history-head">
        <div><div className="activity-card-label">EVENT REVIEW</div><h2>이벤트 심사 내역</h2></div>
        <div className="activity-history-actions">
          <span>전체 <b>{events.length}</b></span><span className="approved-count">승인 {approvedCount}</span><span className="rejected-count">반려 {rejectedCount}</span>
          <button className="button" disabled={loading} onClick={() => void load()}>{loading ? "불러오는 중…" : "새로고침"}</button>
        </div>
      </div>
      {error && <p className="api-error" role="alert">{error}</p>}
      {loading && !events.length ? <div className="activity-empty"><div className="empty-icon">↻</div><b>심사 내역을 불러오고 있습니다.</b></div>
        : events.length ? <div className="table-wrap"><table>
        <thead><tr><th>이벤트</th><th>호스트</th><th>심사 결과</th><th>심사 일시</th><th>사유</th></tr></thead>
        <tbody>{events.map((event) => <tr key={event.id}>
          <td><button className="event-link" onClick={() => openEvent(event.id)}>{event.title}</button><small>{event.id}</small></td>
          <td>{event.hostName ?? "호스트 정보 없음"}<small>{event.universityName ?? "소속 정보 없음"}</small></td>
          <td><StatusBadge status={event.decision.toLowerCase()} /></td>
          <td>{reviewedAt(event.reviewedAt)}</td>
          <td>{event.reason ?? "—"}</td>
        </tr>)}</tbody>
      </table></div> : <div className="activity-empty">
        <div className="empty-icon">✓</div>
        <b>아직 완료한 심사가 없습니다.</b>
        <p>이벤트를 승인하거나 반려하면 이곳에 심사 이력이 표시됩니다.</p>
      </div>}
    </section>

    <section className="panel activity-history report-activity-history">
      <div className="activity-history-head">
        <div><div className="activity-card-label">REPORT ACTION</div><h2>신고 처리 내역</h2></div>
        <div className="activity-history-actions">
          <span>처리 완료 <b>{handledReports.length}</b></span>
          <span className="demo-data-badge">현재 데모 데이터</span>
        </div>
      </div>
      {handledReports.length ? <div className="table-wrap"><table>
        <thead><tr><th>신고 내용</th><th>신고 대상</th><th>조치 결과</th><th>처리 사유</th></tr></thead>
        <tbody>{handledReports.map((report) => <tr key={report.id}>
          <td><button className="event-link" onClick={() => openReport(report.id)}>{report.reason}</button><small>{report.id} · {report.date}</small></td>
          <td>{report.title}<small>{report.type === "event" ? "이벤트" : "호스트"}</small></td>
          <td>{report.resolution ?? "처리 완료"}</td>
          <td>{report.note ?? "—"}</td>
        </tr>)}</tbody>
      </table></div> : <div className="activity-empty">
        <div className="empty-icon">!</div>
        <b>아직 처리 완료한 신고가 없습니다.</b>
        <p>신고 관리에서 조치를 완료하면 이곳에 처리 이력이 표시됩니다.</p>
      </div>}
    </section>
  </>;
}
