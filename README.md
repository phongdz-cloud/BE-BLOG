# Blog Backend API

This is a Spring Boot backend application for a blog system with JWT authentication.

## Technologies Used

- Java 17
- Spring Boot 3.2.3
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT
- Lombok
- Maven

## Prerequisites

- Java 17 or higher
- Maven
- PostgreSQL

## Getting Started

1. Clone the repository
2. Create a PostgreSQL database named `blog_db`
3. Update the database configuration in `src/main/resources/application.yml` if needed
4. Run the application using Maven:
   ```bash
   mvn spring-boot:run
   ```

## API Endpoints

### Authentication

- POST `/api/auth/signup` - Register a new user
- POST `/api/auth/signin` - Login and get JWT token

### Request Examples

#### Sign Up

```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123",
  "roles": ["user"]
}
```

#### Sign In

```json
{
  "username": "testuser",
  "password": "password123"
}
```

## Security

The application uses JWT (JSON Web Token) for authentication. To access protected endpoints, include the JWT token in the Authorization header:

```
Authorization: Bearer <your_jwt_token>
```

## Database

The application uses PostgreSQL as the database. Make sure to create a database named `blog_db` before running the application.

Default database configuration:

- URL: jdbc:postgresql://localhost:5432/blog_db
- Username: postgres
- Password: postgres

You can modify these settings in `src/main/resources/application.yml`.
