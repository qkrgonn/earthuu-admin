import { useCallback, useEffect, useState } from "react";
import { ApiError } from "../../api/client";
import { userApi, type ApiUserDetail, type ApiUserRestriction } from "../../api/userApi";
import { Modal } from "../common/Modal";
import { StatusBadge } from "../common/StatusBadge";

type PendingAction =
  | { type: "suspend" }
  | { type: "restore" }
  | { type: "revoke"; restrictionId: string }
  | null;

const dateTime = (value?: string | null) => value
  ? new Date(value).toLocaleString("ko-KR", { timeZone: "Asia/Seoul", hour12: false })
  : "-";

function restrictionPeriod(item: ApiUserRestriction) {
  if (!item.endsAt) return "무기한";
  return `${dateTime(item.startsAt)} ~ ${dateTime(item.endsAt)}`;
}

export function UserDetailModal({
  userId,
  onClose,
  onChanged,
  notify,
}: {
  userId: string;
  onClose: () => void;
  onChanged: () => Promise<void>;
  notify: (message: string) => void;
}) {
  const [detail, setDetail] = useState<ApiUserDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [pendingAction, setPendingAction] = useState<PendingAction>(null);
  const [reason, setReason] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      setDetail(await userApi.detail(userId));
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "사용자 상세를 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  }, [userId]);

  useEffect(() => { void load(); }, [load]);

  const openAction = (action: Exclude<PendingAction, null>) => {
    setPendingAction(action);
    setReason("");
    setError("");
  };

  const submitAction = async () => {
    if (!pendingAction || !detail) return;
    if (!reason.trim()) {
      setError("처리 사유를 입력해 주세요.");
      return;
    }
    setSubmitting(true);
    setError("");
    try {
      if (pendingAction.type === "suspend") await userApi.suspend(userId, reason.trim());
      if (pendingAction.type === "restore") await userApi.restore(userId, reason.trim());
      if (pendingAction.type === "revoke") {
        await userApi.revokeRestriction(userId, pendingAction.restrictionId, reason.trim());
      }
      const message = pendingAction.type === "suspend"
        ? "사용자 계정을 정지했습니다."
        : pendingAction.type === "restore"
          ? "사용자 계정을 복구했습니다."
          : "활동 제한을 해제했습니다.";
      notify(message);
      setPendingAction(null);
      setReason("");
      await Promise.all([load(), onChanged()]);
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "사용자 요청을 처리하지 못했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  const actionTitle = pendingAction?.type === "suspend"
    ? "계정을 정지할까요?"
    : pendingAction?.type === "restore"
      ? "계정을 복구할까요?"
      : "활동 제한을 해제할까요?";

  return <Modal onClose={onClose}>
    <div className="detail-head user-detail-head">
      <div>
        {detail && <StatusBadge status={detail.status.toLowerCase()} />}
        <h2>{detail?.name ?? "사용자 상세"}</h2>
        <p className="sub">{detail?.email ?? userId}</p>
      </div>
      <button className="quiet" onClick={onClose} aria-label="사용자 상세 닫기">닫기 ✕</button>
    </div>
    <div className="detail-body">
      {loading && <div className="empty user-modal-empty">사용자 정보를 불러오고 있습니다.</div>}
      {!loading && error && !detail && <div className="api-error" role="alert">{error}</div>}
      {detail && <>
        {error && !pendingAction && <div className="api-error user-detail-error" role="alert">{error}</div>}
        <div className="user-summary-card">
          <span className="user-avatar">{(detail.name ?? detail.email ?? "U").slice(0, 1).toUpperCase()}</span>
          <div><strong>{detail.name ?? "이름 정보 없음"}</strong><span>{detail.universityName ?? "소속 대학 정보 없음"}</span></div>
          <span className="user-role">{detail.role === "ADMIN" ? "관리자" : "일반 사용자"}</span>
        </div>
        <div className="detail-grid user-detail-grid">
          <div className="field"><small>사용자 ID</small><b className="mono-value">{detail.id}</b></div>
          <div className="field"><small>계정 상태</small><b>{detail.status}</b></div>
          <div className="field"><small>가입 일시</small><b>{dateTime(detail.createdAt)}</b></div>
          <div className="field"><small>최근 변경</small><b>{dateTime(detail.updatedAt)}</b></div>
        </div>

        {pendingAction && <div className="user-action-box">
          <h3>{actionTitle}</h3>
          <p>{pendingAction.type === "suspend"
            ? "정지된 사용자는 서비스 활동이 제한됩니다. 처리 사유는 관리자 이력에 남습니다."
            : pendingAction.type === "restore"
              ? "계정을 ACTIVE 상태로 복구하고 처리 이력을 남깁니다."
              : "선택한 활동 제한을 즉시 해제하고 감사 로그를 남깁니다."}</p>
          <label htmlFor="user-action-reason">처리 사유 · 필수</label>
          <textarea id="user-action-reason" maxLength={1000} value={reason}
            onChange={(event) => setReason(event.target.value)} placeholder="처리 근거를 입력해 주세요." />
          {error && <p className="error" role="alert">{error}</p>}
          <div className="detail-actions">
            <button className="button" disabled={submitting} onClick={() => { setPendingAction(null); setError(""); }}>취소</button>
            <button className={pendingAction.type === "suspend" ? "danger" : "primary"} disabled={submitting}
              onClick={() => void submitAction()}>{submitting ? "처리 중…" : "처리 확정"}</button>
          </div>
        </div>}

        {!pendingAction && detail.role !== "ADMIN" && <div className="detail-actions user-account-actions">
          {detail.status === "ACTIVE" && <button className="danger" onClick={() => openAction({ type: "suspend" })}>계정 정지</button>}
          {detail.status === "SUSPENDED" && <button className="primary" onClick={() => openAction({ type: "restore" })}>계정 복구</button>}
          {detail.status === "WITHDRAWN" && <span className="sub">탈퇴 계정은 상태를 변경할 수 없습니다.</span>}
        </div>}
        {!pendingAction && detail.role === "ADMIN" && <div className="insight">관리자 계정은 사용자 관리 화면에서 제재할 수 없습니다.</div>}

        <div className="detail-section">
          <div className="user-section-title"><h3>활동 제한</h3><span>{detail.restrictions.length}건</span></div>
          {detail.restrictions.length ? <div className="restriction-list">{detail.restrictions.map((item) => <article key={item.id} className="restriction-card">
            <div className="restriction-card-head"><StatusBadge status={item.status.toLowerCase()} /><small>{restrictionPeriod(item)}</small></div>
            <strong>{item.reason}</strong>
            <p>신고 ID · <span className="mono-value">{item.sourceReportId}</span></p>
            {item.revokeReason && <p>해제 사유 · {item.revokeReason}</p>}
            {item.status === "ACTIVE" && !pendingAction && <button className="button" onClick={() => openAction({ type: "revoke", restrictionId: item.id })}>제한 해제</button>}
          </article>)}</div> : <div className="empty user-section-empty">등록된 활동 제한이 없습니다.</div>}
        </div>

        <div className="detail-section">
          <div className="user-section-title"><h3>계정 처리 이력</h3><span>{detail.statusHistory.length}건</span></div>
          {detail.statusHistory.length ? <div className="user-history-list">{detail.statusHistory.map((item) => <div className="history" key={item.id}>
            <b>{item.action === "SUSPENDED" ? "계정 정지" : "계정 복구"}</b>
            <p>{item.previousStatus} → {item.newStatus}</p>
            <p>{item.reason}</p>
            <small>{dateTime(item.createdAt)} · 관리자 {item.actorId.slice(0, 8)}</small>
          </div>)}</div> : <div className="empty user-section-empty">계정 처리 이력이 없습니다.</div>}
        </div>
      </>}
    </div>
  </Modal>;
}
