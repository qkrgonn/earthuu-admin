export const kstDate = new Intl.DateTimeFormat("en-CA", {
  timeZone: "Asia/Seoul",
  year: "numeric",
  month: "2-digit",
  day: "2-digit",
});

export const todayISO = kstDate.format(new Date());

export const todayLabel = new Intl.DateTimeFormat("ko-KR", {
  timeZone: "Asia/Seoul",
  month: "long",
  day: "numeric",
  weekday: "long",
}).format(new Date());

export const shortDate = (value?: string) => value?.slice(5).replace("-", ".") || "—";

export const nowKST = () =>
  new Date().toLocaleString("sv-SE", { timeZone: "Asia/Seoul" }).slice(0, 16);
