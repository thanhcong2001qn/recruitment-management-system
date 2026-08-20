# Recruitment Management System

A backend system for managing recruitment activities such as users, companies, jobs, candidates, applications, and interviews.

The project is built with **Java 21 and Spring Boot** and is designed with production-oriented practices such as JWT authentication, validation, pagination, dynamic search, soft delete, database migrations, automated testing, Docker, and CI.

---

## 1. Project Overview

Recruitment Management System is a RESTful backend application designed to support the recruitment process.

The system is designed around the following main areas:

* Authentication and authorization
* User management
* Company management
* Job management
* Candidate management
* Application management
* Interview management

The current implementation focuses on the **Authentication** and **Company** modules. The remaining business modules will be implemented incrementally.

---

## 2. Current Features

### Authentication

* User registration
* User login
* JWT authentication
* Refresh token
* Password hashing
* Role-based authorization
* Request validation
* Global exception handling

### Company

* Create company
* Get company by ID
* Update company
* Soft delete company
* Restore company
* Pagination
* Sorting
* Dynamic filtering
* Search by keyword
* Search by city
* Search by status
* Search by company size
* JPA Specification
* Slug generation
* Duplicate validation
* Database unique constraints

### API & Documentation

* Standard API response
* Paged response
* Global exception handling
* Swagger / OpenAPI documentation

### Testing

* JUnit 5
* Mockito
* AssertJ
* MockMvc
* Testcontainers
* MySQL integration testing

### Database

* MySQL
* Flyway database migrations
* Database indexes for company search
* Database constraints

### DevOps

* Docker
* Docker Compose
* GitHub Actions
* GitHub Container Registry
* Spring Boot Actuator health checks

---

## 3. Tech Stack

| Technology                | Purpose                        |
| ------------------------- | ------------------------------ |
| Java 21                   | Backend language               |
| Spring Boot               | Application framework          |
| Spring Security           | Authentication & authorization |
| JWT                       | Stateless authentication       |
| Spring Data JPA           | Data access                    |
| Hibernate                 | ORM                            |
| MySQL 8                   | Relational database            |
| Flyway                    | Database migration             |
| Jakarta Validation        | Request validation             |
| JUnit 5                   | Unit testing                   |
| Mockito                   | Mocking                        |
| AssertJ                   | Assertions                     |
| MockMvc                   | API testing                    |
| Testcontainers            | Integration testing            |
| Swagger / OpenAPI         | API documentation              |
| Docker                    | Containerization               |
| Docker Compose            | Local environment              |
| GitHub Actions            | CI                             |
| GitHub Container Registry | Docker image registry          |
| Spring Boot Actuator      | Health monitoring              |
| Maven                     | Build & dependency management  |

---

## 4. Architecture

The project uses a **feature-based architecture**.

```text
com.example.qltd
│
├── auth
│   ├── controller
│   ├── service
│   ├── repository
│   ├── dto
│   └── ...
│
├── company
│   ├── controller
│   ├── service
│   ├── repository
│   ├── entity
│   ├── dto
│   │   ├── request
│   │   └── response
│   ├── mapper
│   ├── validator
│   ├── specification
│   └── ...
│
├── user
│   ├── entity
│   ├── repository
│   └── ...
│
└── common
    ├── base
    ├── config
    ├── dto
    ├── exception
    ├── mapper
    ├── security
    └── util
```

### Layer responsibility

```text
Controller
    ↓
Service
    ↓
Validator / Business Rules
    ↓
Repository
    ↓
Database
```

Supporting components:

```text
DTO
 ↓
Mapper
 ↓
Entity

Specification
 ↓
Dynamic Query

GlobalExceptionHandler
 ↓
Standardized API Error

Security Filter
 ↓
JWT Authentication
```

---

## 5. Request Flow

### Authenticated API

```text
Client
  │
  ▼
HTTP Request
  │
  ▼
JWT Authentication Filter
  │
  ▼
Spring Security
  │
  ▼
@PreAuthorize
  │
  ▼
Controller
  │
  ▼
Service
  │
  ├── Validator
  ├── Business Rules
  └── Mapper
  │
  ▼
Repository
  │
  ▼
MySQL
```

### Search Flow

```text
GET /api/companies
        │
        ▼
CompanySearchRequest
        │
        ▼
CompanySpecification
        │
        ▼
JpaSpecificationExecutor
        │
        ▼
MySQL
        │
        ▼
Page<Company>
        │
        ▼
PageMapper
        │
        ▼
PagedResponse<CompanyResponse>
```

---

## 6. Authentication

The application uses JWT-based stateless authentication.

Typical authentication flow:

```text
POST /api/auth/login
        │
        ▼
Validate credentials
        │
        ▼
Generate Access Token
        │
        ▼
Generate Refresh Token
        │
        ▼
Client
```

