# Contact Management System

Web app for managing personal contacts. Users can register, log in, and keep their own contact list with names, titles, emails, and phone numbers.

Backend is Spring Boot (Java 17). Frontend is React with Vite. Data is stored in PostgreSQL.

## What's in the repo

```
backend/     Spring Boot API
frontend/    React UI
```

Main features:

- Register with email or phone number
- Session-based login
- CRUD for contacts (paginated list, search by name)
- User profile and password change

## Prerequisites

- Java 17
- Maven (or use the wrapper in `backend/`)
- Node.js 18+ (for the frontend)
- PostgreSQL running locally

## Database setup

Create a database called `contact_management`:

```sql
CREATE DATABASE contact_management;
```

The app expects a `postgres` user. Password comes from an environment variable (see below). Hibernate creates/updates tables on startup (`ddl-auto=update`), so you don't need to run SQL scripts manually.

## Running locally

### Backend

From `backend/`:

**Windows**

```powershell
$env:DB_PASSWORD="your_postgres_password"
.\mvnw.cmd spring-boot:run
```

**macOS / Linux**

```bash
export DB_PASSWORD=your_postgres_password
./mvnw spring-boot:run
```

API runs on http://localhost:8080

### Frontend

From `frontend/`:

```bash
npm install
npm run dev
```

Copy `frontend/.env.example` to `frontend/.env.local` before starting the dev server. On Windows: `copy .env.example .env.local` from inside `frontend/`.

UI runs on http://localhost:5173

The frontend talks to the backend using `VITE_API_BASE_URL` (defaults to `http://localhost:8080`). Auth uses session cookies, so both need to be running for login/register to work.

## Tests

Backend unit tests:

```bash
cd backend
mvn test
```

With coverage (used by SonarCloud):

```bash
mvn clean verify
```

## SonarCloud

Pushes and PRs to `main` trigger a GitHub Actions workflow that runs tests and sends results to SonarCloud. The `SONAR_TOKEN` secret needs to be set in the repo settings for that to work.

## Git workflow

- `main` — working code that builds and runs
- `feature/<short-name>` — branch for new work (e.g. `feature/contact-search`)
- Open a PR into `main` when the feature is ready
- Merge after review (or self-review for solo work)

Keep commits small and named after what changed (`add contact delete modal`, `fix login redirect`, etc.).

## Notes

- Using PostgreSQL (confirmed OK with mentor).
- Search works on first and last name only.
