import { request } from "./client";

export interface NotificationItem {
  id: string;
  type: string;
  targetType: string;
  targetId: string | null;
  title: string;
  body: string;
  readAt: string | null;
  createdAt: string;
}

export interface NotificationPage {
  content: NotificationItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface NotificationSettings {
  pushEnabled: boolean;
  chatEnabled: boolean;
  updatedAt: string | null;
}

export interface OutboxItem {
  id: string;
  topic: string;
  aggregateId: string;
  dedupeKey: string;
  status: string;
  attempts: number;
  nextAttemptAt: string | null;
  lastError: string | null;
  createdAt: string;
}

export interface OutboxPage {
  content: OutboxItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export const notificationApi = {
  list: (page = 0) => request<NotificationPage>(`/notifications?page=${page}&size=20`),
  unreadCount: () => request<{ count: number }>("/notifications/unread-count"),
  markRead: (id: string) => request<NotificationItem>(`/notifications/${id}/read`, { method: "POST" }),
  markAllRead: () => request<{ count: number }>("/notifications/read-all", { method: "POST" }),
  settings: () => request<NotificationSettings>("/notification-settings"),
  updateSettings: (pushEnabled: boolean, chatEnabled: boolean) => request<NotificationSettings>("/notification-settings", {
    method: "PATCH",
    body: JSON.stringify({ pushEnabled, chatEnabled }),
  }),
  failedOutbox: (page = 0) => request<OutboxPage>(`/outbox?page=${page}&size=20`),
  retryOutbox: (id: string) => request<OutboxItem>(`/outbox/${id}/retry`, { method: "POST" }),
};
