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
  ["stats", "통계"],
] as const;

export function AdminLayout() {
  const { events, dark, setDark, logout, notify } = useAdmin();
  const location = useLocation();
  const navigate = useNavigate();
  const page = location.pathname.split("/")[1] || "dashboard";
  const reviewCount = events.filter((event) => ["pending", "reviewing"].includes(event.status)).length;
  const handleLogout = () => { logout(); navigate("/login", { replace: true }); };

  return <>
    <aside id="sidebar">
      <NavLink className="brand" to="/dashboard"><span className="logo">e</span>earthuu<span className="admin">ADMIN</span></NavLink>
      <div className="workspace">WORKSPACE</div>
      <nav>{menu.map(([key, label]) => <NavLink key={key} to={`/${key}`}>
        <NavIcon name={key} />{label}{key === "review" && <span className="nav-count">{reviewCount}</span>}
      </NavLink>)}</nav>
      <div className="sidebar-bottom">
        <div className="demo-label">프로토타입</div>
        <p>예시 데이터로 운영 흐름을<br />확인하는 공간입니다.</p>
        <button className="profile" onClick={() => notify("김관리 · 최고관리자 데모 계정")}>
          <span className="avatar">김</span><span><b>김관리</b><small>최고관리자 · 데모</small></span><span>⋯</span>
        </button>
      </div>
    </aside>
    <div className="shell">
      <header>
        <div className="crumb">워크스페이스 <span>/</span> <b>{PAGE_LABELS[page] ?? "대시보드"}</b></div>
        <div className="header-right">
          <button className="button" aria-pressed={dark} aria-label={dark ? "라이트모드 전환" : "다크모드 전환"} onClick={() => setDark(!dark)}>
            {dark ? "☀ 라이트" : "☾ 다크"}
          </button>
          <span className="demo-label">예시 데이터</span><span>{todayISO.replaceAll("-", ".")}</span>
          <button id="logout" className="quiet" onClick={handleLogout}>로그아웃</button>
        </div>
      </header>
      <main id="main"><Outlet /></main>
      <footer>earthuu admin <span>대학생들의 새로운 연결을 위한 운영 공간</span></footer>
    </div>
  </>;
}
