import { clearCsrfToken, request } from "./client";

export interface AdminSession {
  id: string;
  email: string;
  role: "ADMIN";
}

export const authApi = {
  login: (email: string, password: string) => request<AdminSession>("/auth/login", { method: "POST", body: JSON.stringify({ email, password }) }),
  me: () => request<AdminSession>("/auth/me"),
  logout: async () => {
    await request<void>("/auth/logout", { method: "POST" });
    clearCsrfToken();
  },
};
