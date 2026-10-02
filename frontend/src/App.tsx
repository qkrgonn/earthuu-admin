import { useEffect, type ReactNode } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { ModalHost } from "./components/common/ModalHost";
import { AdminLayout } from "./components/layout/AdminLayout";
import { LoginLayout } from "./components/layout/LoginLayout";
import { DashboardPage } from "./pages/DashboardPage";
import { EventsPage } from "./pages/EventsPage";
import { LoginPage } from "./pages/LoginPage";
import { ReportsPage } from "./pages/ReportsPage";
import { StatisticsPage } from "./pages/StatisticsPage";
import { useAdmin } from "./store/AdminContext";

function Protected({ children }: { children: ReactNode }) {
  const { loggedIn } = useAdmin();
  return loggedIn ? children : <Navigate to="/login" replace />;
}

export default function App() {
  const { loggedIn, toast } = useAdmin();
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
        <Route path="/reports" element={<ReportsPage />} />
        <Route path="/stats" element={<StatisticsPage />} />
      </Route>
      <Route path="*" element={<Navigate to={loggedIn ? "/dashboard" : "/login"} replace />} />
    </Routes>
    <ModalHost />
    <div id="toast" role="status" aria-live="polite" style={{ display: toast ? "block" : "none" }}>{toast}</div>
  </>;
}
