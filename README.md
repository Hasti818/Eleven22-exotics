# Eleven22 Exotics

A full-stack luxury and supercar rental platform built with Java Spring Boot, PostgreSQL and vanilla JavaScript. Customers can browse vehicles, check availability and request rentals, while administrators manage the fleet, pricing, images and bookings.

> **Disclaimer:** Eleven22 Exotics is a fictional portfolio project. It does not process real payments and is not affiliated with any vehicle manufacturer. All prices are demonstration data.

## About the name

The name **Eleven22** is inspired by my birthday in two different calendars:

- **Eleven:** 11 February 2006 in the Gregorian calendar
- **22:** 22 Bahman 1384 in the Persian calendar

## Features

### Customer features

- Browse a responsive catalogue of luxury and performance vehicles
- Filter cars by brand, category, price, transmission and fuel type
- Sort cars by price, model year and horsepower
- View detailed specifications and image galleries
- Open vehicle images in a full-screen lightbox
- Register and log in securely
- Check vehicle availability for selected dates
- Receive an automatically calculated rental price
- Request a vehicle rental
- View current and previous bookings
- Cancel eligible bookings

### Administrator features

- Access a protected administrator dashboard
- View fleet and booking statistics
- Add new vehicles
- Edit vehicle information
- Update daily and monthly prices
- Change vehicle status
- Add and remove vehicle images
- Deactivate vehicles
- View all customer bookings
- Approve or reject booking requests
- Mark rentals as active or completed

### Booking and availability rules

- Rental dates are validated by the backend
- Start dates cannot be in the past
- End dates must be after start dates
- Overlapping reservations are rejected
- Availability is calculated from vehicle status and existing bookings
- Availability is not stored as a simple boolean
- Rental prices are always calculated and verified by the backend
- The daily rate is used for rentals under 30 days
- The monthly rate is used for each complete 30-day period
- Availability checking and booking creation run in one transaction
- A pessimistic database lock helps prevent two customers from booking the same car simultaneously

## Tech stack

### Backend

- Java 25
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Hibernate
- Spring Security
- Spring Validation
- JJWT
- Maven

### Database

- PostgreSQL
- H2 for automated tests

### Frontend

- HTML5
- CSS3
- Vanilla JavaScript
- Fetch API
- CSS Grid
- Flexbox
- Responsive design

### Testing

- JUnit 5
- Mockito
- MockMvc
- Postman

## Project structure

```text
eleven22-exotics/
├── backend/
│   └── Spring Boot REST API
│       ├── config/
│       ├── controller/
│       ├── dto/
│       ├── entity/
│       ├── exception/
│       ├── repository/
│       ├── security/
│       └── service/
│
├── frontend/
│   ├── index.html
│   ├── css/
│   ├── js/
│   └── assets/
│       └── images/
│
├── IMAGE_CREDITS.md
└── README.md
```

## Demo access

A dedicated demonstration administrator account is available for exploring the admin dashboard.

### Administrator

- **Email:** `admin@eleven22.dk`
- **Password:** `admin1234@`

The administrator account can be used to test vehicle management, pricing, images and booking-status features.

> These credentials are for demonstration purposes only. Do not use them for another account or connect them to personal information. The demo should use a separate or resettable database.

## Run locally

### Prerequisites

Install the following software before running the project:

- Java 25
- Maven
- PostgreSQL
- Python 3 or another static-file server

### 1. Clone the repository

```bash
git clone https://github.com/Hasti818/Eleven22-exotics.git
cd Eleven22-exotics
```

### 2. Create the PostgreSQL database

Open PostgreSQL and run:

```sql
CREATE DATABASE eleven22_exotics;
```

### 3. Configure the backend

Open the backend directory:

```bash
cd backend
cp .env.example .env
```

Edit `.env` and replace the placeholder values:

```env
DB_URL=jdbc:postgresql://localhost:5432/eleven22_exotics
DB_USERNAME=your_postgresql_username
DB_PASSWORD=your_postgresql_password

JWT_SECRET=your_secure_jwt_secret

ADMIN_EMAIL=your_admin_email
ADMIN_PASSWORD=your_admin_password
```

The `.env` file contains private values and must not be committed to GitHub.

### 4. Start the backend

From the `backend` directory, run:

```bash
mvn spring-boot:run
```

The REST API starts at:

```text
http://localhost:8080
```

On the first start, the application creates the database tables, inserts 12 demonstration vehicles and creates an administrator account using `ADMIN_EMAIL` and `ADMIN_PASSWORD`.

### 5. Start the frontend

Open another terminal window:

```bash
cd frontend
python3 -m http.server 8000
```

