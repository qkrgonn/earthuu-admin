import { metrics } from "../mock/data";
import type { Channel, MetricItem, Period } from "../types";

export function getStats(period: Period, channel: Channel) {
  const items = metrics.slice(-Number(period));
  const visits = items.reduce((total, item) => total + (channel === "all" ? item.web + item.app : item[channel]), 0);
  return {
    items,
    visits,
    users: Math.round(visits * 0.71),
    active: Math.round(visits * 0.48),
    joins: items.reduce((total, item) => total + item.joins, 0),
  };
}

export function activeIn(days: number) {
  return Math.round(metrics.slice(-days).reduce((total, item) => total + item.web + item.app, 0) * 0.48);
}

export function chartItems(items: MetricItem[]) {
  if (items.length <= 10) return items;
  return Array.from({ length: 6 }, (_, index) => ({
    date: `${index * 5 + 1}–${index * 5 + 5}`,
    web: items.slice(index * 5, index * 5 + 5).reduce((total, item) => total + item.web, 0) / 5,
    app: items.slice(index * 5, index * 5 + 5).reduce((total, item) => total + item.app, 0) / 5,
    joins: 0,
  }));
}
