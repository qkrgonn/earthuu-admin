export interface TabItem { value: string; label: string }

export function Tabs({ items, value, onChange }: { items: TabItem[]; value: string; onChange: (value: string) => void }) {
  return <div className="tabs">
    {items.map((item) => <button
      key={item.value}
      className={value === item.value ? "active" : ""}
      aria-pressed={value === item.value}
      onClick={() => onChange(item.value)}
    >{item.label}</button>)}
  </div>;
}
