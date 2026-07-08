# recruitment-management-system
Recruitment Management System REST API built with Spring Boot, Spring Security, JWT, JPA, MySQL and Docker.

## Authentication Flow

1. Candidate registers with email and password.
2. Password is encrypted using BCrypt before being stored.
3. User logs in with email and password.
4. System validates credentials and account status.
5. System returns JWT access token.
6. Client sends token in Authorization header:

```http
Authorization: Bearer <access_token>