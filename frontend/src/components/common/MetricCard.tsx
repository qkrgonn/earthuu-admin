import type { ReactNode } from "react";

interface Props {
  label: string;
  value: string | number;
  note: string;
  unit?: string;
  blue?: boolean;
  onClick?: () => void;
}

export function MetricCard({ label, value, note, unit, blue, onClick }: Props) {
  const content: ReactNode = <>
    <div className="metric-top">{label}<span /></div>
    <span className="value">
      {value}<small>{unit ?? (label.includes("건") || label.includes("신고") || label.includes("심사") ? "건" : "명")}</small>
    </span>
    <div className="metric-note">{note}</div>
  </>;
  return onClick
    ? <button className={`metric ${blue ? "blue" : ""}`} onClick={onClick}>{content}</button>
    : <div className={`metric ${blue ? "blue" : ""}`}>{content}</div>;
}
