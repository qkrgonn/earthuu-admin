import { request } from "./client";

export type VerificationStatus = "PENDING" | "APPROVED" | "REJECTED";

export interface VerificationListItem {
  id: string;
  userId: string;
  userName: string | null;
  loginEmail: string | null;
  schoolEmail: string;
  universityId: string;
  universityName: string | null;
  status: VerificationStatus;
  emailVerifiedAt: string | null;
  createdAt: string;
  revision: number;
}

export interface VerificationDetail extends VerificationListItem {
  domainVerifiedBy: string | null;
  verifiedAt: string | null;
  reviewedBy: string | null;
  reviewedAt: string | null;
  rejectionReason: string | null;
  updatedAt: string;
}

export interface VerificationPage {
  content: VerificationListItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

interface SearchParams {
  query?: string;
  status?: VerificationStatus;
  sort?: "NEWEST" | "OLDEST";
  page?: number;
  size?: number;
}

function queryString(params: SearchParams) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== "") search.set(key, String(value));
  });
  return search.toString();
}

export const verificationApi = {
  list: (params: SearchParams) => request<VerificationPage>(`/verifications?${queryString(params)}`),
  detail: (id: string) => request<VerificationDetail>(`/verifications/${id}`),
  approve: (id: string) => request<void>(`/verifications/${id}/approve`, { method: "POST" }),
  reject: (id: string, reason: string) => request<void>(`/verifications/${id}/reject`, {
    method: "POST",
    body: JSON.stringify({ reason }),
  }),
};
