import { request } from "./client";

export type ApiReviewStatus = "PENDING" | "REVIEWING" | "APPROVED" | "REJECTED";
export type ApiLifecycleStatus = "NOT_OPEN" | "OPEN" | "ENDED" | "CANCELLED" | "SUSPENDED";

export interface ApiPage<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ApiEventListItem {
  id: string;
  title: string;
  hostId: string;
  hostName: string | null;
  universityName: string | null;
  categoryId: string | null;
  venueName: string | null;
  startsAt: string;
  submittedAt: string | null;
  moderationStatus: ApiReviewStatus;
  lifecycleStatus: ApiLifecycleStatus;
  revision: number;
}

export interface ApiModerationHistory {
  id: string;
  actorId: string;
  action: "REVIEW_STARTED" | "APPROVED" | "REJECTED";
  reason: string | null;
  createdAt: string;
}

export interface ApiEventDetail extends ApiEventListItem {
  currentVersionId: string;
  versionNumber: number;
  submissionState: string;
  cityCode: string | null;
  address: string | null;
  latitude: number | null;
  longitude: number | null;
  endsAt: string;
  timezone: string;
  recruitmentStart: string | null;
  recruitmentEnd: string | null;
  descriptionKo: string | null;
  descriptionEn: string | null;
  publishedAt: string | null;
  moderationHistory: ApiModerationHistory[];
}

export interface ApiParticipant {
  participationId: string;
  userId: string;
  name: string | null;
  quotaGroup: string | null;
  nationality: string | null;
  status: string;
  attendance: string | null;
  confirmedAt: string | null;
}

export interface ApiReviewedEvent {
  id: string;
  title: string;
  hostName: string | null;
  universityName: string | null;
  decision: "APPROVED" | "REJECTED";
  reason: string | null;
  reviewedAt: string;
}

export const eventApi = {
  list: () => request<ApiPage<ApiEventListItem>>("/events?size=100&sort=OLDEST_SUBMITTED"),
  reviewedByMe: () => request<ApiReviewedEvent[]>("/events/reviewed-by-me"),
  detail: (id: string) => request<ApiEventDetail>(`/events/${id}`),
  participants: (id: string) => request<ApiParticipant[]>(`/events/${id}/participants`),
  startReview: (id: string) => request<void>(`/events/${id}/review/start`, { method: "POST" }),
  approve: (id: string) => request<void>(`/events/${id}/review/approve`, { method: "POST" }),
  reject: (id: string, reason: string) => request<void>(`/events/${id}/review/reject`, { method: "POST", body: JSON.stringify({ reason }) }),
};
