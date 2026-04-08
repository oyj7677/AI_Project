# Rhythm Habit Studio

Rhythm Habit Studio is now a two-tier habit tracker made of:

- a React + Vite frontend in the project root
- a Kotlin + Spring Boot backend in `backend/`

The frontend keeps the existing dashboard experience, while the backend now owns habit persistence through a REST API backed by H2.

## What Changed

- Habits are loaded from `Spring Boot` instead of browser-only `localStorage`
- Existing browser data is migrated once into the backend when the server is empty
- Habit create / toggle / delete actions now call `/api/habits`
- Vite proxies `/api` requests to `http://localhost:8080` in development
- Theme preference still stays in `localStorage`

## Tech Stack

### Frontend

- React 19
- TypeScript
- Vite
- Plain CSS
- Vitest

### Backend

- Kotlin
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- H2 file database
- Bean Validation

## Project Structure

```text
habit/
├─ backend/
│  ├─ src/main/kotlin/com/oyj/habit/backend/
│  ├─ src/main/resources/
│  └─ build.gradle.kts
├─ public/
├─ src/
│  ├─ components/
│  ├─ hooks/
│  ├─ lib/
│  └─ types/
├─ package.json
└─ vite.config.ts
```

## Run The Project

### 1. Start the backend

Windows:

```powershell
cd C:\Users\mediazen\Desktop\oyjProject\AI_Project\habit\backend
.\gradlew.bat bootRun
```

macOS / Linux:

```bash
cd /path/to/habit/backend
./gradlew bootRun
```

The backend starts on `http://localhost:8080`.

Useful endpoints:

- `GET /api/health`
- `GET /api/habits`
- `POST /api/habits`
- `POST /api/habits/import`
- `PUT /api/habits/{id}/completion`
- `DELETE /api/habits/{id}`
- `GET /h2-console`

### 2. Start the frontend

```bash
cd /path/to/habit
npm install
npm run dev
```

The frontend starts on `http://127.0.0.1:5173` and proxies API calls to the Spring server.

## Backend Notes

- Habit data is stored in a local H2 file database under `backend/data/`
- `weekly` habits allow one completion per week
- `weekdays` habits reject weekend check-ins on the server
- Local browser habits are imported automatically on the first successful sync if the backend has no data

## Testing

Frontend:

```bash
npm run test
```

Backend:

Windows:

```powershell
cd C:\Users\mediazen\Desktop\oyjProject\AI_Project\habit\backend
.\gradlew.bat test
```

macOS / Linux:

```bash
cd /path/to/habit/backend
./gradlew test
```
