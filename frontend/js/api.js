// Live API client for the Spring Boot backend. Same interface as api-mock.js (the offline demo).
(() => {
const BASE = window.API_BASE || 'http://localhost:8080';
const TOKEN = 'e22_token';
const iso = d => d.toISOString().slice(0, 10);
const today = () => iso(new Date());
const addDays = (s, n) => { const d = new Date(s + 'T00:00:00Z'); d.setUTCDate(d.getUTCDate() + n); return iso(d); };
const diffDays = (a, b) => Math.round((new Date(b + 'T00:00:00Z') - new Date(a + 'T00:00:00Z')) / 864e5);
const slugify = s => s.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');

// Client-side estimate only. The server always calculates the final price.
function estimate(car, start, end) {
  const days = diffDays(start, end);
  if (days < 1) return { days: 0, months: 0, rem: 0, total: 0 };
  if (days >= 30) { const months = Math.floor(days / 30), rem = days % 30; return { days, months, rem, total: months * car.monthly + rem * car.daily }; }
  return { days, months: 0, rem: days, total: days * car.daily };
}

let cars = [], me = null;
const store = { get: () => { try { return localStorage.getItem(TOKEN); } catch { return null; } }, set: t => { try { t ? localStorage.setItem(TOKEN, t) : localStorage.removeItem(TOKEN); } catch {} } };

async function http(method, path, body) {
  const headers = { 'Content-Type': 'application/json' };
  const t = store.get(); if (t) headers.Authorization = 'Bearer ' + t;
  let res;
  try { res = await fetch(BASE + path, { method, headers, body: body ? JSON.stringify(body) : undefined }); }
  catch { throw new Error('Cannot reach the server. Is the backend running on ' + BASE + '?'); }
  if (res.status === 204) return null;
  let data = null; try { data = await res.json(); } catch {}
  if (!res.ok) {
    if (res.status === 401 && t) { store.set(null); me = null; }
    throw new Error(data?.fieldErrors ? Object.values(data.fieldErrors)[0] : data?.message || (res.status === 401 ? 'Please log in' : res.status === 403 ? 'Not allowed' : 'Request failed'));
  }
  return data;
}

// Map backend DTOs to the shape the UI uses.
const toCar = d => ({ id: d.id, brand: d.brand, model: d.model, year: d.modelYear, category: d.category, description: d.description,
  hp: d.horsepower, transmission: d.transmission, daily: +d.dailyPrice, monthly: +d.monthlyPrice, engine: d.engine, drivetrain: d.drivetrain,
  fuel: d.fuelType, seats: d.seats, zero: +d.zeroToOneHundred, top: d.topSpeed, status: d.status,
  slug: (d.mainImageUrl || '').match(/([^/]+)\.\w+$/)?.[1] || slugify(d.brand + ' ' + d.model) });
const toUser = u => ({ id: u.id, firstName: u.firstName, lastName: u.lastName, email: u.email, phone: u.phoneNumber, role: u.role });
const toBooking = b => ({ id: b.id, carId: b.car.id, start: b.startDate, end: b.endDate, total: +b.totalPrice, status: b.status, note: b.customerNote,
  car: { id: b.car.id, brand: b.car.brand, model: b.car.model, category: '', slug: (b.car.mainImageUrl || '').match(/([^/]+)\.\w+$/)?.[1] || slugify(b.car.brand + ' ' + b.car.model) },
  user: toUser(b.customer) });

async function loadCars() { cars = (await http('GET', '/api/cars')).map(toCar); }
async function session(data) { store.set(data.token); me = toUser(data.user); return me; }

const api = {
  today, addDays, estimate, offline: false,
  ready: (async () => {
    try { await loadCars(); } catch { api.offline = true; }
    if (store.get()) { try { me = toUser(await http('GET', '/api/auth/me')); } catch { store.set(null); } }
  })(),
  user: () => me,
  cars: () => cars,
  car: id => cars.find(c => c.id === +id),
  blocked: async id => (await http('GET', `/api/cars/${id}/availability`)).blockedRanges.map(r => ({ start: r.start, end: r.end })),

  register: async f => session(await http('POST', '/api/auth/register', { firstName: f.firstName, lastName: f.lastName, email: f.email, password: f.password, phoneNumber: f.phone })),
  login: async (email, password) => session(await http('POST', '/api/auth/login', { email, password })),
  logout() { store.set(null); me = null; },

  createBooking: async ({ carId, start, end, note }) => toBooking(await http('POST', '/api/bookings', { carId, startDate: start, endDate: end, customerNote: note })),
  myBookings: async () => (await http('GET', '/api/bookings/my')).map(toBooking),
  cancel: async id => { await http('PATCH', `/api/bookings/${id}/cancel`); },

  stats: async () => { const s = await http('GET', '/api/admin/stats'); return { cars: s.cars, available: s.availableCars, maintenance: s.maintenanceCars, pending: s.pendingBookings, active: s.activeRentals, customers: s.customers }; },
  allBookings: async () => (await http('GET', '/api/admin/bookings')).map(toBooking),
  bookingAction: async (id, act) => { await http('PATCH', `/api/admin/bookings/${id}/${act}`); },
  adminCars: async () => (await http('GET', '/api/admin/cars')).map(toCar),
  async setCar(id, patch) {
    if (patch.status) await http('PATCH', `/api/admin/cars/${id}/status`, { status: patch.status });
    else { const c = (await api.adminCars()).find(x => x.id === +id); await http('PATCH', `/api/admin/cars/${id}/prices`, { dailyPrice: patch.daily ?? c.daily, monthlyPrice: patch.monthly ?? c.monthly }); }
    await loadCars();
  },
  async addCar(f) {
    await http('POST', '/api/admin/cars', { brand: f.brand, model: f.model, modelYear: +f.year || 2024, category: f.category || 'Supercar',
      description: 'A new addition to the Eleven22 fleet.', dailyPrice: +f.daily, monthlyPrice: +f.monthly, engine: 'V8', horsepower: +f.hp || 500,
      transmission: 'Automatic', drivetrain: 'AWD', fuelType: 'Petrol', seats: 2, doors: 2, zeroToOneHundred: 3.5, topSpeed: 300,
      mainImageUrl: `assets/images/${slugify(f.brand + ' ' + f.model)}.webp` });
    await loadCars();
  },
};
window.Api = api;
})();
