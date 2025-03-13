# Library Management System

A Spring Boot application providing a RESTful API for librarians to manage books in a library.

## Features

- Manage books, authors and countries
- Add new books for rental
- Delete books that are no longer in good condition
- Edit book records
- Mark books as borrowed
- API documentation with Swagger UI

## Technology Stack

- Java 17
- Spring Boot
- Spring Data JPA
- PostgreSQL Database
- Docker and Docker Compose

## Prerequisites

- Java 17 or higher
- Maven
- Docker and Docker Compose

## Getting Started

Follow these steps to set up and run the application:

1. **Clone the repository**
```bash
git clone https://github.com/deajadrovska/Library.git
cd Library
```

2. Start PostgreSQL database using Docker
```bash
docker-compose up -d
```

3. Run the Spring Boot application
```bash
./mvnw spring-boot:run
```
Or with Maven installed:
```bash
mvn spring-boot:run
```

4. Access Swagger UI

Open your browser and navigate to:
http://localhost:8080/swagger-ui.html

This interface allows you to test all API endpoints.
