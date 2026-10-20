// Offline demo backend (localStorage) with the same interface as api.js and the same booking rules.
// Used for the hosted preview. The real app uses api.js, which calls the Spring Boot API.
(() => {
const KEY = 'e22_db_v3';
const iso = d => d.toISOString().slice(0, 10);
const today = () => iso(new Date());
const addDays = (s, n) => { const d = new Date(s + 'T00:00:00Z'); d.setUTCDate(d.getUTCDate() + n); return iso(d); };
const diffDays = (a, b) => Math.round((new Date(b + 'T00:00:00Z') - new Date(a + 'T00:00:00Z')) / 864e5);
const sha = async s => [...new Uint8Array(await crypto.subtle.digest('SHA-256', new TextEncoder().encode(s)))].map(x => x.toString(16).padStart(2, '0')).join('');
const fail = m => { throw new Error(m); };

// slug, brand, model, year, category, hp, transmission, daily, monthly, engine, drivetrain, fuel, seats, 0-100, top speed
// Year, power and performance figures are approximate: verify against manufacturer data before publishing.
const ROWS = [
['bmw-m4-competition','BMW','M4 Competition',2024,'Performance',510,'Automatic',2499,49999,'3.0L twin-turbo inline-6','AWD','Petrol',4,3.5,250],
['bmw-m8-competition','BMW','M8 Competition',2024,'Grand Tourer',625,'Automatic',3499,69999,'4.4L twin-turbo V8','AWD','Petrol',4,3.2,250],
['porsche-911-carrera-s','Porsche','911 Carrera S',2024,'Sports Car',443,'Automatic',3299,64999,'3.0L twin-turbo flat-6','RWD','Petrol',4,3.5,308],
['porsche-911-turbo-s','Porsche','911 Turbo S',2024,'Supercar',650,'Automatic',5499,104999,'3.7L twin-turbo flat-6','AWD','Petrol',4,2.7,322],
['porsche-718-cayman-gt4-rs','Porsche','718 Cayman GT4 RS',2023,'Sports Car',500,'Automatic',5999,114999,'4.0L naturally aspirated flat-6','RWD','Petrol',2,3.4,315],
['mercedes-amg-gt-63','Mercedes-AMG','GT 63',2024,'Grand Tourer',639,'Automatic',3799,74999,'4.0L twin-turbo V8','AWD','Petrol',4,3.2,315],
['mercedes-amg-g-63','Mercedes-AMG','G 63',2024,'Luxury SUV',577,'Automatic',3999,79999,'4.0L twin-turbo V8','AWD','Petrol',5,4.5,220],
['audi-rs7-performance','Audi','RS7 Performance',2024,'Performance',621,'Automatic',2999,59999,'4.0L twin-turbo V8','AWD','Petrol',5,3.4,250],
['audi-r8-v10-performance','Audi','R8 V10 Performance',2023,'Supercar',602,'Automatic',4499,89999,'5.2L naturally aspirated V10','AWD','Petrol',2,3.1,331],
['lamborghini-huracan-evo','Lamborghini','Huracán EVO',2023,'Supercar',631,'Automatic',7499,144999,'5.2L naturally aspirated V10','AWD','Petrol',2,2.9,325],
['lamborghini-urus','Lamborghini','Urus',2024,'Luxury SUV',641,'Automatic',5999,114999,'4.0L twin-turbo V8','AWD','Petrol',5,3.6,305],
['ferrari-296-gtb','Ferrari','296 GTB',2024,'Supercar',819,'Automatic',8999,169999,'3.0L twin-turbo V6 plug-in hybrid','RWD','Hybrid',2,2.9,330],
];

function estimate(car, start, end) {
  const days = diffDays(start, end);
  if (days < 1) return { days: 0, months: 0, rem: 0, total: 0 };
  if (days >= 30) { const months = Math.floor(days / 30), rem = days % 30; return { days, months, rem, total: months * car.monthly + rem * car.daily }; }
  return { days, months: 0, rem: days, total: days * car.daily };
}

async function seed() {
  const cars = ROWS.map((r, i) => ({ id: i + 1, slug: r[0], brand: r[1], model: r[2], year: r[3], category: r[4], hp: r[5], transmission: r[6], daily: r[7], monthly: r[8], engine: r[9], drivetrain: r[10], fuel: r[11], seats: r[12], zero: r[13], top: r[14], status: 'ACTIVE' }));
  const users = [
    { id: 1, firstName: 'Admin', lastName: 'Eleven22', email: 'admin@eleven22.dk', phone: '', role: 'ADMIN', hash: await sha('admin1234@'), createdAt: today() },
    { id: 2, firstName: 'Demo', lastName: 'Customer', email: 'demo@eleven22.dk', phone: '', role: 'CUSTOMER', hash: await sha('demo123'), createdAt: today() },
  ];
  const t = today();
  const bookings = [
    { id: 1, userId: 2, carId: 12, start: addDays(t, 10), end: addDays(t, 13), status: 'PENDING', note: 'Weekend drive', adminNote: '', createdAt: t },
    { id: 2, userId: 2, carId: 4, start: addDays(t, -20), end: addDays(t, -17), status: 'COMPLETED', note: '', adminNote: '', createdAt: addDays(t, -25) },
  ];
  bookings.forEach(b => b.total = estimate(cars[b.carId - 1], b.start, b.end).total);
  return { cars, users, bookings, session: null, seq: { car: 13, user: 3, booking: 3 } };
}

let db;
const save = () => { try { localStorage.setItem(KEY, JSON.stringify(db)); } catch {} };
const ready = (async () => {
  try { db = JSON.parse(localStorage.getItem(KEY)); } catch {}
  if (!db) { db = await seed(); save(); }
})();

const user = () => db.users.find(u => u.id === db.session) || null;
const need = () => user() || fail('Please log in first');
const admin = () => { const u = need(); if (u.role !== 'ADMIN') fail('Administrators only'); return u; };
const car = id => db.cars.find(c => c.id === +id);
const join = b => ({ ...b, car: car(b.carId), user: db.users.find(u => u.id === b.userId) });
const BLOCKING = ['PENDING', 'APPROVED', 'ACTIVE'];
const blocked = id => db.bookings.filter(b => b.carId === +id && BLOCKING.includes(b.status)).map(b => ({ start: b.start, end: b.end }));

window.Api = {
  demo: true, adminCars: () => (admin(), db.cars),
  ready, today, addDays, estimate, user, car, blocked,
  cars: () => db.cars,
  async register(f) {
    const email = (f.email || '').trim().toLowerCase();
    if (!f.firstName?.trim() || !f.lastName?.trim()) fail('First and last name are required');
    if (!/^\S+@\S+\.\S+$/.test(email)) fail('Enter a valid email address');
    if ((f.password || '').length < 8) fail('Password must be at least 8 characters');
    if (db.users.some(u => u.email === email)) fail('An account with this email already exists');
    const u = { id: db.seq.user++, firstName: f.firstName.trim(), lastName: f.lastName.trim(), email, phone: f.phone || '', role: 'CUSTOMER', hash: await sha(f.password), createdAt: today() };
    db.users.push(u); db.session = u.id; save(); return u;
  },
  async login(email, pw) {
    const u = db.users.find(x => x.email === (email || '').trim().toLowerCase());
    if (!u || u.hash !== await sha(pw || '')) fail('Invalid email or password');
    db.session = u.id; save(); return u;
  },
  logout() { db.session = null; save(); },
  createBooking({ carId, start, end, note }) {
    const u = need(), c = car(carId);
    if (!c) fail('Car not found');
    if (c.status !== 'ACTIVE') fail('This car is not available for rental');
    if (!start || !end) fail('Select a start and end date');
    if (start < today()) fail('The start date cannot be in the past');
    if (end <= start) fail('The end date must be after the start date');
    if (blocked(c.id).some(r => start < r.end && end > r.start)) fail('Those dates overlap an existing booking');
    const b = { id: db.seq.booking++, userId: u.id, carId: c.id, start, end, total: estimate(c, start, end).total, status: 'PENDING', note: note || '', adminNote: '', createdAt: today() };
    db.bookings.push(b); save(); return join(b);
  },
  myBookings: () => db.bookings.filter(b => b.userId === need().id).sort((a, b) => b.start.localeCompare(a.start)).map(join),
  cancel(id) {
    const b = db.bookings.find(x => x.id === +id && x.userId === need().id);
    if (!b) fail('Booking not found');
    if (!['PENDING', 'APPROVED'].includes(b.status)) fail('This booking can no longer be cancelled');
    b.status = 'CANCELLED'; save();
  },
  stats() {
    admin();
    const t = today();
    const rented = new Set(db.bookings.filter(b => b.status === 'ACTIVE').map(b => b.carId));
    return {
      cars: db.cars.length,
      available: db.cars.filter(c => c.status === 'ACTIVE' && !rented.has(c.id)).length,
      maintenance: db.cars.filter(c => c.status === 'MAINTENANCE').length,
      pending: db.bookings.filter(b => b.status === 'PENDING').length,
      active: rented.size,
      customers: db.users.filter(u => u.role === 'CUSTOMER').length,
    };
  },
  allBookings: () => (admin(), db.bookings.slice().sort((a, b) => b.id - a.id).map(join)),
  bookingAction(id, act) {
    admin();
    const b = db.bookings.find(x => x.id === +id);
    const map = { approve: ['PENDING', 'APPROVED'], reject: ['PENDING', 'REJECTED'], activate: ['APPROVED', 'ACTIVE'], complete: ['ACTIVE', 'COMPLETED'] };
    const [from, to] = map[act] || fail('Unknown action');
    if (!b || b.status !== from) fail(`Only ${from} bookings can be changed this way`);
    b.status = to; save();
  },
  setCar(id, patch) {
    admin(); const c = car(id); if (!c) fail('Car not found');
    if (patch.daily != null && !(patch.daily > 0)) fail('Price must be positive');
    Object.assign(c, patch); save();
  },
  addCar(f) {
    admin();
    if (!f.brand || !f.model || !(f.daily > 0) || !(f.monthly > 0)) fail('Brand, model and prices are required');
    const slug = (f.brand + '-' + f.model).toLowerCase().replace(/[^a-z0-9]+/g, '-');
    db.cars.push({ id: db.seq.car++, slug, brand: f.brand, model: f.model, year: +f.year || 2024, category: f.category || 'Supercar', hp: +f.hp || 500, transmission: 'Automatic', daily: +f.daily, monthly: +f.monthly, engine: f.engine || 'V8', drivetrain: 'AWD', fuel: 'Petrol', seats: 2, zero: 3.5, top: 300, status: 'ACTIVE' });
    save();
  },
  async resetDemo() { db = await seed(); save(); },
};
})();
