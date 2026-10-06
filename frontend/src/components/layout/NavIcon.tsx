import type { ReactNode } from "react";

const paths: Record<string, ReactNode> = {
  dashboard: <><rect x="3" y="3" width="7" height="7" rx="1.5" /><rect x="14" y="3" width="7" height="7" rx="1.5" /><rect x="3" y="14" width="7" height="7" rx="1.5" /><rect x="14" y="14" width="7" height="7" rx="1.5" /></>,
  review: <><path d="M9 3h6v4H9zM7 5H5v16h14V5h-2M8 12h8M8 16h5" /></>,
  events: <><rect x="3" y="5" width="18" height="16" rx="2" /><path d="M7 3v4m10-4v4M3 11h18" /></>,
  reports: <><path d="M12 3 3 7v5c0 5 9 9 9 9s9-4 9-9V7zM12 8v5m0 3v1" /></>,
  users: <><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" /><circle cx="9" cy="7" r="4" /><path d="M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" /></>,
  verifications: <><path d="M12 3 4 6v6c0 4.8 3.4 7.5 8 9 4.6-1.5 8-4.2 8-9V6z" /><path d="m8.5 12 2.2 2.2 4.8-5" /></>,
  "master-data": <><ellipse cx="12" cy="5" rx="8" ry="3" /><path d="M4 5v6c0 1.7 3.6 3 8 3s8-1.3 8-3V5M4 11v6c0 1.7 3.6 3 8 3s8-1.3 8-3v-6" /></>,
  notifications: <><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4" /></>,
  stats: <><path d="M4 3v17h17M8 16v-5m5 5V7m5 9V4" /></>,
};

export function NavIcon({ name }: { name: string }) {
  return <span className="ico"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">{paths[name] ?? paths.events}</svg></span>;
}
