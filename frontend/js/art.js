// Generated SVG artwork used until real photos are added to assets/images/.
// Real photos: <slug>.webp (main) and <slug>-1.webp ... <slug>-3.webp (gallery). They load on top automatically.
(() => {
const COL = { BMW: '#2f7bff', Porsche: '#f0b429', 'Mercedes-AMG': '#c8cdd6', Audi: '#e0213d', Lamborghini: '#c6ff1a', Ferrari: '#e10600' };
const wheel = x => `<circle cx="${x}" cy="300" r="54" fill="#08090b"/><circle cx="${x}" cy="300" r="46" fill="#17191e"/><circle cx="${x}" cy="300" r="30" fill="#a9afb9"/><circle cx="${x}" cy="300" r="22" fill="#2a2d33"/><circle cx="${x}" cy="300" r="7" fill="#d6d9df"/>`;
function body(col, suv) {
  const d = suv ? 'M80 292 C74 262 84 232 106 216 L152 205 L214 162 C226 150 240 146 262 146 L560 146 C586 146 602 154 616 172 L660 214 L722 232 C746 240 752 258 752 292 Z'
                : 'M70 292 C70 270 95 260 140 252 L255 214 C300 182 355 162 425 160 L505 162 C560 166 600 188 640 224 L715 242 C745 250 752 264 752 292 Z';
  const win = suv ? 'M238 170 L268 158 L402 158 L402 208 L226 208 Z M418 158 L558 158 L598 192 L610 208 L418 208 Z'
                  : 'M285 216 L332 186 C366 172 412 168 456 169 L456 216 Z M472 169 L522 171 C554 178 580 198 600 218 L472 218 Z';
  return `<ellipse cx="410" cy="350" rx="370" ry="14" fill="#000" opacity=".55"/><path d="${d}" fill="${col}"/><path d="${d}" fill="url(#sh)"/><path d="${win}" fill="#0a0c10" opacity=".85"/>${wheel(200)}${wheel(615)}<path d="M690 244 L730 252 L728 258 L688 252 Z" fill="#fff"/><path d="M72 266 L94 262 L94 270 L72 274 Z" fill="#ff2a1a"/>`;
}
const defs = col => `<defs><radialGradient id="bg" cx="55%" cy="62%" r="75%"><stop offset="0" stop-color="${col}" stop-opacity=".4"/><stop offset=".55" stop-color="#14161b"/><stop offset="1" stop-color="#07080a"/></radialGradient><linearGradient id="sh" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#000" stop-opacity="0"/><stop offset="1" stop-color="#000" stop-opacity=".55"/></linearGradient></defs>`;
const wrap = (inner, vb) => `<svg xmlns="http://www.w3.org/2000/svg" viewBox="${vb}" preserveAspectRatio="xMidYMid slice">${inner}</svg>`;

function svg(c, i) {
  const col = COL[c.brand] || '#e10600', suv = c.category === 'Luxury SUV';
  let scene;
  if (i < 2) {
    const g = i === 1 ? 'transform="translate(800 0) scale(-1 1)"' : '';
    scene = `<g stroke="${col}" stroke-opacity=".35" stroke-width="3"><path d="M20 190H150M0 222H110M40 254H130"/></g><g ${g}>${body(col, suv)}</g>`;
  } else if (i === 2) {
    const spokes = Array.from({ length: 10 }, (_, k) => `<rect x="390" y="108" width="20" height="97" rx="8" fill="#b4bac4" transform="rotate(${k * 36} 400 205)"/>`).join('');
    scene = `<circle cx="400" cy="205" r="175" fill="#06070a"/><circle cx="400" cy="205" r="150" fill="#101216" stroke="#262a31" stroke-width="4"/><circle cx="400" cy="205" r="118" fill="#9aa1ac"/><circle cx="400" cy="205" r="104" fill="#14161a"/>${spokes}<path d="M470 130 A110 110 0 0 1 512 205 L480 205 A80 80 0 0 0 458 152 Z" fill="${col}"/><circle cx="400" cy="205" r="30" fill="#c9ced6"/><circle cx="400" cy="205" r="12" fill="${col}"/>`;
  } else {
    scene = `<rect x="0" y="250" width="800" height="150" fill="#0b0c0f"/><path d="M0 250 Q400 200 800 250" stroke="${col}" stroke-width="4" fill="none"/><circle cx="250" cy="170" r="80" fill="#07080a" stroke="#2b2f37" stroke-width="6"/><circle cx="550" cy="170" r="80" fill="#07080a" stroke="#2b2f37" stroke-width="6"/><path d="M250 170L205 130" stroke="${col}" stroke-width="5" stroke-linecap="round"/><path d="M550 170L590 210" stroke="${col}" stroke-width="5" stroke-linecap="round"/><rect x="340" y="120" width="120" height="70" rx="8" fill="#0f1a26" stroke="#2b2f37"/><ellipse cx="400" cy="330" rx="190" ry="62" fill="none" stroke="#1d2026" stroke-width="26"/><rect x="215" y="320" width="370" height="20" fill="#1d2026"/><circle cx="400" cy="330" r="22" fill="${col}"/>`;
  }
  const label = `<text x="32" y="470" fill="#fff" opacity=".28" font-family="Arial" font-size="20" letter-spacing="6">${(c.brand + ' ' + c.model).toUpperCase().replace(/&/g, '&amp;')}</text>`;
  return wrap(`${defs(col)}<rect width="800" height="500" fill="url(#bg)"/><g transform="translate(0 50)">${scene}</g>${label}`, '0 0 800 500');
}
const cache = {};
const uri = (c, i) => cache[c.id + '-' + i] ||= 'data:image/svg+xml;utf8,' + encodeURIComponent(svg(c, i));
const cutout = c => 'data:image/svg+xml;utf8,' + encodeURIComponent(wrap(`${defs(COL[c.brand] || '#e10600')}${body(COL[c.brand] || '#e10600', c.category === 'Luxury SUV')}`, '40 130 740 240'));
// Focal point (object-position) per car so portrait photos crop around the car.
const P = {'bmw-m4-competition':'50% 55%','bmw-m8-competition':'50% 58%','porsche-911-carrera-s':'50% 58%','porsche-911-turbo-s':'50% 56%','porsche-718-cayman-gt4-rs':'50% 68%','mercedes-amg-gt-63':'50% 56%','mercedes-amg-g-63':'50% 56%','audi-rs7-performance':'50% 61%','audi-r8-v10-performance':'50% 62%','lamborghini-huracan-evo':'50% 52%','lamborghini-urus':'50% 57%','ferrari-296-gtb':'50% 50%'};
const src = s => (window.PHOTO_DATA && PHOTO_DATA[s]) || `assets/images/${s}.webp`;
const count = c => P[c.slug] ? 1 : 4;
const pic = (c, i) => P[c.slug]
  ? (i ? '' : `<img class="photo" src="${src(c.slug)}" alt="${c.brand} ${c.model}" style="object-position:${P[c.slug]}">`)
  : `<img class="art" src="${uri(c, i)}" alt="${c.brand} ${c.model}"><img class="photo" src="assets/images/${c.slug}${i ? '-' + i : ''}.webp" alt="" onerror="this.remove()">`;
window.Art = { pic, uri, cutout, count, src };
})();
