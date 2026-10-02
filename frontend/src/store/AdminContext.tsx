import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { authApi, type AdminSession } from "../api/authApi";
import { ApiError } from "../api/client";
import {
  eventApi,
  type ApiEventDetail,
  type ApiEventListItem,
  type ApiParticipant,
} from "../api/eventApi";
import { initialReports } from "../mock/data";
import { STATUS_LABELS } from "../constants/status";
import { nowKST } from "../utils/date";
import type { EventItem, Participant, ReportItem, ReviewStatus } from "../types";

type ModalState =
  | { type: "event"; id: string }
  | { type: "report"; id: string }
  | { type: "forgot" }
  | null;

interface AdminContextValue {
  authReady: boolean;
  loggedIn: boolean;
  session: AdminSession | null;
  events: EventItem[];
  eventsLoading: boolean;
  eventActionPending: string | null;
  eventError: string;
  participants: Record<string, Participant[]>;
  reports: ReportItem[];
  modal: ModalState;
  toast: string;
  dark: boolean;
  login: (email: string, password: string) => Promise<string | null>;
  logout: () => Promise<void>;
  refreshEvents: () => Promise<void>;
  notify: (message: string) => void;
  setDark: (value: boolean) => void;
  openEvent: (id: string) => void;
  openReport: (id: string) => void;
  openForgot: () => void;
  closeModal: () => void;
  startReview: (id: string) => Promise<boolean>;
  decideEvent: (id: string, status: Extract<ReviewStatus, "approved" | "rejected">, reason?: string) => Promise<boolean>;
  startReport: (id: string) => void;
  resolveReport: (id: string, resolution: string, note: string) => void;
}

const AdminContext = createContext<AdminContextValue | null>(null);
const cloneReports = () => structuredClone(initialReports);

const statusText = (value: string) => value.toLowerCase() as ReviewStatus;
const lifecycleText = (value: string) => value.toLowerCase() as EventItem["life"];
const dateOnly = (value?: string | null) => value?.slice(0, 10) ?? "";
const dateTime = (value: string) => new Date(value).toLocaleString("sv-SE", {
  timeZone: "Asia/Seoul",
  hour12: false,
}).slice(0, 16);

function mapListItem(item: ApiEventListItem): EventItem {
  return {
    id: item.id,
    title: item.title,
    host: item.hostName ?? item.hostId.slice(0, 8),
    school: item.universityName ?? "소속 정보 없음",
    cat: item.categoryId ? `카테고리 ${item.categoryId.slice(0, 8)}` : "미분류",
    date: dateOnly(item.startsAt),
    submitted: dateOnly(item.submittedAt),
    venue: item.venueName ?? "장소 미정",
    status: statusText(item.moderationStatus),
    life: lifecycleText(item.lifecycleStatus),
    capacity: 0,
    version: 1,
    history: [],
  };
}

function mergeDetail(current: EventItem, detail: ApiEventDetail): EventItem {
  const actionLabels: Record<string, string> = {
    REVIEW_STARTED: "심사 시작",
    APPROVED: "승인완료",
    REJECTED: "불가 판정",
  };
  const actionHistory = detail.moderationHistory.map((item) => ({
    action: actionLabels[item.action] ?? item.action,
    at: dateTime(item.createdAt),
    actor: `관리자 ${item.actorId.slice(0, 8)}`,
    ...(item.reason ? { reason: item.reason } : {}),
  }));
  const rejected = detail.moderationHistory.find((item) => item.action === "REJECTED");
  return {
    ...current,
    ...mapListItem(detail),
    version: detail.versionNumber,
    endsAt: detail.endsAt,
    timezone: detail.timezone,
    address: detail.address ?? undefined,
    description: detail.descriptionKo ?? detail.descriptionEn ?? "등록된 이벤트 소개가 없습니다.",
    history: [
      ...actionHistory,
      ...(detail.submittedAt ? [{ action: "심사신청", at: dateTime(detail.submittedAt), actor: detail.hostName ?? "호스트" }] : []),
    ],
    reason: rejected?.reason ?? undefined,
    detailLoaded: true,
  };
}

function mapParticipant(item: ApiParticipant): Participant {
  const groupLabels: Record<string, string> = { KOREAN: "한국학생", INTERNATIONAL: "국제학생" };
  return {
    id: item.participationId,
    name: item.name ?? item.userId.slice(0, 8),
    group: item.quotaGroup ? (groupLabels[item.quotaGroup] ?? item.quotaGroup) : "구분 없음",
    status: item.status.toLowerCase() as Participant["status"],
  };
}

