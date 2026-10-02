import { useEffect, type ReactNode } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { ModalHost } from "./components/common/ModalHost";
import { AdminLayout } from "./components/layout/AdminLayout";
import { LoginLayout } from "./components/layout/LoginLayout";
import { DashboardPage } from "./pages/DashboardPage";
import { EventsPage } from "./pages/EventsPage";
import { LoginPage } from "./pages/LoginPage";
import { MyReviewsPage } from "./pages/MyReviewsPage";
import { ReportsPage } from "./pages/ReportsPage";
import { StatisticsPage } from "./pages/StatisticsPage";
import { useAdmin } from "./store/AdminContext";

function Protected({ children }: { children: ReactNode }) {
  const { authReady, loggedIn } = useAdmin();
  if (!authReady) return <div className="app-loading">관리자 세션을 확인하고 있습니다.</div>;
  return loggedIn ? children : <Navigate to="/login" replace />;
}

export default function App() {
  const { authReady, loggedIn, toast } = useAdmin();
  useEffect(() => {
    document.body.classList.toggle("signed-out", !loggedIn);
    return () => document.body.classList.remove("signed-out");
  }, [loggedIn]);

  return <>
    <Routes>
      <Route element={<LoginLayout />}><Route path="/login" element={<LoginPage />} /></Route>
      <Route element={<Protected><AdminLayout /></Protected>}>
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/review" element={<EventsPage mode="review" />} />
        <Route path="/events" element={<EventsPage mode="management" />} />
        <Route path="/my-reviews" element={<MyReviewsPage />} />
        <Route path="/reports" element={<ReportsPage />} />
        <Route path="/stats" element={<StatisticsPage />} />
      </Route>
      <Route path="*" element={authReady ? <Navigate to={loggedIn ? "/dashboard" : "/login"} replace /> : <div className="app-loading">관리자 세션을 확인하고 있습니다.</div>} />
    </Routes>
    <ModalHost />
    <div id="toast" role="status" aria-live="polite" style={{ display: toast ? "block" : "none" }}>{toast}</div>
  </>;
}
