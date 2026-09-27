import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import { events as initialEvents, financeRecords as initialRecords, members as initialMembers } from '../data/mockData';
import type { EventItem, FinanceRecord, Member } from '../types';

type ChurchData = {
  members: Member[];
  events: EventItem[];
  records: FinanceRecord[];
  saveMember: (member: Omit<Member, 'id'> & { id?: number }) => void;
  deleteMember: (id: number) => void;
  saveEvent: (event: Omit<EventItem, 'id'> & { id?: number }) => void;
  deleteEvent: (id: number) => void;
  saveRecord: (record: Omit<FinanceRecord, 'id'> & { id?: number }) => void;
  deleteRecord: (id: number) => void;
};

const ChurchContext = createContext<ChurchData | null>(null);

function useStoredList<T>(key: string, initial: T[]) {
  const [items, setItems] = useState<T[]>(() => {
    try {
      const saved = localStorage.getItem(key);
      if (saved) {
        const parsed: unknown = JSON.parse(saved);
        if (Array.isArray(parsed)) return parsed as T[];
      }
    } catch { /* Invalid or unavailable storage: use example data. */ }
    return initial;
  });
  useEffect(() => {
    try { localStorage.setItem(key, JSON.stringify(items)); } catch { /* Storage unavailable. */ }
  }, [key, items]);
  return [items, setItems] as const;
}

function nextId<T extends { id: number }>(items: T[]) {
  return Math.max(0, ...items.map((item) => item.id)) + 1;
}

export function ChurchProvider({ children }: { children: ReactNode }) {
  const [members, setMembers] = useStoredList<Member>('iglesiaflow-members-v1', initialMembers);
  const [events, setEvents] = useStoredList<EventItem>('iglesiaflow-events-v1', initialEvents);
  const [records, setRecords] = useStoredList<FinanceRecord>('iglesiaflow-finance-v1', initialRecords);

  const saveMember: ChurchData['saveMember'] = (member) => setMembers((current) => member.id
    ? current.map((item) => item.id === member.id ? { ...member, id: member.id! } : item)
    : [{ ...member, id: nextId(current) }, ...current]);
  const saveEvent: ChurchData['saveEvent'] = (event) => setEvents((current) => event.id
    ? current.map((item) => item.id === event.id ? { ...event, id: event.id! } : item)
    : [{ ...event, id: nextId(current) }, ...current]);
  const saveRecord: ChurchData['saveRecord'] = (record) => setRecords((current) => record.id
    ? current.map((item) => item.id === record.id ? { ...record, id: record.id! } : item)
    : [{ ...record, id: nextId(current) }, ...current]);

  return <ChurchContext.Provider value={{
    members, events, records, saveMember, saveEvent, saveRecord,
    deleteMember: (id) => setMembers((current) => current.filter((item) => item.id !== id)),
    deleteEvent: (id) => setEvents((current) => current.filter((item) => item.id !== id)),
    deleteRecord: (id) => setRecords((current) => current.filter((item) => item.id !== id)),
  }}>{children}</ChurchContext.Provider>;
}

export function useChurch() {
  const context = useContext(ChurchContext);
  if (!context) throw new Error('useChurch must be used inside ChurchProvider');
  return context;
}
