import { useCallback, useEffect, useState, type FormEvent } from "react";
import { ApiError } from "../api/client";
import { verificationApi, type VerificationDetail, type VerificationPage, type VerificationStatus } from "../api/verificationApi";
import { Modal } from "../components/common/Modal";
import { PageHead } from "../components/common/PageHead";
import { StatusBadge } from "../components/common/StatusBadge";
import { Tabs } from "../components/common/Tabs";
import { useAdmin } from "../store/AdminContext";

type Filter = "ALL" | VerificationStatus;
const emptyPage: VerificationPage = { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0, first: true, last: true };
const dateTime = (value?: string | null) => value ? new Date(value).toLocaleString("ko-KR", { timeZone: "Asia/Seoul" }) : "-";

function VerificationModal({ id, onClose, onChanged }: { id: string; onClose: () => void; onChanged: () => Promise<void> }) {
  const { notify } = useAdmin();
  const [detail, setDetail] = useState<VerificationDetail | null>(null);
  const [reason, setReason] = useState("");
  const [error, setError] = useState("");
  const [pending, setPending] = useState(false);

  useEffect(() => {
    void verificationApi.detail(id).then(setDetail).catch((caught) => setError(caught instanceof ApiError ? caught.message : "인증 상세를 불러오지 못했습니다."));
  }, [id]);

  const action = async (kind: "approve" | "reject") => {
    if (kind === "reject" && !reason.trim()) { setError("반려 사유를 입력해 주세요."); return; }
    setPending(true); setError("");
    try {
      if (kind === "approve") await verificationApi.approve(id);
      else await verificationApi.reject(id, reason.trim());
      notify(kind === "approve" ? "학생 인증을 승인했습니다." : "학생 인증을 반려했습니다.");
      await onChanged();
      onClose();
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "인증 처리에 실패했습니다.");
    } finally { setPending(false); }
  };

  return <Modal onClose={onClose}>
    <div className="detail-head"><div><h2>학생 인증 상세</h2><p>{detail?.schoolEmail ?? "불러오는 중…"}</p></div><button className="quiet" onClick={onClose}>닫기</button></div>
    <div className="detail-body">
      {error && <p className="api-error user-detail-error" role="alert">{error}</p>}
      {!detail ? <div className="empty">인증 정보를 불러오고 있습니다.</div> : <>
        <div className="user-summary-card"><span className="user-avatar">{(detail.userName ?? "U").slice(0, 1)}</span><div><b>{detail.userName ?? "이름 정보 없음"}</b><span>{detail.loginEmail ?? detail.userId}</span></div><StatusBadge status={detail.status.toLowerCase()} /></div>
        <div className="detail-grid user-detail-grid">
          <div className="field"><small>학교</small><b>{detail.universityName ?? "-"}</b></div>
          <div className="field"><small>학교 이메일</small><b>{detail.schoolEmail}</b></div>
          <div className="field"><small>이메일 확인</small><b>{dateTime(detail.emailVerifiedAt)}</b></div>
          <div className="field"><small>신청일</small><b>{dateTime(detail.createdAt)}</b></div>
          <div className="field"><small>처리일</small><b>{dateTime(detail.reviewedAt)}</b></div>
          <div className="field"><small>반려 사유</small><b>{detail.rejectionReason ?? "-"}</b></div>
        </div>
        {detail.status === "PENDING" && <div className="user-action-box">
          <h3>인증 심사</h3><p>학교 이메일 확인 상태와 신청 정보를 확인한 후 승인하거나 반려하세요.</p>
          <label htmlFor="verification-reason">반려 사유</label>
          <textarea id="verification-reason" rows={3} maxLength={1000} value={reason} onChange={(event) => setReason(event.target.value)} placeholder="반려할 경우 사유를 입력하세요." />
          <div className="action-row"><button className="danger" disabled={pending} onClick={() => void action("reject")}>반려</button><button className="primary" disabled={pending || !detail.emailVerifiedAt} onClick={() => void action("approve")}>승인</button></div>
        </div>}
      </>}
    </div>
  </Modal>;
}