Authenticated requests use:

```http
Authorization: Bearer <access-token>
```

Role-based access control is implemented with Spring Security method security.

Example:

```java
@PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
```

and:

```java
@PreAuthorize("hasRole('ADMIN')")
```

---

## 7. Standard API Response

Successful API responses use:

```json
{
  "success": true,
  "message": "Company retrieved successfully",
  "data": {}
}
```

Paginated responses use:

```json
{
  "success": true,
  "message": "Company list retrieved successfully",
  "data": {
    "items": [],
    "page": 0,
    "size": 10,
    "totalElements": 0,
    "totalPages": 0,
    "first": true,
    "last": true,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

---

## 8. Exception Handling

The application uses a centralized exception handler.

Common HTTP responses:

| Status | Meaning                            |
| ------ | ---------------------------------- |
| 400    | Validation / bad request           |
| 401    | Authentication required            |
| 403    | Access denied                      |
| 404    | Resource not found                 |
| 409    | Duplicate resource / data conflict |

Example validation response:

```json
{
  "timestamp": "2026-01-01T10:00:00",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Validation failed",
  "errors": {
    "email": "Invalid email format"
  }
}
```

---

## 9. Company Search

The company search API supports:

### Pagination

```http
GET /api/companies?page=0&size=10
```

### Sorting

```http
GET /api/companies?sort=name,asc
```

### Keyword

```http
GET /api/companies?keyword=software
```

### City

```http
GET /api/companies?city=Ho%20Chi%20Minh
```

### Status

```http
GET /api/companies?status=ACTIVE
```

### Company size

```http
GET /api/companies?companySize=LARGE
```

### Combined filters

```http
GET /api/companies?keyword=software&city=Ho%20Chi%20Minh&status=ACTIVE&page=0&size=10
```

Dynamic filtering is implemented using Spring Data JPA `Specification`.

---

## 10. Soft Delete

Company deletion uses soft delete instead of physical deletion.

Delete operation:

```text
deleted = true
status  = INACTIVE
```

Restore operation:

```text
deleted = false
status  = ACTIVE
```

This preserves historical data and avoids physically removing records that may later be referenced by other recruitment modules.

---

## 11. Database & Flyway

Database schema changes are managed through Flyway migrations.

```text
src/main/resources/db/migration/

V1__create_users_table.sql
V2__create_companies_table.sql
V3__add_company_search_indexes.sql
```

Hibernate is configured to validate the schema:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

The expected flow is:

```text
Flyway
   ↓
Create / update schema
   ↓
Hibernate
   ↓
Validate Entity ↔ Database
```

This prevents Hibernate from silently changing the production database schema.

---

## 12. Database Indexes

The Company search module uses database indexes for frequently used filtering and sorting patterns.

Current indexes include:

```text
(deleted, city)

(deleted, status, company_size)

(deleted, created_at)
```

Keyword search uses:

```text
LIKE '%keyword%'
```

so normal B-Tree indexes on `name` and `description` are not treated as the primary optimization strategy for that query.

---

## 13. Testing Strategy

The project uses two main testing levels.

### Unit Test

Business logic is tested in isolation with:

* JUnit 5
* Mockito
* AssertJ

Example:

```text
CompanyServiceImplTest
```

### Integration Test

The API is tested through:

```text
MockMvc
    ↓
Controller
    ↓
Service
    ↓
Repository
    ↓
MySQL
```

Integration tests use:

```text
Testcontainers
      ↓
MySQL container
```

This avoids relying on an in-memory H2 database for integration testing.

---

## 14. Running Tests

Run all tests:

```bash
mvn clean verify
```

Using Maven Wrapper:

```bash
./mvnw clean verify
```

Windows:

```bash
mvnw.cmd clean verify
```

Integration tests require Docker because Testcontainers starts a MySQL container.

---

## 15. Docker

The project provides a Docker Compose environment containing:

```text
Spring Boot
     │
     ▼
MySQL
```

MySQL is exposed for local development, while Spring Boot connects to the MySQL service through the Docker network.

Inside Docker, the datasource uses:

```text
mysql
```

as the database hostname instead of:

```text
localhost
```

### Start the environment

```bash
docker compose up --build
```

### Stop

```bash
docker compose down
```

### Stop and remove database volume

```bash
docker compose down -v
```

---

## 16. Environment Variables

Sensitive values should not be committed to Git.

Example `.env`:

```env
DB_PORT=3306
DB_NAME=recruitment_db
MYSQL_ROOT_PASSWORD=change-me
APP_PORT=8080
```

A template is provided as:

```text
.env.example
```

The real `.env` file should remain ignored by Git.

---

## 17. Swagger / OpenAPI

API documentation is available through Swagger UI.

After starting the application:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

Authenticated APIs require a JWT token.

Use the Swagger:

```text
Authorize
```

button and enter:

```text
Bearer <JWT>
```

---

## 18. Health Check

Spring Boot Actuator provides application health information.

```text
http://localhost:8080/actuator/health
```

Liveness:

```text
http://localhost:8080/actuator/health/liveness
```

Readiness:

```text
http://localhost:8080/actuator/health/readiness
```

Docker uses the health endpoint to determine application health.

---

## 19. CI

GitHub Actions runs the Maven verification pipeline.

Current CI flow:

```text
Push / Pull Request
        ↓
