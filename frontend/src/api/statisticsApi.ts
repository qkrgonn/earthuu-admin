import { request } from "./client";

export interface TimePoint { period: string; count: number }
export interface StatisticsOverview {
  from: string;
  to: string;
  newUsers: number;
  newEvents: number;
  receivedReports: number;
  verificationRequests: number;
}
export interface UserStatistics { registrations: TimePoint[]; currentStatusCounts: Record<string, number> }
export interface EventStatistics { created: TimePoint[]; approved: TimePoint[]; rejected: TimePoint[] }
export interface ReportStatistics { received: TimePoint[]; resolved: TimePoint[] }

export interface StatisticsQuery { from: string; to: string; granularity: "DAY" | "WEEK" | "MONTH" }
const query = (value: StatisticsQuery) => new URLSearchParams({
  from: value.from,
  to: value.to,
  granularity: value.granularity,
}).toString();

export const statisticsApi = {
  overview: (value: StatisticsQuery) => request<StatisticsOverview>(`/statistics/overview?${query(value)}`),
  users: (value: StatisticsQuery) => request<UserStatistics>(`/statistics/users?${query(value)}`),
  events: (value: StatisticsQuery) => request<EventStatistics>(`/statistics/events?${query(value)}`),
  reports: (value: StatisticsQuery) => request<ReportStatistics>(`/statistics/reports?${query(value)}`),
};
