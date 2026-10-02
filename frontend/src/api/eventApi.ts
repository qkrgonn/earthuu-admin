import type { EventItem } from "../types";
import { request } from "./client";

export const eventApi = {
  list: (query = "") => request<EventItem[]>(`/events${query ? `?${query}` : ""}`),
  detail: (id: string) => request<EventItem>(`/events/${id}`),
  startReview: (id: string) => request<void>(`/events/${id}/review`, { method: "POST" }),
  approve: (id: string) => request<void>(`/events/${id}/approve`, { method: "POST" }),
  reject: (id: string, reason: string) => request<void>(`/events/${id}/reject`, { method: "POST", body: JSON.stringify({ reason }) }),
};