Checkout
        ↓
Set up Java 21
        ↓
mvn clean verify
        ↓
Unit Tests
        ↓
Integration Tests
        ↓
Testcontainers + MySQL
        ↓
PASS / FAIL
```

The project also contains a Docker workflow that builds and publishes the application image to GitHub Container Registry.

---

## 20. GitHub Container Registry

The Docker image is published to GHCR.

Example:

```bash
docker pull ghcr.io/<github-username>/<repository>:latest
```

Images are also tagged by Git commit SHA for traceability.

Example:

```text
ghcr.io/<github-username>/<repository>:<commit-sha>
```

---

## 21. Local Development

### Prerequisites

Install:

* Java 21
* Maven
* Docker
* Git

### Clone

```bash
git clone <repository-url>
cd <repository-name>
```

### Configure environment

Create `.env` from `.env.example`.

### Start MySQL and application

```bash
docker compose up --build
```

### Or run Spring Boot locally

Keep MySQL running through Docker:

```bash
docker compose up -d mysql
```

Then run:

```bash
mvn spring-boot:run
```

---

## 22. API Examples

### Register

```http
POST /api/auth/register
Content-Type: application/json
```

### Login

```http
POST /api/auth/login
Content-Type: application/json
```

### Create Company

```http
POST /api/companies
Authorization: Bearer <JWT>
Content-Type: application/json
```

Example request:

```json
{
  "name": "OpenAI Vietnam",
  "description": "AI technology company",
  "website": "https://example.com",
  "email": "contact@example.com",
  "phone": "0912345678",
  "city": "Ho Chi Minh",
  "country": "Vietnam",
  "foundedYear": 2020,
  "employeeCount": 500,
  "companySize": "LARGE"
}
```

### Search Companies

```http
GET /api/companies?page=0&size=10&keyword=software
Authorization: Bearer <JWT>
```

---

## 23. Development Principles

The project follows several development principles:

### Separation of responsibilities

Controllers handle HTTP concerns.

Services handle business logic.

Repositories handle persistence.

Mappers handle DTO ↔ Entity conversion.

Validators handle validation and business rules.

Specifications handle dynamic search conditions.

### Database integrity

Application-level validation is combined with database constraints.

For example:

```text
Application validation
        +
Database unique constraint
```

This helps protect the system from race conditions and invalid data.

### Reusable infrastructure

Common infrastructure such as:

* Security
* Exception handling
* API responses
* Pagination
* Auditing
* Database migrations
* Docker
* Testing infrastructure

is designed to be reused by future modules.

---

## 24. Planned Modules

Current progress:

```text
Authentication      ✅
Company             ✅
Job                 🚧
Candidate           📋
Application         📋
Interview           📋
Dashboard           📋
Notification        📋
```

Planned recruitment workflow:

```text
Job
  ↓
Candidate
  ↓
Application
  ↓
Screening
  ↓
Interview
  ↓
Offer
  ↓
Hired / Rejected
```

---

## 25. Roadmap

### Phase 1 — Foundation

* [x] Project setup
* [x] GitHub repository
* [x] Docker MySQL
* [x] Authentication
* [x] JWT
* [x] Global exception handling

### Phase 2 — Company

* [x] Company CRUD
* [x] Validation
* [x] Dynamic search
* [x] Pagination
* [x] Specification
* [x] Soft delete
* [x] Restore
* [x] Unit tests
* [x] Integration tests
* [x] Swagger
* [x] Flyway
* [x] Database indexes

### Phase 3 — Infrastructure

* [x] Docker Compose
* [x] Testcontainers
* [x] GitHub Actions
* [x] GHCR
* [x] Actuator
* [ ] Production deployment

### Phase 4 — Recruitment Modules

* [ ] Job
* [ ] Candidate
* [ ] Application
* [ ] Interview
* [ ] Recruitment workflow
* [ ] Dashboard

---

## 26. Project Status

The project currently provides a production-oriented foundation for a recruitment management backend.

The next major development phase is the **Job module**, which will reuse the established architecture and infrastructure from the Company module.
