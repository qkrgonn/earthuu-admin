import { useState, type FormEvent } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { useAdmin } from "../store/AdminContext";

export function LoginPage() {
  const { loggedIn, login, openForgot } = useAdmin();
  const navigate = useNavigate();
  const [email, setEmail] = useState("admin@earthuu.demo");
  const [password, setPassword] = useState("earthuu-demo");
  const [error, setError] = useState("");
  if (loggedIn) return <Navigate to="/dashboard" replace />;

  const submit = (event: FormEvent) => {
    event.preventDefault();
    if (login(email, password)) navigate("/dashboard", { replace: true });
    else setError("표시된 데모 계정으로 로그인해 주세요.");
  };

  return <div className="login">
    <div className="brand"><span className="logo">e</span>earthuu</div>
    <h1>관리자 로그인</h1><p className="sub">운영 워크스페이스에 접속하세요.</p>
    <form onSubmit={submit}>
      <label htmlFor="email">이메일</label>
      <input id="email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} required autoComplete="username" />
      <label htmlFor="password">비밀번호</label>
      <input id="password" type="password" value={password} onChange={(event) => setPassword(event.target.value)} required autoComplete="current-password" />
      <div className="error" role="alert">{error}</div>
      <button className="primary">데모 계정으로 로그인</button>
    </form>
    <button className="quiet" style={{ marginTop: 12 }} onClick={openForgot}>비밀번호 재설정</button>
    <div className="login-note">시제품 전용 계정입니다.<br />admin@earthuu.demo / earthuu-demo<br />실제 계정 정보는 입력하지 마세요.</div>
  </div>;
}