export function VerificationsPage() {
  const [result, setResult] = useState(emptyPage);
  const [status, setStatus] = useState<Filter>("ALL");
  const [queryInput, setQueryInput] = useState("");
  const [query, setQuery] = useState("");
  const [sort, setSort] = useState<"NEWEST" | "OLDEST">("NEWEST");
  const [page, setPage] = useState(0);
  const [selected, setSelected] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    setLoading(true); setError("");
    try { setResult(await verificationApi.list({ query: query || undefined, status: status === "ALL" ? undefined : status, sort, page, size: 20 })); }
    catch (caught) { setError(caught instanceof ApiError ? caught.message : "학생 인증 목록을 불러오지 못했습니다."); }
    finally { setLoading(false); }
  }, [page, query, sort, status]);
  useEffect(() => { void load(); }, [load]);

  const search = (event: FormEvent) => { event.preventDefault(); setQuery(queryInput.trim()); setPage(0); };
  return <>
    <PageHead title="학생 인증 관리" description="학교 이메일 인증 신청을 확인하고 승인 또는 반려하세요." extra={<button className="button" disabled={loading} onClick={() => void load()}>{loading ? "불러오는 중…" : "새로고침"}</button>} />
    <section className="panel">
      <Tabs items={[{ value: "ALL", label: "전체" }, { value: "PENDING", label: "승인 대기" }, { value: "APPROVED", label: "승인" }, { value: "REJECTED", label: "반려" }]} value={status} onChange={(value) => { setStatus(value as Filter); setPage(0); }} />
      <form className="toolbar user-toolbar" onSubmit={search}><input aria-label="학생 인증 검색" placeholder="이름, 이메일 검색" value={queryInput} onChange={(event) => setQueryInput(event.target.value)} /><select value={sort} onChange={(event) => { setSort(event.target.value as "NEWEST" | "OLDEST"); setPage(0); }}><option value="NEWEST">최근 신청순</option><option value="OLDEST">오래된 신청순</option></select><button className="primary">검색</button></form>
      {error && <p className="api-error" role="alert">{error}</p>}
      <div className="count">총 <b>{result.totalElements}</b>건 · 행을 누르면 신청 상세를 확인할 수 있습니다.</div>
      <div className="table-wrap"><table><thead><tr><th>신청자</th><th>학교 이메일</th><th>대학교</th><th>신청일</th><th>상태</th></tr></thead><tbody>
        {loading && !result.content.length ? <tr><td colSpan={5} className="empty">학생 인증 목록을 불러오고 있습니다.</td></tr> : result.content.length ? result.content.map((item) => <tr key={item.id} className="clickable-row" onClick={() => setSelected(item.id)}><td><button className="event-link" onClick={() => setSelected(item.id)}>{item.userName ?? "이름 정보 없음"}</button><small>{item.loginEmail ?? item.userId}</small></td><td>{item.schoolEmail}<small>{item.emailVerifiedAt ? "이메일 확인 완료" : "이메일 미확인"}</small></td><td>{item.universityName ?? "-"}</td><td>{dateTime(item.createdAt)}</td><td><StatusBadge status={item.status.toLowerCase()} /></td></tr>) : <tr><td colSpan={5} className="empty">조건에 맞는 인증 신청이 없습니다.</td></tr>}
      </tbody></table></div>
      {result.totalPages > 1 && <div className="user-pagination"><button className="button" disabled={result.first || loading} onClick={() => setPage((value) => value - 1)}>이전</button><span><b>{result.page + 1}</b> / {result.totalPages}</span><button className="button" disabled={result.last || loading} onClick={() => setPage((value) => value + 1)}>다음</button></div>}
    </section>
    {selected && <VerificationModal id={selected} onClose={() => setSelected(null)} onChanged={load} />}
  </>;
}
