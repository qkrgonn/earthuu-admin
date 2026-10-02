import type { ReportItem } from "../types";
import { request } from "./client";

export const reportApi = {
  list: (query = "") => request<ReportItem[]>(`/reports${query ? `?${query}` : ""}`),
  detail: (id: string) => request<ReportItem>(`/reports/${id}`),
  startReview: (id: string) => request<void>(`/reports/${id}/review`, { method: "POST" }),
  resolve: (id: string, resolution: string, note: string) => request<void>(`/reports/${id}/resolve`, { method: "POST", body: JSON.stringify({ resolution, note }) }),
};