Open the application at:

```text
http://localhost:8000
```

The backend allows requests from `localhost:8000` and `localhost:5500` by default.

If the backend is hosted at another address, set `window.API_BASE` before `js/api.js` is loaded in `index.html`.

## Offline mock mode

The file `frontend/js/api-mock.js` provides an offline demonstration of the API using `localStorage`.

To test the interface without running Java or PostgreSQL, load `api-mock.js` instead of `api.js` in `index.html`.

The mock version keeps its data inside the visitor’s browser and does not connect to the real PostgreSQL database.

## API overview

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Register a customer |
| `POST` | `/api/auth/login` | Log in and receive a JWT |
| `GET` | `/api/auth/me` | Get the authenticated user |

### Public vehicle endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/cars` | Retrieve and filter vehicles |
| `GET` | `/api/cars/{id}` | Retrieve one vehicle |
| `GET` | `/api/cars/{id}/availability?start=&end=` | Check availability |

Supported car filters include:

- `brand`
- `category`
- `minPrice`
- `maxPrice`
- `transmission`
- `fuelType`

### Customer booking endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/bookings` | Request a booking |
| `GET` | `/api/bookings/my` | View the customer’s bookings |
| `GET` | `/api/bookings/{id}` | View one booking |
| `PATCH` | `/api/bookings/{id}/cancel` | Cancel an eligible booking |

### Administrator vehicle endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/admin/cars` | Retrieve all vehicles |
| `POST` | `/api/admin/cars` | Add a vehicle |
| `PUT` | `/api/admin/cars/{id}` | Update a vehicle |
| `DELETE` | `/api/admin/cars/{id}` | Deactivate or delete a vehicle |
| `PATCH` | `/api/admin/cars/{id}/status` | Change vehicle status |
| `PATCH` | `/api/admin/cars/{id}/prices` | Update rental prices |
| `POST` | `/api/admin/cars/{id}/images` | Add a vehicle image |
| `DELETE` | `/api/admin/cars/{carId}/images/{imageId}` | Remove an image |

### Administrator booking endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/admin/bookings` | Retrieve all bookings |
| `PATCH` | `/api/admin/bookings/{id}/approve` | Approve a booking |
| `PATCH` | `/api/admin/bookings/{id}/reject` | Reject a booking |
| `PATCH` | `/api/admin/bookings/{id}/activate` | Mark a rental as active |
| `PATCH` | `/api/admin/bookings/{id}/complete` | Mark a rental as completed |

## Security

The project includes:

- BCrypt password hashing
- JWT authentication
- Role-based authorization using `CUSTOMER` and `ADMIN`
- Protected administrator endpoints
- Request validation
- Consistent API error responses
- CORS configuration
- Server-side price calculation
- Transactional availability checking
- Environment variables for passwords and secrets

No real passwords, database credentials or JWT secrets should be committed to the repository.

## Tests

From the backend directory, run:

```bash
mvn test
```

The automated tests cover:

- Customer registration
- Duplicate email rejection
- Successful and unsuccessful login
- Vehicle retrieval and filtering
- Missing-vehicle responses
- Administrator-only access
- Valid booking creation
- Invalid rental dates
- Overlapping booking rejection
- Rental-price calculation
- Booking cancellation
- Administrator approval and rejection

## Vehicle collection

The initial demonstration fleet includes:

- BMW M4 Competition
- BMW M8 Competition
- Porsche 911 Carrera S
- Porsche 911 Turbo S
- Porsche 718 Cayman GT4 RS
- Mercedes-AMG GT 63
- Mercedes-AMG G 63
- Audi RS7 Performance
- Audi R8 V10 Performance
- Lamborghini Huracán EVO
- Lamborghini Urus
- Ferrari 296 GTB

All prices and availability information are fictional demonstration data.

## Photos and credits

Vehicle photos are stored in:

```text
frontend/assets/images/<vehicle-slug>.webp
```

The creator, source, original URL and licence for every photograph should be recorded in `IMAGE_CREDITS.md`.

Vehicle names, logos and trademarks belong to their respective owners. Their inclusion does not imply sponsorship or affiliation.

## Possible future improvements

- Online payment integration
- Email booking confirmations
- Customer favourites
- Vehicle reviews
- Discount codes
- PDF rental confirmations
- Refresh tokens
- Flyway database migrations
- Cloud image storage
- Multiple pickup locations
- Multiple languages
- Advanced analytics

## Author

**Hasti Torbatzadeh**

Software Technology Engineering student at VIA University College.

- GitHub: [Hasti818](https://github.com/Hasti818)

---

**Eleven22 Exotics — Born for performance.**

