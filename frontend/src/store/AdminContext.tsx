import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { initialEvents, initialReports } from "../mock/data";
import { STATUS_LABELS } from "../constants/status";
import { nowKST } from "../utils/date";
import type { EventItem, ReportItem, ReviewStatus } from "../types";

type ModalState =
  | { type: "event"; id: string }
  | { type: "report"; id: string }
  | { type: "forgot" }
  | null;

interface AdminContextValue {
  loggedIn: boolean;
  events: EventItem[];
  reports: ReportItem[];
  modal: ModalState;
  toast: string;
  dark: boolean;
  login: (email: string, password: string) => boolean;
  logout: () => void;
  notify: (message: string) => void;
  setDark: (value: boolean) => void;
  openEvent: (id: string) => void;
  openReport: (id: string) => void;
  openForgot: () => void;
  closeModal: () => void;
  startReview: (id: string) => void;
  decideEvent: (id: string, status: Extract<ReviewStatus, "approved" | "rejected">, reason?: string) => void;
  startReport: (id: string) => void;
  resolveReport: (id: string, resolution: string, note: string) => void;
}

const AdminContext = createContext<AdminContextValue | null>(null);

const cloneEvents = () => structuredClone(initialEvents);
const cloneReports = () => structuredClone(initialReports);

export function AdminProvider({ children }: { children: ReactNode }) {
  const [loggedIn, setLoggedIn] = useState(() => {
    try {
      return sessionStorage.getItem("earthuu-demo-session") === "active";
    } catch {
      return false;
    }
  });
  const [events, setEvents] = useState<EventItem[]>(cloneEvents);
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

  const login = useCallback((email: string, password: string) => {
    if (email !== "admin@earthuu.demo" || password !== "earthuu-demo") return false;
    setLoggedIn(true);
    try {
      sessionStorage.setItem("earthuu-demo-session", "active");
    } catch {
      // Demo continues without persistence when storage is unavailable.
    }
    notify("데모 계정으로 로그인했습니다.");
    return true;
  }, [notify]);

  const logout = useCallback(() => {
    setLoggedIn(false);
    setModal(null);
    try {
      sessionStorage.removeItem("earthuu-demo-session");
    } catch {
      // Ignore storage failures.
    }
    notify("로그아웃했습니다.");
  }, [notify]);

  const startReview = useCallback((id: string) => {
    setEvents((current) => current.map((event) => event.id === id && event.status === "pending"
      ? {
          ...event,
          status: "reviewing",
          history: [...event.history, { action: "심사 시작", at: nowKST(), actor: "김관리" }],
        }
      : event));
    notify("심사를 시작했습니다.");
  }, [notify]);

  const decideEvent = useCallback((id: string, status: "approved" | "rejected", reason = "") => {
    setEvents((current) => current.map((event) => event.id === id && event.status === "reviewing"
      ? {
          ...event,
          status,
          life: status === "approved" ? "open" : event.life,
          reason: reason || event.reason,
          history: [
            ...event.history,
            { action: STATUS_LABELS[status], at: nowKST(), actor: "김관리", ...(reason ? { reason } : {}) },
          ],
        }
      : event));
    notify(`${STATUS_LABELS[status]} 처리했습니다.`);
  }, [notify]);

  const startReport = useCallback((id: string) => {
    setReports((current) => current.map((report) => report.id === id && report.status === "received"
      ? {
          ...report,
          status: "investigating",
          history: [...report.history, { action: "검토 시작", at: nowKST(), actor: "김관리" }],
        }
      : report));
  }, []);

  const resolveReport = useCallback((id: string, resolution: string, note: string) => {
    const target = reports.find((report) => report.id === id)?.target;
    setReports((current) => current.map((report) => report.id === id && report.status === "investigating"
      ? {
          ...report,
          status: "resolved",
          resolution,
          note,
          history: [...report.history, { action: resolution, reason: note, at: nowKST(), actor: "김관리" }],
        }
      : report));
    if (resolution === "운영 중단" && target) {
      setEvents((current) => current.map((event) => event.id === target
        ? {
            ...event,
            life: "suspended",
            history: [...event.history, { action: "신고 대응 · 운영 중단", reason: note, at: nowKST(), actor: "김관리" }],
          }
        : event));
    }
    notify("신고 처리 결과를 기록했습니다.");
  }, [notify, reports]);

  const value = useMemo<AdminContextValue>(() => ({
    loggedIn, events, reports, modal, toast, dark, login, logout, notify,
    setDark: setDarkState,
    openEvent: (id) => setModal({ type: "event", id }),
    openReport: (id) => setModal({ type: "report", id }),
    openForgot: () => setModal({ type: "forgot" }),
    closeModal: () => setModal(null),
    startReview, decideEvent, startReport, resolveReport,
  }), [loggedIn, events, reports, modal, toast, dark, login, logout, notify, startReview, decideEvent, startReport, resolveReport]);

  return <AdminContext.Provider value={value}>{children}</AdminContext.Provider>;
}

export function useAdmin() {
  const context = useContext(AdminContext);
  if (!context) throw new Error("useAdmin must be used inside AdminProvider");
  return context;
}
