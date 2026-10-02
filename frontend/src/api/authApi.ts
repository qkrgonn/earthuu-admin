import { request } from "./client";

export const authApi = {
  login: (email: string, password: string) => request<void>("/auth/login", { method: "POST", body: JSON.stringify({ email, password }) }),
  logout: () => request<void>("/auth/logout", { method: "POST" }),
};
