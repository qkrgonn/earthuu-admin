import { useAdmin } from "../../store/AdminContext";
import { EventDetailModal } from "../events/EventDetailModal";
import { ReportDetailModal } from "../events/ReportDetailModal";
import { Modal } from "./Modal";

export function ModalHost() {
  const { modal, closeModal } = useAdmin();
  if (!modal) return null;
  if (modal.type === "event") return <EventDetailModal id={modal.id} />;
  if (modal.type === "report") return <ReportDetailModal id={modal.id} />;
  return <Modal onClose={closeModal}>
    <div className="detail-head"><h2>비밀번호 재설정</h2><button className="quiet" onClick={closeModal}>닫기</button></div>
    <div className="detail-body"><p>비밀번호 재설정 이메일 발송은 아직 구현되지 않았습니다. 로컬 개발 계정은 백엔드 환경 변수에서 다시 설정해 주세요.</p></div>
  </Modal>;
}
