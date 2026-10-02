import type { ReactNode } from "react";

const paths: Record<string, ReactNode> = {
  dashboard: <><rect x="3" y="3" width="7" height="7" rx="1.5" /><rect x="14" y="3" width="7" height="7" rx="1.5" /><rect x="3" y="14" width="7" height="7" rx="1.5" /><rect x="14" y="14" width="7" height="7" rx="1.5" /></>,
  review: <><path d="M9 3h6v4H9zM7 5H5v16h14V5h-2M8 12h8M8 16h5" /></>,
  events: <><rect x="3" y="5" width="18" height="16" rx="2" /><path d="M7 3v4m10-4v4M3 11h18" /></>,
  reports: <><path d="M12 3 3 7v5c0 5 9 9 9 9s9-4 9-9V7zM12 8v5m0 3v1" /></>,
  stats: <><path d="M4 3v17h17M8 16v-5m5 5V7m5 9V4" /></>,
};

export function NavIcon({ name }: { name: string }) {
  return <span className="ico"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">{paths[name] ?? paths.events}</svg></span>;
}
