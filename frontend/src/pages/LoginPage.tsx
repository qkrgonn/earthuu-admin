import { useState, type FormEvent } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { useAdmin } from "../store/AdminContext";

export function LoginPage() {
  const { authReady, loggedIn, login, openForgot } = useAdmin();
  const navigate = useNavigate();
  const [email, setEmail] = useState("admin@earthuu.local");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  if (!authReady) return <div className="app-loading">관리자 세션을 확인하고 있습니다.</div>;
  if (loggedIn) return <Navigate to="/dashboard" replace />;

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setSubmitting(true);
    setError("");
    const form = new FormData(event.currentTarget as HTMLFormElement);
    const submittedEmail = String(form.get("email") ?? email).trim();
    const submittedPassword = String(form.get("password") ?? password);
    const loginError = await login(submittedEmail, submittedPassword);
    if (loginError === null) navigate("/dashboard", { replace: true });
    else setError(loginError);
    setSubmitting(false);
  };

  return <div className="login">
    <div className="brand"><span className="logo">e</span>earthuu</div>
    <h1>관리자 로그인</h1><p className="sub">운영 워크스페이스에 접속하세요.</p>
    <form onSubmit={submit}>
      <label htmlFor="email">이메일</label>
      <input id="email" name="email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} required autoComplete="username" />
      <label htmlFor="password">비밀번호</label>
      <input id="password" name="password" type="password" value={password} onChange={(event) => setPassword(event.target.value)} required autoComplete="current-password" />
      <div className="error" role="alert">{error}</div>
      <button className="primary" disabled={submitting}>{submitting ? "로그인 중…" : "관리자 로그인"}</button>
    </form>
    <button className="quiet" style={{ marginTop: 12 }} onClick={openForgot}>비밀번호 재설정</button>
    <div className="login-note">Spring 서버에 등록된 관리자 계정으로 로그인합니다.<br />로컬 계정은 백엔드 환경 변수로 생성할 수 있습니다.</div>
  </div>;
}
