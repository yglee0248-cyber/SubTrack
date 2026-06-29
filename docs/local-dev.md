# Local Development Guide

This guide explains how to run the SubTrack backend with a local Docker MySQL database.

## 1. Environment Variables

Copy `.env.example` if you want a local `.env` file for Docker Compose or personal notes.

```powershell
Copy-Item .env.example .env
```

Default local values:

```env
DB_URL=jdbc:mysql://localhost:3307/subtrack?serverTimezone=Asia/Seoul&useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci&characterSetResults=utf8mb4
DB_USERNAME=root
DB_PASSWORD=password
JWT_SECRET=local-development-secret-key-change-later
JWT_ACCESS_TOKEN_EXPIRATION_MS=3600000
CORS_ALLOWED_ORIGINS=http://localhost:5173
EXCHANGE_RATE_BASE_URL=https://api.frankfurter.dev
```

Spring Boot also has the same local defaults in `backend/src/main/resources/application.yml`, so the backend can run locally without creating a real `.env` file.

If you already created `.env` before the UTF-8 JDBC URL change, update its `DB_URL` to match the value above. Environment variables override the default in `application.yml`.

## 2. Start Docker MySQL

Run MySQL from the project root.

```powershell
docker compose up -d mysql
```

Local port rule:

```txt
localhost:3307 -> mysql container:3306
```

Check the container.

```powershell
docker compose ps
```

## 3. Apply Schema

Run the schema SQL manually from the project root.

```powershell
Get-Content .\database\01_schema.sql | docker exec -i subtrack-mysql mysql -uroot -ppassword subtrack
```

## 4. Apply Seed Data

Run the category seed SQL manually from the project root.

```powershell
Get-Content .\database\02_seed_category.sql | docker exec -i subtrack-mysql mysql -uroot -ppassword subtrack
```

For a fresh local database, `01_schema.sql` already contains the current schema. The numbered SQL files from `03_*` onward are manual migrations for an older local database that you want to keep.

## 5. Build Backend

Use the Maven Wrapper. Do not assume a global `mvn` command exists.

```powershell
cd backend
.\mvnw.cmd clean package
```

## 6. Run Backend

Run Spring Boot with the Maven Wrapper.

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

The backend runs on:

```txt
http://localhost:8080
```

Swagger UI runs on:

```txt
http://localhost:8080/swagger-ui.html
```

## 7. Health Check

Check that the backend is running.

```powershell
curl http://localhost:8080/api/health
```

Expected response:

```json
{
  "success": true,
  "message": "요청이 성공했습니다.",
  "data": "SubTrack backend is running"
}
```

## 8. Run Frontend

Create the frontend env file if needed.

```powershell
Copy-Item .\frontend\.env.example .\frontend\.env
```

Run the Vite app.

```powershell
cd frontend
npm install
npm run dev
```

The frontend runs on:

```txt
http://localhost:5173
```

Build check:

```powershell
cmd /c npm run build
```

## 9. Stop MySQL

Stop the container.

```powershell
docker compose down
```

To remove the named volume and delete local database data:

```powershell
docker compose down -v
```
