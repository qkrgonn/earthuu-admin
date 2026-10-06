import { request } from "./client";

export interface University {
  id: string;
  nameKo: string;
  nameEn: string | null;
  active: boolean;
}

export interface UniversityDomain {
  domain: string;
  universityId: string;
  verifiedBy: string | null;
  verifiedAt: string | null;
  active: boolean;
}

export interface City {
  code: string;
  officialName: string;
  displayNameKo: string;
  displayNameEn: string | null;
  active: boolean;
}

export interface Category {
  id: string;
  code: string;
  nameKo: string;
  nameEn: string | null;
  active: boolean;
}

const body = (value: unknown): RequestInit => ({ method: "POST", body: JSON.stringify(value) });
const patch = (value: unknown): RequestInit => ({ method: "PATCH", body: JSON.stringify(value) });

export const masterDataApi = {
  universities: () => request<University[]>("/master-data/universities"),
  createUniversity: (nameKo: string, nameEn: string) => request<University>("/master-data/universities", body({ nameKo, nameEn: nameEn || null })),
  updateUniversity: (id: string, nameKo: string, nameEn: string) => request<University>(`/master-data/universities/${id}`, patch({ nameKo, nameEn: nameEn || null })),
  setUniversityActive: (id: string, active: boolean) => request<University>(`/master-data/universities/${id}/${active ? "activate" : "deactivate"}`, { method: "POST" }),
  domains: (id: string) => request<UniversityDomain[]>(`/master-data/universities/${id}/domains`),
  addDomain: (id: string, domain: string) => request<UniversityDomain>(`/master-data/universities/${id}/domains`, body({ domain })),
  setDomainActive: (id: string, domain: string, active: boolean) => request<UniversityDomain>(
    `/master-data/universities/${id}/domains/${encodeURIComponent(domain)}${active ? "/activate" : ""}`,
    { method: active ? "POST" : "DELETE" },
  ),
  cities: () => request<City[]>("/master-data/cities"),
  createCity: (value: Omit<City, "active">) => request<City>("/master-data/cities", body(value)),
  updateCity: (code: string, value: Omit<City, "code" | "active">) => request<City>(`/master-data/cities/${code}`, patch(value)),
  setCityActive: (code: string, active: boolean) => request<City>(`/master-data/cities/${code}/${active ? "activate" : "deactivate"}`, { method: "POST" }),
  categories: () => request<Category[]>("/master-data/categories"),
  createCategory: (value: Omit<Category, "id" | "active">) => request<Category>("/master-data/categories", body(value)),
  updateCategory: (id: string, nameKo: string, nameEn: string) => request<Category>(`/master-data/categories/${id}`, patch({ nameKo, nameEn: nameEn || null })),
  setCategoryActive: (id: string, active: boolean) => request<Category>(`/master-data/categories/${id}/${active ? "activate" : "deactivate"}`, { method: "POST" }),
};
