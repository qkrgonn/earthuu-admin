import { Outlet } from "react-router-dom";
import { useAdmin } from "../../store/AdminContext";
import { todayISO } from "../../utils/date";

export function LoginLayout() {
  const { dark, setDark } = useAdmin();
  return <div className="shell">
    <header>
      <div className="crumb" />
      <div className="header-right">
        <button className="button" aria-pressed={dark} onClick={() => setDark(!dark)}>{dark ? "☀ 라이트" : "☾ 다크"}</button>
        <span className="demo-label">예시 데이터</span><span>{todayISO.replaceAll("-", ".")}</span>
      </div>
    </header>
    <main id="main"><Outlet /></main>
    <footer>earthuu admin <span>대학생들의 새로운 연결을 위한 운영 공간</span></footer>
  </div>;
}
