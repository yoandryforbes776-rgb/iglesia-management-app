import type { Member, EventItem, FinanceRecord } from '../types';

const localISO = (date: Date) => `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
export const todayISO = () => localISO(new Date());
export const offsetDate = (days: number) => {
  const date = new Date();
  date.setHours(12, 0, 0, 0);
  date.setDate(date.getDate() + days);
  return localISO(date);
};
export const nextWeekday = (weekday: number) => {
  const today = new Date();
  return offsetDate((weekday - today.getDay() + 7) % 7 || 7);
};
export const monthDate = (monthsAgo: number, day: number) => {
  const date = new Date();
  date.setDate(1);
  date.setMonth(date.getMonth() - monthsAgo);
  date.setDate(day);
  return localISO(date);
};

export const members: Member[] = [
  { id: 1, name: 'María López', email: 'maria.lopez@correo.com', phone: '555-0101', role: 'Líder de jóvenes', ministry: 'Jóvenes', status: 'Activo', attendance: 94, joinedAt: offsetDate(-7) },
  { id: 2, name: 'Carlos Gómez', email: 'carlos.gomez@correo.com', phone: '555-0102', role: 'Presbítero', ministry: 'Pastoral', status: 'Activo', attendance: 96, joinedAt: offsetDate(-12) },
  { id: 3, name: 'Ana Torres', email: 'ana.torres@correo.com', phone: '555-0103', role: 'Coordinadora', ministry: 'Mujeres', status: 'En revisión', attendance: 81, joinedAt: offsetDate(-3) },
  { id: 4, name: 'José Reyes', email: 'jose.reyes@correo.com', phone: '555-0104', role: 'Voluntario', ministry: 'Servicio', status: 'Inactivo', attendance: 52, joinedAt: offsetDate(-90) },
  { id: 5, name: 'Lucía Fernández', email: 'lucia.fernandez@correo.com', phone: '555-0105', role: 'Líder de alabanza', ministry: 'Alabanza', status: 'Activo', attendance: 91, joinedAt: offsetDate(-18) },
  { id: 6, name: 'David Martínez', email: 'david.martinez@correo.com', phone: '555-0106', role: 'Servidor', ministry: 'Servicio', status: 'Activo', attendance: 88, joinedAt: offsetDate(-24) },
  { id: 7, name: 'Elena Rodríguez', email: 'elena.rodriguez@correo.com', phone: '555-0107', role: 'Maestra', ministry: 'Niños', status: 'Activo', attendance: 93, joinedAt: offsetDate(-32) },
  { id: 8, name: 'Andrés Castillo', email: 'andres.castillo@correo.com', phone: '555-0108', role: 'Voluntario', ministry: 'Jóvenes', status: 'En revisión', attendance: 76, joinedAt: offsetDate(-2) },
  { id: 9, name: 'Sofía Herrera', email: 'sofia.herrera@correo.com', phone: '555-0109', role: 'Coordinadora', ministry: 'Alabanza', status: 'Activo', attendance: 89, joinedAt: offsetDate(-41) },
  { id: 10, name: 'Miguel Pérez', email: 'miguel.perez@correo.com', phone: '555-0110', role: 'Servidor', ministry: 'Servicio', status: 'Activo', attendance: 85, joinedAt: offsetDate(-51) },
  { id: 11, name: 'Valeria Cruz', email: 'valeria.cruz@correo.com', phone: '555-0111', role: 'Maestra', ministry: 'Niños', status: 'Activo', attendance: 92, joinedAt: offsetDate(-15) },
  { id: 12, name: 'Pedro Ramírez', email: 'pedro.ramirez@correo.com', phone: '555-0112', role: 'Voluntario', ministry: 'Pastoral', status: 'Inactivo', attendance: 44, joinedAt: offsetDate(-110) },
];

export const events: EventItem[] = [
  { id: 1, title: 'Servicio dominical', date: nextWeekday(0), time: '10:00', location: 'Auditorio principal', category: 'Servicio', capacity: 300, registered: 240, status: 'Confirmado' },
  { id: 2, title: 'Noche de oración', date: nextWeekday(3), time: '19:00', location: 'Capilla central', category: 'Oración', capacity: 120, registered: 78, status: 'Confirmado' },
  { id: 3, title: 'Conferencia de jóvenes', date: offsetDate(10), time: '18:30', location: 'Salón juvenil', category: 'Jóvenes', capacity: 150, registered: 110, status: 'Pendiente' },
  { id: 4, title: 'Encuentro familiar', date: offsetDate(16), time: '11:00', location: 'Patio central', category: 'Comunidad', capacity: 200, registered: 170, status: 'Confirmado' },
  { id: 5, title: 'Taller de líderes', date: offsetDate(24), time: '09:00', location: 'Sala de formación', category: 'Formación', capacity: 80, registered: 42, status: 'Pendiente' },
  { id: 6, title: 'Jornada solidaria', date: offsetDate(-8), time: '09:00', location: 'Centro comunitario', category: 'Servicio', capacity: 100, registered: 92, status: 'Cerrado' },
];

export const financeRecords: FinanceRecord[] = [
  { id: 1, concept: 'Diezmos del domingo', category: 'Diezmos', type: 'Ingreso', amount: 2450, date: offsetDate(-2) },
  { id: 2, concept: 'Ofrenda de oración', category: 'Ofrendas', type: 'Ingreso', amount: 850, date: offsetDate(-5) },
  { id: 3, concept: 'Donación para misiones', category: 'Donaciones', type: 'Ingreso', amount: 1800, date: offsetDate(-9) },
  { id: 4, concept: 'Mantenimiento del salón', category: 'Mantenimiento', type: 'Gasto', amount: 690, date: offsetDate(-11) },
  { id: 5, concept: 'Diezmos de la semana', category: 'Diezmos', type: 'Ingreso', amount: 2120, date: offsetDate(-15) },
  { id: 6, concept: 'Material para niños', category: 'Ministerios', type: 'Gasto', amount: 320, date: offsetDate(-18) },
  { id: 7, concept: 'Ofrenda dominical', category: 'Ofrendas', type: 'Ingreso', amount: 960, date: offsetDate(-22) },
  { id: 8, concept: 'Servicios generales', category: 'Servicios', type: 'Gasto', amount: 480, date: offsetDate(-24) },
  { id: 9, concept: 'Diezmos del mes anterior', category: 'Diezmos', type: 'Ingreso', amount: 6200, date: monthDate(1, 16) },
  { id: 10, concept: 'Apoyo comunitario', category: 'Ministerios', type: 'Gasto', amount: 1100, date: monthDate(1, 12) },
  { id: 11, concept: 'Ofrendas del mes anterior', category: 'Ofrendas', type: 'Ingreso', amount: 3250, date: monthDate(1, 8) },
  { id: 12, concept: 'Equipamiento', category: 'Mantenimiento', type: 'Gasto', amount: 1250, date: monthDate(2, 10) },
  { id: 13, concept: 'Diezmos', category: 'Diezmos', type: 'Ingreso', amount: 7850, date: monthDate(2, 5) },
  { id: 14, concept: 'Diezmos', category: 'Diezmos', type: 'Ingreso', amount: 7100, date: monthDate(3, 8) },
  { id: 15, concept: 'Gastos operativos', category: 'Servicios', type: 'Gasto', amount: 1820, date: monthDate(3, 13) },
  { id: 16, concept: 'Diezmos', category: 'Diezmos', type: 'Ingreso', amount: 6800, date: monthDate(4, 12) },
  { id: 17, concept: 'Gastos operativos', category: 'Servicios', type: 'Gasto', amount: 1550, date: monthDate(4, 19) },
  { id: 18, concept: 'Diezmos', category: 'Diezmos', type: 'Ingreso', amount: 5900, date: monthDate(5, 11) },
  { id: 19, concept: 'Gastos operativos', category: 'Servicios', type: 'Gasto', amount: 1300, date: monthDate(5, 18) },
];
