# Eleven22 Exotics

A full-stack luxury and supercar rental platform built with Java Spring Boot, PostgreSQL and vanilla JavaScript. Customers browse vehicles, check availability and request rentals, while administrators manage the fleet, pricing, images and bookings.

> Fictional portfolio project. No real payments, not affiliated with any manufacturer. Prices are demo data.

## Features
- Responsive dark sports-car UI: homepage, filterable fleet, car details with gallery and lightbox, login/register, customer bookings, admin dashboard
- REST API with JWT authentication and role-based access (`CUSTOMER`, `ADMIN`), BCrypt password hashing
- Booking rules enforced on the server: dates validated, overlapping bookings rejected, price calculated server-side (daily rate under 30 days, monthly rate per full 30-day block)
- Availability check and insert run in one transaction with a pessimistic lock on the car row, so two customers cannot book the same car at once
- Availability is derived from car status and bookings, not a stored boolean
- Admin: add/edit/deactivate cars, change prices and status, manage images, approve/reject/activate/complete bookings, dashboard statistics
- Consistent error responses, request validation, CORS configuration, secrets via environment variables

## Tech stack
Java 17, Spring Boot 3 (Web, Data JPA, Security, Validation), Hibernate, PostgreSQL, JJWT, Maven, JUnit 5, Mockito, MockMvc with H2 for tests, HTML/CSS/JavaScript (Fetch API, CSS Grid/Flexbox).

## Project structure
```
backend/    Spring Boot API (config, controller, dto, entity, exception, repository, security, service)
frontend/   Static single-page app (hash routing), css/, js/, assets/images/
```

## Run locally
**1. Database**
```sql
CREATE DATABASE eleven22_exotics;
```
**2. Backend**
```bash
cd backend
cp .env.example .env        # then edit the placeholder values
mvn spring-boot:run
```
The API starts on `http://localhost:8080`. On first start it creates the tables, 12 demo cars and the admin account from `ADMIN_EMAIL` / `ADMIN_PASSWORD`.

**3. Frontend** (any static server, the backend allows `localhost:5500` and `localhost:8000` by default)
```bash
cd frontend
python3 -m http.server 8000
```
Open `http://localhost:8000`. If the backend runs elsewhere, set `window.API_BASE` before `js/api.js` loads in `index.html`.

`frontend/js/api-mock.js` is an offline demo of the same API (localStorage, demo logins). To try the UI without a backend, load it instead of `api.js`.

## Tests
```bash
cd backend && mvn test
```
Covers registration, duplicate email, login, car retrieval and filters, missing car, admin-only access, valid and invalid bookings, overlap rejection, price calculation, cancellation, and admin approval/rejection.

## API overview
| Area | Endpoints |
|---|---|
| Auth | `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me` |
| Cars (public) | `GET /api/cars` (filters: brand, category, minPrice, maxPrice, transmission, fuelType), `GET /api/cars/{id}`, `GET /api/cars/{id}/availability?start=&end=` |
| Bookings (customer) | `POST /api/bookings`, `GET /api/bookings/my`, `GET /api/bookings/{id}`, `PATCH /api/bookings/{id}/cancel` |
| Admin cars | `GET/POST /api/admin/cars`, `PUT/DELETE /api/admin/cars/{id}`, `PATCH .../status`, `PATCH .../prices`, `POST .../images`, `DELETE /api/admin/cars/{carId}/images/{imageId}` |
| Admin bookings | `GET /api/admin/bookings`, `PATCH /api/admin/bookings/{id}/approve|reject|activate|complete`, `GET /api/admin/stats` |

## Photos and credits
Car photos live in `frontend/assets/images/<slug>.webp`. Record the creator, source, URL and licence of every image in `IMAGE_CREDITS.md` before publishing.

## Possible next steps
Payments, email confirmations, favourites, reviews, Flyway migrations, refresh tokens, cloud image storage.
