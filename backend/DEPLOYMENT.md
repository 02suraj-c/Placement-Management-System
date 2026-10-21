# Deployment & Production Operations Guide

This guide covers building, configuring, deploying, and troubleshooting the **Placement Management System (Backend)** with AI services and Question Bank.

---

## 1. Prerequisites

- **Java JDK**: 17+ (Eclipse Temurin recommended)
- **Maven**: 3.9+
- **Database**: MySQL 8.0+ or PostgreSQL 14+
- **AI API Key**: OpenAI (`sk-...`) or Google Gemini (`AIza...`)
- **Docker & Docker Compose** (Optional, for containerized deployments)

---

## 2. Environment Configuration

Copy `.env.example` to `.env` in the `backend/` directory:

```bash
cp .env.example .env
```

### Essential Configuration Variables

| Variable | Description | Example Value |
|---|---|---|
| `PORT` | HTTP server port (Default: `8080`, mapped `8080:8080`) | `8080` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `prod` |
| `DB_URL` | JDBC Connection String | `jdbc:mysql://localhost:3306/placement_db?useSSL=false&allowPublicKeyRetrieval=true` |
| `DB_USERNAME` | Database username (never hardcoded) | `root` |
| `DB_PASSWORD` | Database password (never hardcoded, in `.env`) | `your_secure_password` |
| `JWT_SECRET` | 256-bit secret key for HMAC-SHA256 | `32+ characters secret phrase` |
| `AI_PROVIDER` | AI service provider | `openai` or `gemini` |
| `AI_API_KEY` | Real provider API key | `sk-proj-...` |
| `AI_MODEL` | Provider model name | `gpt-4o-mini` or `gemini-1.5-flash` |
| `AI_BASE_URL` | Chat completions API URL | `https://api.openai.com/v1/chat/completions` |

---

## 3. Local Build & Run

### A. Run via Maven (Standalone)

```bash
# 1. Run automated test suite
mvn clean test

# 2. Package production JAR
mvn clean package -DskipTests

# 3. Launch application with environment variables
java -jar target/placement-management-1.0.0-SNAPSHOT.jar
```

### B. Run via Docker Compose (Multi-container)

```bash
# Launch MySQL 8.0 and Spring Boot Backend
docker compose up -d --build

# View real-time logs
docker compose logs -f backend

# Stop containers and preserve database volume
docker compose down
```

---

## 4. Deploying to Cloud Platforms

### A. Render (Recommended for quick deployment)

1. **Create Database**:
   - In Render Dashboard, click **New +** -> **PostgreSQL** or connect external MySQL (Aiven/PlanetScale).
   - Copy the internal/external connection URL.
2. **Create Web Service**:
   - Connect your GitHub repository.
   - **Root Directory**: `backend`
   - **Environment**: `Docker`
   - **Instance Type**: Free / Starter
3. **Environment Variables**:
   Add the following in Render **Environment** tab:
   - `DB_URL`: JDBC connection string
   - `DB_USERNAME`: Database user
   - `DB_PASSWORD`: Database password
   - `JWT_SECRET`: Random 256-bit string
   - `AI_API_KEY`: Your OpenAI/Gemini API key
   - `AI_PROVIDER`: `openai`
   - `AI_MODEL`: `gpt-4o-mini`
4. **Deploy**:
   - Click **Manual Deploy** -> **Deploy latest commit**.

### B. AWS (Elastic Beanstalk / EC2)

1. **Build JAR**:
   ```bash
   mvn clean package -DskipTests
   ```
2. **Elastic Beanstalk**:
   - Platform: `Java 17 (Corretto)`
   - Upload `target/placement-management-1.0.0-SNAPSHOT.jar`.
   - Under **Configuration -> Software**, configure environment properties (`DB_URL`, `AI_API_KEY`, `JWT_SECRET`).
   - Add AWS RDS MySQL instance under **Configuration -> Database**.

---

## 5. Health Checks & Monitoring

The service exposes Spring Boot Actuator endpoints for liveness and readiness monitoring:

- **Health Check**: `GET http://localhost:8080/actuator/health`
  ```json
  {
    "status": "UP"
  }
  ```
- **Service Info**: `GET http://localhost:8080/actuator/info`

---

## 6. Troubleshooting Common Issues

### 1. `CommunicationsException: Communications link failure`
- **Cause**: MySQL service is not running or not reachable on `localhost:3306`.
- **Solution**:
  - Verify database status: `mysql -u root -p -e "SHOW DATABASES;"`
  - Ensure `createDatabaseIfNotExist=true` is present in the JDBC connection string.

### 2. `AiServiceException [CLIENT_ERROR]: AI provider returned HTTP 401`
- **Cause**: Invalid or expired `AI_API_KEY`.
- **Solution**:
  - Verify that the key starts with `sk-` (OpenAI) or `AIza` (Gemini).
  - Verify your API account has active quota/credits.

### 3. `AiServiceException [TIMEOUT]: AI provider request timed out`
- **Cause**: Provider latency exceeded configured `ai.timeout-seconds` (default 30s).
- **Solution**:
  - Increase timeout in `.env`: `AI_TIMEOUT_SECONDS=60`.

### 4. `403 Forbidden` on `/api/questions` or `/api/ai/interview/generate-and-save`
- **Cause**: User role does not have write permissions.
- **Solution**:
  - Ensure JWT token has role `TPO`, `DEPUTY_TPO`, `COORDINATOR`, or `FACULTY`. `STUDENT` users are restricted to read-only question bank access and non-persisting interview generation.

### 5. `400 Bad Request: Invalid file type` during Resume Upload
- **Cause**: Uploaded file is not PDF or DOCX, or file size exceeds 5MB limit.
- **Solution**:
  - Ensure `Content-Type` header is `multipart/form-data`.
  - Ensure files uploaded have extension `.pdf` or `.docx` and size `< 5MB`.