export function AdminProvider({ children }: { children: ReactNode }) {
  const [authReady, setAuthReady] = useState(false);
  const [session, setSession] = useState<AdminSession | null>(null);
  const [events, setEvents] = useState<EventItem[]>([]);
  const [eventsLoading, setEventsLoading] = useState(false);
  const [eventActionPending, setEventActionPending] = useState<string | null>(null);
  const [eventError, setEventError] = useState("");
  const [participants, setParticipants] = useState<Record<string, Participant[]>>({});
  const [reports, setReports] = useState<ReportItem[]>(cloneReports);
  const [modal, setModal] = useState<ModalState>(null);
  const [toast, setToast] = useState("");
  const [dark, setDarkState] = useState(() => {
    try {
      return localStorage.getItem("earthuu-theme") === "dark";
    } catch {
      return false;
    }
  });

  useEffect(() => {
    document.documentElement.dataset.theme = dark ? "dark" : "light";
    try {
      localStorage.setItem("earthuu-theme", dark ? "dark" : "light");
    } catch {
      // Storage can be disabled in privacy mode.
    }
  }, [dark]);

  const notify = useCallback((message: string) => {
    setToast(message);
    window.setTimeout(() => setToast(""), 3000);
  }, []);

  const refreshEvents = useCallback(async () => {
    setEventsLoading(true);
    setEventError("");
    try {
      const page = await eventApi.list();
      setEvents(page.content.map(mapListItem));
    } catch (error) {
      const message = error instanceof ApiError ? error.message : "이벤트 목록을 불러오지 못했습니다.";
      setEventError(message);
      if (error instanceof ApiError && error.status === 401) setSession(null);
    } finally {
      setEventsLoading(false);
    }
  }, []);

  const loadEventDetail = useCallback(async (id: string) => {
    setEventError("");
    try {
      const [detail, participantItems] = await Promise.all([
        eventApi.detail(id),
        eventApi.participants(id),
      ]);
      setEvents((current) => current.map((event) => event.id === id ? mergeDetail(event, detail) : event));
      setParticipants((current) => ({ ...current, [id]: participantItems.map(mapParticipant) }));
    } catch (error) {
      setEventError(error instanceof ApiError ? error.message : "이벤트 상세를 불러오지 못했습니다.");
    }
  }, []);

  useEffect(() => {
    let active = true;
    void authApi.me()
      .then(async (admin) => {
        if (!active) return;
        setSession(admin);
        await refreshEvents();
      })
      .catch(() => {
        if (active) setSession(null);
      })
      .finally(() => {
        if (active) setAuthReady(true);
      });
    return () => { active = false; };
  }, [refreshEvents]);

  const login = useCallback(async (email: string, password: string) => {
    try {
      const admin = await authApi.login(email, password);
      setSession(admin);
      await refreshEvents();
      notify("관리자 계정으로 로그인했습니다.");
      return null;
    } catch (error) {
      const message = error instanceof ApiError
        ? error.message
        : "백엔드 서버에 연결할 수 없습니다.";
      setEventError(message);
      notify(message);
      return message;
    }
  }, [notify, refreshEvents]);

  const logout = useCallback(async () => {
    try {
      await authApi.logout();
    } catch {
      // The local session is cleared even if the server is already unavailable.
    }
    setSession(null);
    setEvents([]);
    setParticipants({});
    setModal(null);
    notify("로그아웃했습니다.");
  }, [notify]);

  const runEventAction = useCallback(async (id: string, action: () => Promise<unknown>, successMessage: string) => {
    if (eventActionPending) return false;
    setEventActionPending(id);
    setEventError("");
    try {
      await action();
      await refreshEvents();
      await loadEventDetail(id);
      notify(successMessage);
      return true;
    } catch (error) {
      const message = error instanceof ApiError ? error.message : "심사 요청을 처리하지 못했습니다.";
      setEventError(message);
      notify(message);
      return false;
    } finally {
      setEventActionPending(null);
    }
  }, [eventActionPending, loadEventDetail, notify, refreshEvents]);

  const startReview = useCallback((id: string) =>
    runEventAction(id, () => eventApi.startReview(id), "심사를 시작했습니다."), [runEventAction]);

  const decideEvent = useCallback((id: string, status: "approved" | "rejected", reason = "") =>
    runEventAction(
      id,
      () => status === "approved" ? eventApi.approve(id) : eventApi.reject(id, reason),
      `${STATUS_LABELS[status]} 처리했습니다.`,
    ), [runEventAction]);

  const openEvent = useCallback((id: string) => {
    setModal({ type: "event", id });
    void loadEventDetail(id);
  }, [loadEventDetail]);

  const startReport = useCallback((id: string) => {
    setReports((current) => current.map((report) => report.id === id && report.status === "received"
      ? { ...report, status: "investigating", history: [...report.history, { action: "검토 시작", at: nowKST(), actor: "김관리" }] }
      : report));
  }, []);

  const resolveReport = useCallback((id: string, resolution: string, note: string) => {
    setReports((current) => current.map((report) => report.id === id && report.status === "investigating"
      ? { ...report, status: "resolved", resolution, note, history: [...report.history, { action: resolution, reason: note, at: nowKST(), actor: "김관리" }] }
      : report));
    notify("신고 처리 결과를 기록했습니다. 신고 기능은 아직 데모 데이터입니다.");
  }, [notify]);

  const value = useMemo<AdminContextValue>(() => ({
    authReady,
    loggedIn: session !== null,
    session,
    events,
    eventsLoading,
    eventActionPending,
    eventError,
    participants,
    reports,
    modal,
    toast,
    dark,
    login,
    logout,
    refreshEvents,
    notify,
    setDark: setDarkState,
    openEvent,
    openReport: (id) => setModal({ type: "report", id }),
    openForgot: () => setModal({ type: "forgot" }),
    closeModal: () => setModal(null),
    startReview,
    decideEvent,
    startReport,
    resolveReport,
  }), [authReady, session, events, eventsLoading, eventActionPending, eventError, participants, reports, modal,
    toast, dark, login, logout, refreshEvents, notify, openEvent, startReview, decideEvent, startReport, resolveReport]);

  return <AdminContext.Provider value={value}>{children}</AdminContext.Provider>;
}

export function useAdmin() {
  const context = useContext(AdminContext);
  if (!context) throw new Error("useAdmin must be used inside AdminProvider");
  return context;
}
