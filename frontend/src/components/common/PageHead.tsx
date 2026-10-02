import type { ReactNode } from "react";

export function PageHead({ title, description, extra }: { title: string; description: string; extra?: ReactNode }) {
  return (
    <div className="page-head">
      <div><h1>{title}</h1><p className="sub">{description}</p></div>
      {extra}
    </div>
  );
}
