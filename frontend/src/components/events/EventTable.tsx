import { useLocation } from "react-router-dom";
import { useAdmin } from "../../store/AdminContext";
import { shortDate } from "../../utils/date";
import type { EventItem } from "../../types";
import { StatusBadge } from "../common/StatusBadge";

export function EventTable({ events, compact = false }: { events: EventItem[]; compact?: boolean }) {
  const { openEvent } = useAdmin();
  const management = useLocation().pathname === "/events";
  const columnCount = compact ? 4 : management ? 6 : 5;
  return <div className="table-wrap"><table>
    <thead><tr>
      <th>이벤트</th><th>호스트</th>{!compact && <th>개최일</th>}<th>신청일</th><th>심사 상태</th>
      {!compact && management && <th>운영 상태</th>}
    </tr></thead>
    <tbody>{events.length ? events.map((event) => <tr key={event.id}>
      <td><button className="event-link" onClick={() => openEvent(event.id)}>{event.title}</button><small>{event.id} · {event.cat}</small></td>
      <td>{event.host}<small>{event.school}</small></td>
      {!compact && <td>{event.date.replaceAll("-", ".")}<small>{event.venue}</small></td>}
      <td>{shortDate(event.submitted)}<small>{event.submitted.slice(0, 4)}</small></td>
      <td><StatusBadge status={event.status} /></td>
      {!compact && management && <td>{event.status === "approved" ? <StatusBadge status={event.life} /> : "—"}</td>}
    </tr>) : <tr><td colSpan={columnCount} className="empty">조건에 맞는 이벤트가 없습니다.</td></tr>}</tbody>
  </table></div>;
}
