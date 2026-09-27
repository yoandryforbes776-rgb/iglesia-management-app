import { Member, EventItem, FinanceRecord } from '../types';

export const stats = [
  { label: 'Miembros activos', value: '1,248', delta: '+12%', trend: 'primary' },
  { label: 'Asistencia domingo', value: '86%', delta: '+4%', trend: 'secondary' },
  { label: 'Eventos este mes', value: '08', delta: '+2', trend: 'success' },
  { label: 'Ingresos del mes', value: '$12.4K', delta: '+9%', trend: 'warning' },
];

export const members: Member[] = [
  { id: 1, name: 'María López', role: 'Líder de jóvenes', ministry: 'Jóvenes', status: 'Activo', attendance: 94 },
  { id: 2, name: 'Carlos Gómez', role: 'Presbítero', ministry: 'Pastoral', status: 'Activo', attendance: 96 },
  { id: 3, name: 'Ana Torres', role: 'Coordinadora', ministry: 'Mujeres', status: 'En revisión', attendance: 81 },
  { id: 4, name: 'José Reyes', role: 'Voluntario', ministry: 'Mantenimiento', status: 'Inactivo', attendance: 52 },
];

export const events: EventItem[] = [
  { id: 1, title: 'Servicio dominical', date: '2026-09-27', location: 'Auditorio principal', capacity: 300, registered: 240, status: 'Confirmado' },
  { id: 2, title: 'Conferencia de jóvenes', date: '2026-10-03', location: 'Salón juvenil', capacity: 150, registered: 110, status: 'Pendiente' },
  { id: 3, title: 'Encuentro familiar', date: '2026-10-12', location: 'Patio central', capacity: 200, registered: 170, status: 'Confirmado' },
];

export const financeRecords: FinanceRecord[] = [
  { id: 1, concept: 'Diezmos del domingo', category: 'Diezmos', amount: 2450, date: '2026-09-22' },
  { id: 2, concept: 'Ofrenda de oración', category: 'Ofrendas', amount: 850, date: '2026-09-20' },
  { id: 3, concept: 'Evento de jóvenes', category: 'Eventos', amount: 1320, date: '2026-09-15' },
  { id: 4, concept: 'Mantenimiento del salón', category: 'Mantenimiento', amount: 690, date: '2026-09-10' },
];
