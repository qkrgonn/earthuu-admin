import { useEffect, useRef, type ReactNode } from "react";

export function Modal({ children, onClose }: { children: ReactNode; onClose: () => void }) {
  const ref = useRef<HTMLDialogElement>(null);
  useEffect(() => {
    const dialog = ref.current;
    if (dialog && !dialog.open) dialog.showModal();
    return () => { if (dialog?.open) dialog.close(); };
  }, []);
  return <dialog
    ref={ref}
    id="detail"
    onCancel={onClose}
    onClick={(event) => { if (event.target === event.currentTarget) onClose(); }}
  ><div id="detail-content">{children}</div></dialog>;
}
