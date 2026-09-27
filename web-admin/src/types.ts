export type Member = {
  id: number;
  name: string;
  role: string;
  ministry: string;
  status: 'Activo' | 'En revisión' | 'Inactivo';
  attendance: number;
};

export type EventItem = {
  id: number;
  title: string;
  date: string;
  location: string;
  capacity: number;
  registered: number;
  status: 'Confirmado' | 'Pendiente' | 'Cerrado';
};

export type FinanceRecord = {
  id: number;
  concept: string;
  category: 'Diezmos' | 'Ofrendas' | 'Eventos' | 'Mantenimiento';
  amount: number;
  date: string;
};
