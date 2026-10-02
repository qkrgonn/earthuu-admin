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
    <div className="detail-body"><p>실제 이메일 발송은 연결되지 않았습니다. 데모 비밀번호는 <b>earthuu-demo</b>입니다.</p></div>
  </Modal>;
}
