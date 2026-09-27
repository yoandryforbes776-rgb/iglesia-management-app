export const money = (amount: number) => new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', minimumFractionDigits: Number.isInteger(amount) ? 0 : 2, maximumFractionDigits: 2 }).format(amount);
export const formatDate = (date: string, options?: Intl.DateTimeFormatOptions) => new Intl.DateTimeFormat('es-ES', options ?? { day: 'numeric', month: 'short', year: 'numeric' }).format(new Date(`${date}T12:00:00`));
export const initials = (name: string) => name.trim().split(/\s+/).slice(0, 2).map((part) => part[0]?.toUpperCase() ?? '').join('');
export const monthKey = (date: string) => date.slice(0, 7);
export const downloadCSV = (filename: string, rows: (string | number)[][]) => {
  const csv = rows.map((row) => row.map((value) => {
    const cell = String(value);
    // Prevent spreadsheet formula execution when opening exported user-entered text.
    const safe = /^[\s]*[=+@-]/.test(cell) && typeof value === 'string' ? `\u0027${cell}` : cell;
    return `"${safe.replace(/"/g, '""')}"`;
  }).join(',')).join('\r\n');
  const url = URL.createObjectURL(new Blob(['\uFEFF', csv], { type: 'text/csv;charset=utf-8;' }));
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
};
