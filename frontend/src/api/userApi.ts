import { request } from "./client";

export type ApiUserStatus = "ACTIVE" | "SUSPENDED" | "WITHDRAWN";
export type ApiUserRole = "USER" | "ADMIN";
export type ApiRestrictionStatus = "ACTIVE" | "REVOKED" | "EXPIRED";

export interface ApiUserListItem {
  id: string;
  email: string | null;
  name: string | null;
  universityName: string | null;
  status: ApiUserStatus;
  role: ApiUserRole;
  createdAt: string;
  revision: number;
}

export interface ApiUserPage {
  content: ApiUserListItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ApiUserRestriction {
  id: string;
  sourceReportId: string;
  imposedBy: string;
  reason: string;
  status: ApiRestrictionStatus;
  startsAt: string;
  endsAt: string | null;
  revokedAt: string | null;
  revokedBy: string | null;
  revokeReason: string | null;
  revision: number;
}

export interface ApiUserStatusAction {
  id: string;
  actorId: string;
  action: "SUSPENDED" | "RESTORED";
  previousStatus: ApiUserStatus;
  newStatus: ApiUserStatus;
  reason: string;
  createdAt: string;
}

export interface ApiUserDetail extends ApiUserListItem {
  updatedAt: string;
  restrictions: ApiUserRestriction[];
  statusHistory: ApiUserStatusAction[];
}

export interface UserSearchParams {
  query?: string;
  status?: ApiUserStatus;
  role?: ApiUserRole;
  sort?: "NEWEST" | "OLDEST";
  page?: number;
  size?: number;
}

function queryString(params: UserSearchParams) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== "") search.set(key, String(value));
  });
  return search.toString();
}

export const userApi = {
  list: (params: UserSearchParams) => request<ApiUserPage>(`/users?${queryString(params)}`),
  detail: (userId: string) => request<ApiUserDetail>(`/users/${userId}`),
  suspend: (userId: string, reason: string) => request<void>(`/users/${userId}/suspend`, {
    method: "POST",
    body: JSON.stringify({ reason }),
  }),
  restore: (userId: string, reason: string) => request<void>(`/users/${userId}/restore`, {
    method: "POST",
    body: JSON.stringify({ reason }),
  }),
  restrictions: (userId: string) => request<ApiUserRestriction[]>(`/users/${userId}/restrictions`),
  revokeRestriction: (userId: string, restrictionId: string, reason: string) => request<void>(
    `/users/${userId}/restrictions/${restrictionId}/revoke`,
    { method: "POST", body: JSON.stringify({ reason }) },
  ),
};
