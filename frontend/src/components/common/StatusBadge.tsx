import { STATUS_LABELS } from "../../constants/status";

export function StatusBadge({ status }: { status: string }) {
  const className = ["pending", "received", "applied"].includes(status)
    ? "wait"
    : ["reviewing", "investigating"].includes(status)
      ? "review"
      : ["approved", "confirmed", "resolved"].includes(status)
        ? "ok"
        : ["rejected", "declined", "suspended"].includes(status)
          ? "no"
          : "";
  return <span className={`badge ${className}`}>{STATUS_LABELS[status] ?? status}</span>;
}
