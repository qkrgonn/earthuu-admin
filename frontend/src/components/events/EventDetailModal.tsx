import { useState } from "react";
import { participantsByEvent } from "../../mock/data";
import { useAdmin } from "../../store/AdminContext";
import { HistoryList } from "../common/HistoryList";
import { Modal } from "../common/Modal";
import { StatusBadge } from "../common/StatusBadge";

export function EventDetailModal({ id }: { id: string }) {
  const { events, closeModal, startReview, decideEvent } = useAdmin();
  const event = events.find((item) => item.id === id);
  const [reason, setReason] = useState("");
  const [error, setError] = useState("");
  const [decision, setDecision] = useState<"approved" | "rejected" | null>(null);
  const participants = participantsByEvent[id] ?? [];
  if (!event) return null;

  const requestDecision = (next: "approved" | "rejected") => {
    if (next === "rejected" && !reason.trim()) {
      setError("불가 사유를 입력해 주세요.");
      return;
    }
    setError("");
    setDecision(next);
  };

  if (decision) return <Modal onClose={closeModal}>
    <div className="detail-head"><h2>{decision === "approved" ? "이벤트를 승인할까요?" : "불가 판정을 확정할까요?"}</h2><button className="quiet" onClick={closeModal}>닫기</button></div>
    <div className="detail-body">
      <p>{event.title}</p>
      <p className="sub" style={{ marginTop: 12 }}>{decision === "approved" ? "승인하면 학생에게 공개되고 참가 신청이 가능해집니다." : `호스트에게 표시할 사유: ${reason}`}</p>
      <div className="detail-actions"><button className="button" onClick={() => setDecision(null)}>돌아가기</button><button className="primary" onClick={() => { decideEvent(id, decision, reason.trim()); setDecision(null); }}>{decision === "approved" ? "승인 확정" : "불가 판정 확정"}</button></div>
    </div>
  </Modal>;

  const fields = [
    ["호스트", `${event.host} · ${event.school}`], ["카테고리", event.cat], ["개최일", `${event.date} 14:00–17:00`],
    ["장소", event.venue], ["모집 정원", `${event.capacity}명 · 한국학생 / 국제학생 각 ${event.capacity / 2}명`], ["심사신청일", event.submitted],
  ];

  return <Modal onClose={closeModal}>
    <div className="detail-head">
      <div><StatusBadge status={event.status} /> {event.status === "approved" && <StatusBadge status={event.life} />}<h2>{event.title}</h2><p className="sub">{event.id} · 원문 버전 {event.version}</p></div>
      <button className="quiet" onClick={closeModal} aria-label="상세 닫기">닫기 ✕</button>
    </div>
    <div className="detail-body">
      <div className="detail-grid">{fields.map(([label, value]) => <div className="field" key={label}><small>{label}</small><b>{value}</b></div>)}</div>
      <div className="detail-section"><h3>이벤트 소개 · 등록 원문</h3><p className="description">{event.title}에 함께할 친구들을 모집합니다. 한국학생과 국제학생이 소규모 팀을 이루어 대화하고 서로의 문화를 알아가는 모임입니다.{"\n\n"}진행: 서로 소개하기 → 함께하는 활동 → 자유 교류{"\n"}언어: 한국어 / 영어 · 준비물: 편한 복장</p></div>
      {event.status === "rejected" && <div className="insight">불가 사유: {event.reason}</div>}
      {event.life === "cancelled" && <div className="insight">호스트 폐기 기록 · {event.cancelReason}<br />기존 신청자 처리·알림 정책은 미정입니다.</div>}
      {event.status === "approved" && <div className="detail-section">
        <h3>참가 신청 현황 · {participants.length}명</h3><p className="sub">참여 승인·반려는 호스트가 처리합니다.</p>
        <div className="table-wrap"><table><thead><tr><th>신청자</th><th>구분</th><th>상태</th></tr></thead><tbody>
          {participants.length ? participants.map((participant) => <tr key={participant.name}><td>{participant.name}</td><td>{participant.group}</td><td><StatusBadge status={participant.status} /></td></tr>) : <tr><td colSpan={3} className="empty">아직 참가 신청자가 없습니다. 승인 후부터 참가자를 모집합니다.</td></tr>}
        </tbody></table></div>
      </div>}
      <div className="detail-section"><h3>심사 및 운영 이력</h3><HistoryList items={event.history} /></div>
      {event.status === "pending" && <><div className="insight">심사를 시작하면 호스트의 글 수정이 잠깁니다.</div><div className="detail-actions"><button className="primary" onClick={() => startReview(id)}>심사 시작</button></div></>}
      {event.status === "reviewing" && <div className="detail-section">
        <label htmlFor="reject-reason">불가 사유 · 반려 시 필수</label>
        <input id="reject-reason" maxLength={150} placeholder="호스트에게 보여줄 사유 한 줄" style={{ width: "100%" }} value={reason} onChange={(input) => setReason(input.target.value)} />
        <p className="error" role="alert">{error}</p>
        <div className="detail-actions"><button className="danger" onClick={() => requestDecision("rejected")}>불가 판정</button><button className="primary" onClick={() => requestDecision("approved")}>승인</button></div>
      </div>}
      {!["pending", "reviewing"].includes(event.status) && <div className="insight">심사 시작 이후 원문을 수정할 수 없습니다. 승인 후 변경은 폐기 후 신규 심사가 필요합니다.</div>}
    </div>
  </Modal>;
}
