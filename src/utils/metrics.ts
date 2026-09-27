import type { EventItem, FinanceRecord, Member } from '../types';
import { todayISO } from '../data/mockData';
import { monthKey } from './format';

export const getMetrics = (members: Member[], events: EventItem[], records: FinanceRecord[]) => {
  const active = members.filter((m) => m.status === 'Activo');
  const pending = members.filter((m) => m.status === 'En revisión');
  const upcoming = events.filter((e) => e.date >= todayISO() && e.status !== 'Cerrado').sort((a, b) => a.date.localeCompare(b.date));
  const thisMonth = monthKey(todayISO());
  const monthly = records.filter((r) => monthKey(r.date) === thisMonth);
  const income = monthly.filter((r) => r.type === 'Ingreso').reduce((sum, r) => sum + r.amount, 0);
  const expenses = monthly.filter((r) => r.type === 'Gasto').reduce((sum, r) => sum + r.amount, 0);
  const attendance = active.length ? Math.round(active.reduce((sum, m) => sum + m.attendance, 0) / active.length) : 0;
  return { active, pending, upcoming, income, expenses, balance: income - expenses, attendance };
};
