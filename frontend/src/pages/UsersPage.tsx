import { useCallback, useEffect, useState, type FormEvent } from "react";
import { ApiError } from "../api/client";
import { userApi, type ApiUserListItem, type ApiUserPage, type ApiUserRole, type ApiUserStatus } from "../api/userApi";
import { PageHead } from "../components/common/PageHead";
import { StatusBadge } from "../components/common/StatusBadge";
import { Tabs } from "../components/common/Tabs";
import { UserDetailModal } from "../components/users/UserDetailModal";
import { useAdmin } from "../store/AdminContext";

type StatusFilter = "ALL" | ApiUserStatus;
const initialPage: ApiUserPage = { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 };
const date = (value: string) => new Date(value).toLocaleDateString("ko-KR", { timeZone: "Asia/Seoul" });
const displayName = (user: ApiUserListItem) => user.name?.trim() || user.email?.split("@")[0] || "이름 정보 없음";

export function UsersPage() {
  const { notify } = useAdmin();
  const [result, setResult] = useState<ApiUserPage>(initialPage);
  const [queryInput, setQueryInput] = useState("");
  const [query, setQuery] = useState("");
  const [status, setStatus] = useState<StatusFilter>("ALL");
  const [role, setRole] = useState<ApiUserRole>("USER");
  const [sort, setSort] = useState<"NEWEST" | "OLDEST">("NEWEST");
  const [page, setPage] = useState(0);
  const [selectedUserId, setSelectedUserId] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      setResult(await userApi.list({
        query: query || undefined,
        status: status === "ALL" ? undefined : status,
        role,
        sort,
        page,
        size: 20,
      }));
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "사용자 목록을 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  }, [page, query, role, sort, status]);

  useEffect(() => { void load(); }, [load]);

  const search = (event: FormEvent) => {
    event.preventDefault();
    setQuery(queryInput.trim());
    setPage(0);
  };

  const reset = () => {
    setQueryInput(""); setQuery(""); setStatus("ALL"); setRole("USER"); setSort("NEWEST"); setPage(0);
  };

  return <>
    <PageHead title="사용자 관리" description="사용자 계정 상태와 활동 제한을 확인하고 필요한 운영 조치를 처리하세요."
      extra={<button className="button" disabled={loading} onClick={() => void load()}>{loading ? "불러오는 중…" : "새로고침"}</button>} />
    <section className="panel user-panel">
      <Tabs items={[
        { value: "ALL", label: "전체" }, { value: "ACTIVE", label: "활성" },
        { value: "SUSPENDED", label: "정지" }, { value: "WITHDRAWN", label: "탈퇴" },
      ]} value={status} onChange={(value) => { setStatus(value as StatusFilter); setPage(0); }} />
      <form className="toolbar user-toolbar" onSubmit={search}>
        <input aria-label="사용자 검색" placeholder="이름, 이메일, 사용자 ID 검색" value={queryInput}
          onChange={(event) => setQueryInput(event.target.value)} />
        <select aria-label="사용자 역할" value={role} onChange={(event) => { setRole(event.target.value as ApiUserRole); setPage(0); }}>
          <option value="USER">일반 사용자</option><option value="ADMIN">관리자</option>
        </select>
        <select aria-label="가입일 정렬" value={sort} onChange={(event) => { setSort(event.target.value as "NEWEST" | "OLDEST"); setPage(0); }}>
          <option value="NEWEST">최근 가입순</option><option value="OLDEST">오래된 가입순</option>
        </select>
        <button className="primary" type="submit">검색</button><button className="button" type="button" onClick={reset}>초기화</button>
      </form>
      {error && <p className="api-error" role="alert">{error}</p>}
      <div className="count">총 <b>{result.totalElements}</b>명 · 사용자 행을 누르면 계정 상세와 처리 이력을 확인할 수 있습니다.</div>
      <div className="table-wrap"><table className="user-table">
        <thead><tr><th>사용자</th><th>소속</th><th>역할</th><th>계정 상태</th><th>가입일</th></tr></thead>
        <tbody>{loading && !result.content.length
          ? <tr><td colSpan={5} className="empty">사용자 목록을 불러오고 있습니다.</td></tr>
          : result.content.length ? result.content.map((user) => <tr key={user.id} className="clickable-row" onClick={() => setSelectedUserId(user.id)}>
            <td><div className="user-identity"><span className="user-avatar user-avatar-small">{displayName(user).slice(0, 1).toUpperCase()}</span><span><button type="button" className="event-link" onClick={() => setSelectedUserId(user.id)}>{displayName(user)}</button><small>{user.email ?? user.id}</small></span></div></td>
            <td>{user.universityName ?? "-"}</td><td><span className="user-role-badge">{user.role === "ADMIN" ? "관리자" : "일반 사용자"}</span></td>
            <td><StatusBadge status={user.status.toLowerCase()} /></td><td>{date(user.createdAt)}</td>
          </tr>) : <tr><td colSpan={5} className="empty">조건에 맞는 사용자가 없습니다.</td></tr>}</tbody>
      </table></div>
      {result.totalPages > 1 && <div className="user-pagination" aria-label="사용자 목록 페이지 이동">
        <button className="button" disabled={result.page === 0 || loading} onClick={() => setPage((value) => value - 1)}>이전</button>
        <span><b>{result.page + 1}</b> / {result.totalPages}</span>
        <button className="button" disabled={result.page + 1 >= result.totalPages || loading} onClick={() => setPage((value) => value + 1)}>다음</button>
      </div>}
    </section>
    {selectedUserId && <UserDetailModal userId={selectedUserId} onClose={() => setSelectedUserId(null)} onChanged={load} notify={notify} />}
  </>;
}
