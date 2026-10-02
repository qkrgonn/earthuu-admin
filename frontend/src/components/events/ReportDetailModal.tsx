import { useState } from "react";
import { useAdmin } from "../../store/AdminContext";
import { HistoryList } from "../common/HistoryList";
import { Modal } from "../common/Modal";
import { StatusBadge } from "../common/StatusBadge";

export function ReportDetailModal({ id }: { id: string }) {
  const { reports, events, closeModal, openEvent, startReport, resolveReport } = useAdmin();
  const report = reports.find((item) => item.id === id);
  const [resolution, setResolution] = useState("조치 불필요");
  const [note, setNote] = useState("");
  const [error, setError] = useState("");
  const [confirming, setConfirming] = useState(false);
  if (!report) return null;
  const event = events.find((item) => item.id === report.target);

  const requestResolve = () => {
    if (!note.trim()) { setError("처리 사유를 입력해 주세요."); return; }
    setError(""); setConfirming(true);
  };

  if (confirming) return <Modal onClose={closeModal}>
    <div className="detail-head"><h2>신고 처리를 완료할까요?</h2><button className="quiet" onClick={closeModal}>닫기</button></div>
    <div className="detail-body"><p><b>{resolution}</b></p><p className="description">{note}</p><div className="detail-actions">
      <button className="button" onClick={() => setConfirming(false)}>돌아가기</button>
      <button className="primary" onClick={() => { resolveReport(id, resolution, note.trim()); setConfirming(false); }}>처리 완료</button>
    </div></div>
  </Modal>;

  return <Modal onClose={closeModal}>
    <div className="detail-head"><div><StatusBadge status={report.status} /><h2>{report.reason}</h2><p className="sub">{report.id} · {report.date} 접수</p></div><button className="quiet" onClick={closeModal}>닫기 ✕</button></div>
    <div className="detail-body">
      <div className="detail-grid">
        <div className="field"><small>신고 대상</small><b>{report.type === "event" ? "이벤트" : "호스트"} · {report.title}</b></div>
        <div className="field"><small>관련 이벤트</small>{event ? <button className="event-link" onClick={() => openEvent(event.id)}>{event.title}</button> : <span className="sub">연결된 이벤트 없음</span>}</div>
      </div>
      <div className="detail-section"><h3>신고 내용</h3><p className="description">{report.description}</p></div>
      <div className="detail-section"><h3>증빙</h3><p className="sub">{report.evidence}</p></div>
      <div className="detail-section"><h3>처리 이력</h3><HistoryList items={report.history} /></div>
      {report.status === "received" && <div className="detail-actions"><button className="primary" onClick={() => startReport(id)}>검토 시작</button></div>}
      {report.status === "investigating" && <div className="detail-section">
        <label htmlFor="resolution">조치 결정</label>
        <select id="resolution" value={resolution} onChange={(input) => setResolution(input.target.value)}><option>조치 불필요</option>{event && <option>운영 중단</option>}</select>
        <label htmlFor="resolution-note">처리 사유 · 필수</label>
        <textarea id="resolution-note" placeholder="확인한 내용과 판단 사유를 입력하세요." value={note} onChange={(input) => setNote(input.target.value)} />
        <p className="sub">운영 중단은 관련 이벤트가 있을 때만 선택할 수 있습니다.</p><p className="error" role="alert">{error}</p>
        <div className="detail-actions"><button className="primary" onClick={requestResolve}>처리 결과 확인</button></div>
      </div>}
      {report.status === "resolved" && <div className="insight">{report.resolution} · {report.note}</div>}
    </div>
  </Modal>;
}
