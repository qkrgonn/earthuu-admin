import { useState } from "react";
import { NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { PAGE_LABELS } from "../../constants/status";
import { useAdmin } from "../../store/AdminContext";
import { todayISO } from "../../utils/date";
import { NavIcon } from "./NavIcon";

const menu = [
  ["dashboard", "대시보드"],
  ["review", "이벤트 심사"],
  ["events", "이벤트 관리"],
  ["reports", "신고 관리"],
  ["users", "사용자 관리"],
  ["stats", "통계"],
] as const;

export function AdminLayout() {
  const { events, session, dark, setDark, logout } = useAdmin();
  const [accountMenuOpen, setAccountMenuOpen] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const page = location.pathname.split("/")[1] || "dashboard";
  const adminId = session?.email.split("@")[0] ?? "관리자";
  const reviewCount = events.filter((event) => ["pending", "reviewing"].includes(event.status)).length;
  const handleLogout = async () => { await logout(); navigate("/login", { replace: true }); };

  return <>
    <aside id="sidebar">
      <NavLink className="brand" to="/dashboard"><span className="logo">e</span>earthuu<span className="admin">ADMIN</span></NavLink>
      <div className="workspace">WORKSPACE</div>
      <nav>{menu.map(([key, label]) => <NavLink key={key} to={`/${key}`}>
        <NavIcon name={key} />{label}{key === "review" && <span className="nav-count">{reviewCount}</span>}
      </NavLink>)}</nav>
      <div className="sidebar-bottom">
        <div className="demo-label">API 연결됨</div>
        <p>이벤트·사용자 관리 기능은 Spring API와<br />PostgreSQL 데이터를 사용합니다.</p>
        <div className="profile">
          <span className="avatar">A</span>
          <span className="profile-copy"><b>관리자</b><small>{session?.email ?? "ADMIN"}</small></span>
          <button
            type="button"
            className="profile-menu-trigger"
            aria-label="관리자 계정 메뉴 열기"
            aria-expanded={accountMenuOpen}
            aria-controls="account-menu"
            onClick={() => setAccountMenuOpen((open) => !open)}
          >⋯</button>
          {accountMenuOpen && <div id="account-menu" className="profile-menu" role="menu">
            <strong>현재 관리자 계정</strong>
            <span>{session?.email ?? "계정 정보 없음"}</span>
            <div className="profile-permission"><small>권한</small><b>{session?.role === "ADMIN" ? "관리자 (ADMIN)" : "확인 불가"}</b></div>
            <button type="button" role="menuitem" onClick={() => {
              setAccountMenuOpen(false);
              navigate("/my-reviews");
            }}><span className="profile-menu-link-copy"><b>{adminId} 관리자 활동 내역</b><small>이벤트 심사 · 신고 처리</small></span><span aria-hidden="true">→</span></button>
          </div>}
        </div>
      </div>
    </aside>
    <div className="shell">
      <header>
        <div className="crumb">워크스페이스 <span>/</span> <b>{PAGE_LABELS[page] ?? "대시보드"}</b></div>
        <div className="header-right">
          <button className="button" aria-pressed={dark} aria-label={dark ? "라이트모드 전환" : "다크모드 전환"} onClick={() => setDark(!dark)}>
            {dark ? "☀ 라이트" : "☾ 다크"}
          </button>
          <span className="demo-label">Spring API</span><span>{todayISO.replaceAll("-", ".")}</span>
          <button id="logout" className="quiet" onClick={() => void handleLogout()}>로그아웃</button>
        </div>
      </header>
      <main id="main"><Outlet /></main>
      <footer>earthuu admin <span>대학생들의 새로운 연결을 위한 운영 공간</span></footer>
    </div>
  </>;
}
