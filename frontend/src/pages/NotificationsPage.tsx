import { useCallback, useEffect, useState } from "react";
import { ApiError } from "../api/client";
import { notificationApi, type NotificationPage, type NotificationSettings, type OutboxPage } from "../api/notificationApi";
import { PageHead } from "../components/common/PageHead";
import { Tabs } from "../components/common/Tabs";
import { useAdmin } from "../store/AdminContext";

const emptyNotifications: NotificationPage = { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0, first: true, last: true };
const emptyOutbox: OutboxPage = { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0, first: true, last: true };
const initialSettings: NotificationSettings = { pushEnabled: true, chatEnabled: true, updatedAt: null };
const dateTime = (value?: string | null) => value ? new Date(value).toLocaleString("ko-KR", { timeZone: "Asia/Seoul" }) : "-";

export function NotificationsPage() {
  const { notify } = useAdmin();
  const [tab, setTab] = useState("notifications");
  const [notifications, setNotifications] = useState(emptyNotifications);
  const [outbox, setOutbox] = useState(emptyOutbox);
  const [settings, setSettings] = useState(initialSettings);
  const [unread, setUnread] = useState(0);
  const [page, setPage] = useState(0);
  const [outboxPage, setOutboxPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [pending, setPending] = useState(false);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    setLoading(true); setError("");
    try {
      const [notificationResult, countResult, settingResult, outboxResult] = await Promise.all([
        notificationApi.list(page), notificationApi.unreadCount(), notificationApi.settings(), notificationApi.failedOutbox(outboxPage),
      ]);
      setNotifications(notificationResult); setUnread(countResult.count); setSettings(settingResult); setOutbox(outboxResult);
    } catch (caught) { setError(caught instanceof ApiError ? caught.message : "알림 정보를 불러오지 못했습니다."); }
    finally { setLoading(false); }
  }, [outboxPage, page]);
  useEffect(() => { void load(); }, [load]);

  const run = async (action: () => Promise<unknown>, success: string) => {
    setPending(true); setError("");
    try { await action(); notify(success); await load(); }
    catch (caught) { setError(caught instanceof ApiError ? caught.message : "알림 요청을 처리하지 못했습니다."); }
    finally { setPending(false); }
  };

  return <>
    <PageHead title="알림 관리" description="관리자 알림과 수신 설정, 실패한 비동기 작업을 확인하세요." extra={<button className="button" disabled={loading} onClick={() => void load()}>{loading ? "불러오는 중…" : "새로고침"}</button>} />
    <section className="panel notification-panel">
      <Tabs items={[{ value: "notifications", label: `관리자 알림 ${unread ? `(${unread})` : ""}` }, { value: "settings", label: "알림 설정" }, { value: "outbox", label: `실패 Outbox (${outbox.totalElements})` }]} value={tab} onChange={setTab} />
      {error && <p className="api-error" role="alert">{error}</p>}
      {tab === "notifications" && <>
        <div className="notification-toolbar"><div>총 <b>{notifications.totalElements}</b>건 · 읽지 않음 <b>{unread}</b>건</div><button className="button" disabled={pending || unread === 0} onClick={() => void run(() => notificationApi.markAllRead(), "모든 알림을 읽음 처리했습니다.")}>모두 읽음</button></div>
        <div className="notification-list">{notifications.content.length ? notifications.content.map((item) => <article key={item.id} className={`notification-item ${item.readAt ? "" : "unread"}`}><span className="notification-dot" /><div><div className="notification-title"><b>{item.title}</b><small>{dateTime(item.createdAt)}</small></div><p>{item.body}</p><span>{item.type} · {item.targetType}</span></div>{!item.readAt && <button className="button" disabled={pending} onClick={() => void run(() => notificationApi.markRead(item.id), "알림을 읽음 처리했습니다.")}>읽음</button>}</article>) : <div className="empty">도착한 관리자 알림이 없습니다.</div>}</div>
        {notifications.totalPages > 1 && <div className="user-pagination"><button className="button" disabled={notifications.first || loading} onClick={() => setPage((value) => value - 1)}>이전</button><span><b>{notifications.page + 1}</b> / {notifications.totalPages}</span><button className="button" disabled={notifications.last || loading} onClick={() => setPage((value) => value + 1)}>다음</button></div>}
      </>}
      {tab === "settings" && <div className="settings-panel"><h2>수신 설정</h2><p className="sub">현재 관리자 계정에 적용됩니다.</p><label className="setting-row"><span><b>푸시 알림</b><small>심사·신고·인증 처리 결과를 알립니다.</small></span><input type="checkbox" checked={settings.pushEnabled} onChange={(event) => setSettings((current) => ({ ...current, pushEnabled: event.target.checked }))} /></label><label className="setting-row"><span><b>채팅 알림</b><small>관리자 채팅 관련 알림을 받습니다.</small></span><input type="checkbox" checked={settings.chatEnabled} onChange={(event) => setSettings((current) => ({ ...current, chatEnabled: event.target.checked }))} /></label><div className="action-row"><span className="sub">마지막 변경: {dateTime(settings.updatedAt)}</span><button className="primary" disabled={pending} onClick={() => void run(() => notificationApi.updateSettings(settings.pushEnabled, settings.chatEnabled), "알림 설정을 저장했습니다.")}>설정 저장</button></div></div>}
      {tab === "outbox" && <><div className="count">전송 실패 메시지만 표시합니다. 재시도하면 대기 상태로 돌아갑니다.</div><div className="table-wrap"><table><thead><tr><th>주제</th><th>대상</th><th>시도</th><th>오류</th><th>생성일</th><th>관리</th></tr></thead><tbody>{outbox.content.length ? outbox.content.map((item) => <tr key={item.id}><td>{item.topic}<small>{item.status}</small></td><td><code>{item.aggregateId.slice(0, 8)}</code></td><td>{item.attempts}회</td><td>{item.lastError ?? "-"}</td><td>{dateTime(item.createdAt)}</td><td><button className="button" disabled={pending} onClick={() => void run(() => notificationApi.retryOutbox(item.id), "Outbox 재시도를 예약했습니다.")}>재시도</button></td></tr>) : <tr><td colSpan={6} className="empty">실패한 Outbox 메시지가 없습니다.</td></tr>}</tbody></table></div>{outbox.totalPages > 1 && <div className="user-pagination"><button className="button" disabled={outbox.first || loading} onClick={() => setOutboxPage((value) => value - 1)}>이전</button><span><b>{outbox.page + 1}</b> / {outbox.totalPages}</span><button className="button" disabled={outbox.last || loading} onClick={() => setOutboxPage((value) => value + 1)}>다음</button></div>}</>}
    </section>
  </>;
}
