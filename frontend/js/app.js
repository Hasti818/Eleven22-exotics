(() => {
const $ = (s, r = document) => r.querySelector(s), $$ = (s, r = document) => [...r.querySelectorAll(s)];
const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const fmt = n => Number(n).toLocaleString('en-US') + ' DKK';
const app = $('#app'), nav = $('#nav');
let returnTo = null, fleetState = { q: '', cat: '', sort: '' };
const go = h => { location.hash = h; };
const HOME = ['', 'fleet', 'how', 'why'];

function toast(m, bad) {
  const t = $('#toast'); t.textContent = m; t.className = 'show' + (bad ? ' bad' : '');
  clearTimeout(toast.t); toast.t = setTimeout(() => t.className = '', 3200);
}
const routeName = () => location.hash.replace(/^#\/?/, '').split('/')[0];

function renderNav() {
  const u = Api.user();
  nav.classList.toggle('solid', scrollY > 40 || !HOME.includes(routeName()));
  nav.innerHTML = `<a class="logo" href="#/"><span>ELEVEN</span><b>22</b><small>EXOTICS</small></a>
  <nav><a href="#/fleet">Fleet</a><a href="#/how">How it works</a>
  ${u ? '<a href="#/bookings">My bookings</a>' : ''}${u && u.role === 'ADMIN' ? '<a href="#/admin">Admin</a>' : ''}
  ${u ? '<a class="btn btn-sm" href="#/logout">Log out</a>' : '<a class="btn btn-sm" href="#/login">Log in</a>'}</nav>`;
}

/* ---------- Home ---------- */
function homeView(section) {
  app.innerHTML = `
  <section class="hero">
    <div class="hero-photo" style="background-image:url('${Art.src('porsche-718-cayman-gt4-rs')}')"></div>
    <div class="hero-shade"></div>
    <div class="hero-content">
      <p class="eyebrow">Luxury &amp; supercar rental</p>
      <h1>Drive beyond<br><em>ordinary.</em></h1>
      <p class="lead">Born for performance. Rentals from a single day to a full month, and a thrill you won't forget.</p>
      <div class="cta"><a class="btn" href="#/fleet">Explore the fleet</a><a class="btn btn-ghost" href="#/how">Book your experience</a></div>
    </div>
    <div class="stats"><div><b>${Api.cars().length}</b><span>Vehicles</span></div><div><b>800+</b><span>Max horsepower</span></div><div><b>2.7s</b><span>Quickest 0–100 km/h</span></div></div>
  </section>
  <section id="fleet" class="section">
    <div class="section-head"><h2>The <em>fleet</em></h2>
      <div class="controls">
        <input id="search" type="search" placeholder="Search brand or model" value="${esc(fleetState.q)}">
        <select id="category"><option value="">All categories</option>${[...new Set(Api.cars().map(c => c.category))].map(c => `<option ${c === fleetState.cat ? 'selected' : ''}>${c}</option>`).join('')}</select>
        <select id="sort">${[['', 'Sort by'], ['price-asc', 'Lowest price'], ['price-desc', 'Highest price'], ['hp-desc', 'Highest horsepower'], ['year-desc', 'Newest model']].map(([v, l]) => `<option value="${v}" ${v === fleetState.sort ? 'selected' : ''}>${l}</option>`).join('')}</select>
      </div></div>
    <div id="grid" class="grid"></div>
  </section>
  <section id="how" class="section dark"><h2>How it <em>works</em></h2><div class="steps">
    <div><i>01</i><h3>Choose</h3><p>Browse the fleet and pick the car that fits the mood.</p></div>
    <div><i>02</i><h3>Request</h3><p>Select your dates. The price is calculated instantly.</p></div>
    <div><i>03</i><h3>Approval</h3><p>We confirm your request and check availability.</p></div>
    <div><i>04</i><h3>Drive</h3><p>Pick up the keys and enjoy the road.</p></div></div></section>
  <section id="why" class="section"><h2>Why <em>Eleven22</em></h2><div class="steps three">
    <div><h3>Premium vehicles</h3><p>Only performance-focused cars from the best marques.</p></div>
    <div><h3>Flexible periods</h3><p>From a single day to a full month, with better monthly rates.</p></div>
    <div><h3>Verified fleet</h3><p>Every car is inspected and maintained before each rental.</p></div></div></section>`;
  const draw = () => {
    const { q, cat, sort } = fleetState;
    const list = Api.cars().filter(c => (!cat || c.category === cat) && `${c.brand} ${c.model}`.toLowerCase().includes(q.toLowerCase()));
    const S = { 'price-asc': (a, b) => a.daily - b.daily, 'price-desc': (a, b) => b.daily - a.daily, 'hp-desc': (a, b) => b.hp - a.hp, 'year-desc': (a, b) => b.year - a.year };
    if (S[sort]) list.sort(S[sort]);
    $('#grid').innerHTML = list.map(c => `<article class="card"><a class="thumb" href="#/car/${c.id}">
      <span class="badge ${c.status === 'ACTIVE' ? 'ok' : 'off'}">${c.status === 'ACTIVE' ? 'Available' : c.status === 'MAINTENANCE' ? 'Maintenance' : 'Unavailable'}</span>${Art.pic(c, 0)}</a>
      <div class="info"><small>${c.category} · ${c.year}</small><h3>${esc(c.brand)} ${esc(c.model)}</h3>
      <div class="specs"><span>${c.hp} HP</span><span>${c.transmission}</span><span>${c.fuel}</span></div>
      <div class="price-row"><div class="price">${fmt(c.daily)} <span>/ day</span></div><a class="btn btn-sm" href="#/car/${c.id}">View details</a></div></div></article>`).join('') || `<p class="muted">${Api.offline ? 'Cannot reach the server. Is the backend running?' : 'No cars match your filters.'}</p>`;
  };
  $('#search').oninput = e => { fleetState.q = e.target.value; draw(); };
  $('#category').onchange = e => { fleetState.cat = e.target.value; draw(); };
  $('#sort').onchange = e => { fleetState.sort = e.target.value; draw(); };
  draw();
  if (section) setTimeout(() => document.getElementById(section)?.scrollIntoView({ behavior: 'smooth' }), 30);
}

/* ---------- Lightbox ---------- */
function lightbox(c, i) {
  const lb = $('#lightbox'), n = Art.count(c); lb.className = 'open';
  const show = () => {
    lb.innerHTML = `<button class="lb-x" aria-label="Close">×</button><button class="lb-p" aria-label="Previous">‹</button><div class="lb-img">${Art.pic(c, i)}</div><button class="lb-n" aria-label="Next">›</button>`;
    $('.lb-x', lb).onclick = close; $('.lb-p', lb).onclick = () => { i = (i + n - 1) % n; show(); }; $('.lb-n', lb).onclick = () => { i = (i + 1) % n; show(); };
    if (n < 2) { $('.lb-p', lb).remove(); $('.lb-n', lb).remove(); }
  };
  const close = () => { lb.className = ''; lb.innerHTML = ''; document.removeEventListener('keydown', key); };
  const key = e => { if (e.key === 'Escape') close(); if (n > 1 && e.key === 'ArrowLeft') { i = (i + n - 1) % n; show(); } if (n > 1 && e.key === 'ArrowRight') { i = (i + 1) % n; show(); } };
  lb.onclick = e => { if (e.target === lb) close(); };
  document.addEventListener('keydown', key); show();
}

/* ---------- Car details ---------- */
const DESC = {
  'Performance': 'A sharp, brutally quick performance machine that is as comfortable on a daily drive as it is on an open road.',
  'Grand Tourer': 'Long-distance luxury with serious power. Effortless pace, a hand-built feel and room to enjoy it.',
  'Sports Car': 'Precision engineering and razor-sharp handling. A driver\'s car in the purest sense.',
  'Supercar': 'Extreme performance wrapped in a head-turning design. Built to be heard, seen and remembered.',
  'Luxury SUV': 'Commanding presence, supercar-level power and space for the whole crew.',
};
async function carView(id) {
  const c = Api.car(id);
  if (!c) { app.innerHTML = '<section class="page narrow"><h2>Car not <em>found</em></h2><a class="btn" href="#/fleet">Back to fleet</a></section>'; return; }
  let cur = 0; const t = Api.today();
  const specs = [['Engine', c.engine], ['Horsepower', c.hp + ' HP'], ['Transmission', c.transmission], ['Drivetrain', c.drivetrain], ['Fuel type', c.fuel], ['Seats', c.seats], ['0–100 km/h', c.zero + ' s'], ['Top speed', c.top + ' km/h'], ['Status', { ACTIVE: 'Available', MAINTENANCE: 'In maintenance', INACTIVE: 'Unavailable' }[c.status]]];
  const blocked = (await Promise.resolve(Api.blocked(c.id)).catch(() => [])).sort((a, b) => a.start.localeCompare(b.start));
  app.innerHTML = `<section class="page">
    <a class="crumb" href="#/fleet">← Back to fleet</a>
    <div class="detail">
      <div><div class="main-pic" id="mainPic">${Art.pic(c, 0)}<span class="zoom">⤢ Enlarge</span></div>
        ${Art.count(c) > 1 ? `<div class="thumbs">${Array.from({ length: Art.count(c) }, (_, i) => i).map(i => `<button class="thumb-btn ${i ? '' : 'on'}" data-i="${i}" aria-label="Image ${i + 1}">${Art.pic(c, i)}</button>`).join('')}</div>` : ''}</div>
      <div>
        <small class="eyebrow">${c.category} · ${c.year}</small>
        <h1 class="car-title">${esc(c.brand)} <em>${esc(c.model)}</em></h1>
        <p class="muted desc">${DESC[c.category] || ''}</p>
        <div class="prices"><div><span>Per day</span><b>${fmt(c.daily)}</b></div><div><span>Per month (30 days)</span><b>${fmt(c.monthly)}</b></div></div>
        <div class="book-box">
          <h3>Request this car</h3>
          <div class="dates"><label>Start date<input type="date" id="start" min="${t}"></label><label>End date<input type="date" id="end" min="${Api.addDays(t, 1)}"></label></div>
          <label class="block">Note (optional)<textarea id="note" rows="2" placeholder="Anything we should know?"></textarea></label>
          <div id="est" class="est"></div>
          ${blocked.length ? `<p class="muted small">Already booked: ${blocked.map(r => `${r.start} → ${r.end}`).join(', ')}</p>` : ''}
          <button class="btn full" id="bookBtn" ${c.status !== 'ACTIVE' ? 'disabled' : ''}>${c.status !== 'ACTIVE' ? 'Currently unavailable' : Api.user() ? 'Request booking' : 'Log in to book'}</button>
        </div>
      </div>
    </div>
    <h3 class="sub">Specifications</h3>
    <div class="spec-grid">${specs.map(([l, v]) => `<div><span>${l}</span><b>${esc(v)}</b></div>`).join('')}</div>
    <h3 class="sub">Rental rules</h3>
    <ul class="rules"><li>Minimum driver age 25 with a full licence held for at least 2 years.</li><li>Rentals under 30 days use the daily rate. From 30 days, each full 30-day period uses the monthly rate.</li><li>Bookings are requests and are confirmed after admin approval.</li><li>Demo project: no real payments or rentals.</li></ul>
  </section>`;
  $$('.thumb-btn').forEach(b => b.onclick = () => { cur = +b.dataset.i; $('#mainPic').innerHTML = Art.pic(c, cur) + '<span class="zoom">⤢ Enlarge</span>'; $$('.thumb-btn').forEach(x => x.classList.toggle('on', x === b)); });
  $('#mainPic').onclick = () => lightbox(c, cur);
  const upd = () => {
    const s = $('#start').value, e = $('#end').value, box = $('#est');
    if (!s || !e || e <= s) { box.innerHTML = '<span class="muted">Select your dates to see the price</span>'; return; }
    const r = Api.estimate(c, s, e);
    box.innerHTML = `<div>${r.days} day${r.days > 1 ? 's' : ''}${r.months ? ` · ${r.months} × 30-day period${r.rem ? ` + ${r.rem} day${r.rem > 1 ? 's' : ''}` : ''}` : ''}</div><div class="total">${fmt(r.total)}</div><small class="muted">Estimate only. The server calculates the final price.</small>`;
  };
  $('#start').onchange = () => { const s = $('#start').value; if (s) { $('#end').min = Api.addDays(s, 1); if ($('#end').value <= s) $('#end').value = ''; } upd(); };
  $('#end').onchange = upd; upd();
  $('#bookBtn').onclick = async () => {
    if (!Api.user()) { returnTo = '#/car/' + c.id; toast('Please log in to submit a booking'); return go('#/login'); }
    try { await Api.createBooking({ carId: c.id, start: $('#start').value, end: $('#end').value, note: $('#note').value }); toast('Booking requested. Waiting for approval.'); go('#/bookings'); }
    catch (e) { toast(e.message, true); }
  };
}

/* ---------- Auth ---------- */
const pwField = (id, label) => `<label class="block">${label}<span class="pw"><input type="password" id="${id}" required><button type="button" class="eye" data-for="${id}">Show</button></span></label>`;
const wireEyes = () => $$('.eye').forEach(b => b.onclick = () => { const i = $('#' + b.dataset.for); i.type = i.type === 'password' ? 'text' : 'password'; b.textContent = i.type === 'password' ? 'Show' : 'Hide'; });
function done(u) { toast(`Welcome, ${u.firstName}`); const to = returnTo || (u.role === 'ADMIN' ? '#/admin' : '#/bookings'); returnTo = null; go(to); }
function loginView() {
  app.innerHTML = `<section class="page narrow"><div class="form-card"><h2>Welcome <em>back</em></h2>
    <form id="f"><label class="block">Email<input type="email" id="email" required autocomplete="email"></label>${pwField('pw', 'Password')}
    <p class="err" id="err"></p><button class="btn full">Log in</button></form>
    <p class="muted small">New here? <a class="lnk" href="#/register">Create an account</a></p>
    ${Api.demo ? '<p class="muted small demo">Demo logins: <b>admin@eleven22.dk</b> / admin123 · <b>demo@eleven22.dk</b> / demo123</p>' : ''}</div></section>`;
  wireEyes();
  $('#f').onsubmit = async e => { e.preventDefault(); try { done(await Api.login($('#email').value, $('#pw').value)); } catch (x) { $('#err').textContent = x.message; } };
}
function registerView() {
  app.innerHTML = `<section class="page narrow"><div class="form-card"><h2>Join <em>Eleven22</em></h2>
    <form id="f"><div class="dates"><label>First name<input id="fn" required></label><label>Last name<input id="ln" required></label></div>
    <label class="block">Email<input type="email" id="email" required></label><label class="block">Phone (optional)<input id="ph" type="tel"></label>
    ${pwField('pw', 'Password (min. 8 characters)')}<p class="err" id="err"></p><button class="btn full">Create account</button></form>
    <p class="muted small">Already registered? <a class="lnk" href="#/login">Log in</a></p></div></section>`;
  wireEyes();
  $('#f').onsubmit = async e => { e.preventDefault(); try { done(await Api.register({ firstName: $('#fn').value, lastName: $('#ln').value, email: $('#email').value, phone: $('#ph').value, password: $('#pw').value })); } catch (x) { $('#err').textContent = x.message; } };
}

/* ---------- Customer bookings ---------- */
const badge = s => `<span class="st st-${s.toLowerCase()}">${s}</span>`;
async function bookingsView() {
  const u = Api.user(); if (!u) { returnTo = '#/bookings'; return go('#/login'); }
  let list; try { list = await Api.myBookings(); } catch (e) { toast(e.message, true); list = []; }
  const groups = [['Upcoming', b => ['PENDING', 'APPROVED', 'ACTIVE'].includes(b.status)], ['Previous rentals', b => b.status === 'COMPLETED'], ['Cancelled & rejected', b => ['CANCELLED', 'REJECTED'].includes(b.status)]];
  app.innerHTML = `<section class="page"><h2>My <em>bookings</em></h2>${list.length ? '' : '<p class="muted">No bookings yet. <a class="lnk" href="#/fleet">Browse the fleet</a></p>'}
  ${groups.map(([title, f]) => { const rows = list.filter(f); return rows.length ? `<h3 class="sub">${title}</h3><div class="bk-list">${rows.map(b => `<div class="bk">
    <div class="bk-pic">${Art.pic(b.car, 0)}</div>
    <div class="bk-body"><h4>${esc(b.car.brand)} ${esc(b.car.model)}</h4><p class="muted">${b.start} → ${b.end}</p>${b.note ? `<p class="muted small">“${esc(b.note)}”</p>` : ''}</div>
    <div class="bk-side">${badge(b.status)}<b class="price">${fmt(b.total)}</b>${['PENDING', 'APPROVED'].includes(b.status) ? `<button class="btn btn-sm btn-ghost" data-cancel="${b.id}">Cancel</button>` : ''}</div></div>`).join('')}</div>` : ''; }).join('')}</section>`;
  $$('[data-cancel]').forEach(b => b.onclick = async () => { if (!confirm('Cancel this booking?')) return; try { await Api.cancel(b.dataset.cancel); toast('Booking cancelled'); bookingsView(); } catch (e) { toast(e.message, true); } });
}

/* ---------- Admin ---------- */
function adminView() {
  const u = Api.user();
  if (!u || u.role !== 'ADMIN') { app.innerHTML = '<section class="page narrow"><h2>Admin <em>only</em></h2><p class="muted">Log in with an administrator account.</p><a class="btn" href="#/login">Log in</a></section>'; returnTo = '#/admin'; return; }
  const draw = async () => {
    const y = scrollY; let s, bks, cars;
    try { [s, bks, cars] = await Promise.all([Api.stats(), Api.allBookings(), Api.adminCars()]); } catch (e) { toast(e.message, true); return; }
    const acts = b => ({ PENDING: [['approve', 'Approve'], ['reject', 'Reject']], APPROVED: [['activate', 'Activate']], ACTIVE: [['complete', 'Complete']] }[b.status] || []);
    app.innerHTML = `<section class="page"><h2>Admin <em>dashboard</em></h2>
    <div class="tiles">${[['Cars', s.cars], ['Available', s.available], ['In maintenance', s.maintenance], ['Pending bookings', s.pending], ['Active rentals', s.active], ['Customers', s.customers]].map(([l, v]) => `<div class="tile"><b>${v}</b><span>${l}</span></div>`).join('')}</div>
    <h3 class="sub">Booking requests</h3><div class="tbl-wrap"><table><tr><th>#</th><th>Customer</th><th>Car</th><th>Dates</th><th>Total</th><th>Status</th><th></th></tr>
    ${bks.map(b => `<tr><td>${b.id}</td><td>${esc(b.user.firstName)} ${esc(b.user.lastName)}</td><td>${esc(b.car.brand)} ${esc(b.car.model)}</td><td>${b.start} → ${b.end}</td><td>${fmt(b.total)}</td><td>${badge(b.status)}</td><td class="act">${acts(b).map(([a, l]) => `<button class="btn btn-sm ${a === 'reject' ? 'btn-ghost' : ''}" data-act="${a}" data-id="${b.id}">${l}</button>`).join('')}</td></tr>`).join('')}</table></div>
    <h3 class="sub">Fleet management</h3><div class="tbl-wrap"><table><tr><th>Car</th><th>Category</th><th>Daily (DKK)</th><th>Monthly (DKK)</th><th>Status</th></tr>
    ${cars.map(c => `<tr><td>${esc(c.brand)} ${esc(c.model)}</td><td>${esc(c.category)}</td>
      <td><input type="number" min="1" value="${c.daily}" data-car="${c.id}" data-f="daily"></td><td><input type="number" min="1" value="${c.monthly}" data-car="${c.id}" data-f="monthly"></td>
      <td><select data-car="${c.id}" data-f="status">${['ACTIVE', 'MAINTENANCE', 'INACTIVE'].map(v => `<option ${v === c.status ? 'selected' : ''}>${v}</option>`).join('')}</select></td></tr>`).join('')}</table></div>
    <h3 class="sub">Add a car</h3><form id="addCar" class="add-car">
      <input name="brand" placeholder="Brand" required><input name="model" placeholder="Model" required><input name="year" type="number" placeholder="Year"><input name="category" placeholder="Category"><input name="hp" type="number" placeholder="Horsepower"><input name="daily" type="number" placeholder="Daily price" required><input name="monthly" type="number" placeholder="Monthly price" required><button class="btn">Add car</button></form>
    ${Api.resetDemo ? '<p class="muted small" style="margin-top:28px"><a class="lnk" href="#/admin" id="reset">Reset demo data</a></p>' : ''}</section>`;
    $$('[data-act]').forEach(b => b.onclick = async () => { try { await Api.bookingAction(b.dataset.id, b.dataset.act); toast('Booking updated'); draw(); } catch (e) { toast(e.message, true); } });
    $$('[data-car]').forEach(el => el.onchange = async () => { try { await Api.setCar(el.dataset.car, { [el.dataset.f]: el.dataset.f === 'status' ? el.value : +el.value }); toast('Saved'); draw(); } catch (e) { toast(e.message, true); draw(); } });
    $('#addCar').onsubmit = async e => { e.preventDefault(); const f = Object.fromEntries(new FormData(e.target)); try { await Api.addCar({ ...f, daily: +f.daily, monthly: +f.monthly }); toast('Car added'); draw(); } catch (x) { toast(x.message, true); } };
    if ($('#reset')) $('#reset').onclick = async e => { e.preventDefault(); await Api.resetDemo(); toast('Demo data reset'); draw(); };
    scrollTo(0, y);
  };
  draw();
}

/* ---------- Router ---------- */
async function route() {
  await Api.ready;
  const [name, arg] = location.hash.replace(/^#\/?/, '').split('/');
  if (name === 'logout') { Api.logout(); toast('Logged out'); return go('#/'); }
  renderNav();
  const views = { car: () => carView(arg), login: loginView, register: registerView, bookings: bookingsView, admin: adminView };
  if (views[name]) { scrollTo(0, 0); Promise.resolve(views[name]()).catch(e => toast(e.message, true)); }
  else { const keepScroll = !name && app.dataset.v === 'home'; homeView(name || null); app.dataset.v = 'home'; if (!keepScroll && !name) scrollTo(0, 0); return; }
  app.dataset.v = name; renderNav();
}
addEventListener('hashchange', route);
addEventListener('scroll', () => nav.classList.toggle('solid', scrollY > 40 || !HOME.includes(routeName())));
route();
})();
