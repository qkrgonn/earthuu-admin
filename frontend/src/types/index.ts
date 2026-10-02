export type ReviewStatus = "pending" | "reviewing" | "approved" | "rejected";
export type LifeStatus = "not_open" | "open" | "ended" | "cancelled" | "suspended";
export type ReportStatus = "received" | "investigating" | "resolved";
export type ParticipantStatus = "confirmed" | "declined" | "applied";

export interface HistoryItem {
  action: string;
  at: string;
  actor: string;
  reason?: string;
}

export interface EventItem {
  id: string;
  title: string;
  host: string;
  school: string;
  cat: string;
  date: string;
  submitted: string;
  venue: string;
  status: ReviewStatus;
  life: LifeStatus;
  capacity: number;
  version: number;
  history: HistoryItem[];
  reason?: string;
  cancelReason?: string;
  endsAt?: string;
  timezone?: string;
  address?: string;
  description?: string;
  detailLoaded?: boolean;
}

export interface ReportItem {
  id: string;
  type: "event" | "host";
  target: string;
  title: string;
  reason: string;
  description: string;
  evidence: string;
  status: ReportStatus;
  date: string;
  history: HistoryItem[];
  resolution?: string;
  note?: string;
}

export interface Participant {
  id?: string;
  name: string;
  group: string;
  status: ParticipantStatus;
}

export interface MetricItem {
  date: string;
  web: number;
  app: number;
  joins: number;
}

export type Channel = "all" | "web" | "app";
export type Period = "1" | "7" | "30";
