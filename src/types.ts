export type MemberStatus = 'Activo' | 'En revisión' | 'Inactivo';
export type EventStatus = 'Confirmado' | 'Pendiente' | 'Cerrado';
export type FinanceType = 'Ingreso' | 'Gasto';

export type Member = {
  id: number;
  name: string;
  email: string;
  phone: string;
  role: string;
  ministry: string;
  status: MemberStatus;
  attendance: number;
  joinedAt: string;
};

export type EventItem = {
  id: number;
  title: string;
  date: string;
  time: string;
  location: string;
  category: string;
  capacity: number;
  registered: number;
  status: EventStatus;
};

export type FinanceRecord = {
  id: number;
  concept: string;
  category: string;
  type: FinanceType;
  amount: number;
  date: string;
};
