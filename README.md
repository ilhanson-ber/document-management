# Document and Author Management REST API

## Overview

The objective of this project is to create a web application for managing documents and authors. Each document has a
title, body, authors, and references. Authors have a first name and a last name. The application is built using the
Spring Framework and provides a set of RESTful services to perform CRUD operations on both documents and authors. The
project uses PostgreSQL for data persistence and includes additional features like Flyway for database migrations,
Testcontainers for dockerized test, and
OpenAPI for API documentation.

The following sections will detail the project setup, technologies used, implementation details, testing strategies, and
potential future improvements.

## Table of Contents

- [Project Setup](#project-setup)
- [Implementation Details](#implementation-details)
- [Testing and Validation](#testing-and-validation)
- [Conclusion](#conclusion)
- [Future Improvements](#future-improvements)

## Project Setup

This project is implemented using Spring Boot (v3.3.1) with Java 22 and PostgreSQL as the primary database. The
application includes Flyway for database migrations and OpenAPI for generating API documentation.

### Building and Running the Application

To build and run the application, make sure Docker is running, and then simply run this command on the root of the
project:

```bash
mvn spring-boot:run
```

After that the API can be accessed using the base URL: http://localhost:8080/api/v1

Alternatively, you can first build the application with maven install and then run the resulting `.jar` file:

```bash
mvn clean install
java -jar target/document-management-0.0.1-SNAPSHOT.jar
```

## Implementation Details

The application provides a rich set of RESTful APIs to manage both authors and documents. It also incorporates
validation, error handling, and transaction management to ensure data consistency and robustness.

### Main Components

The application comprises three main services and two blocking queues:

- **Author Management**: CRUD operations for authors, including associating them with documents.
- **Document Management**: CRUD operations for documents, including managing references between documents and
  associating authors with documents.
- **Error Handling**: Custom exception handlers ensure that meaningful error messages and appropriate HTTP status codes
  are returned to clients.
- **Validation**: Input data is validated using annotations to ensure that data integrity is maintained.

### OpenAPI Documentation

The application integrates OpenAPI (Swagger) to provide interactive API documentation. Once the application is running,
you can access the documentation at:

[localhost:8080/swagger-ui/index.html]()

### Application Workflow

The main workflow of the application involves managing authors and documents. Each document can have multiple authors
and references to other documents, and each author can be associated with multiple documents. The application provides
endpoints to manage these relationships and ensures data consistency through proper validation and error handling.

### Key Endpoints

- Author Endpoints
    - GET /api/v1/authors: Retrieve all authors.
    - POST /api/v1/authors: Create a new author.
    - PUT /api/v1/authors/{id}: Update an existing author.
    - DELETE /api/v1/authors/{id}: Delete an author.

- Document Endpoints
    - GET /api/v1/documents: Retrieve all documents.
    - POST /api/v1/documents: Create a new document.
    - PUT /api/v1/documents/{id}: Update an existing document.
    - DELETE /api/v1/documents/{id}: Delete a document.

### Key Business Limitations

- To add a document to an author, the document must already exist in the system. Similarly, to add an author or a
  reference to a document, the respective author or referenced document must already be present. The application does
  not create non-existing resources within the main create or update requests.

- An author can currently exist without being attached to any document, and a document can exist without an associated
  author. The application does not automatically remove or label these unattached entities.

These limitations can be easily addressed within the current implementation once clarified with the business team.

## Testing and Validation

The project includes comprehensive unit and integration tests to ensure the correctness of its functionality. Unit tests
cover the service layer, helper utilities, and validation logic, while integration tests verify the correct operation of
the entire system using Testcontainers for the PostgreSQL database.

To run the unit and integration tests, make sure Docker is running, and then:

```bash
mvn test
```

## Future Improvements

While the core functionality is in place, there are a few areas where further enhancements can be made:

- Implement authentication and authorization to secure the API endpoints.
- Integrate message/event publishing and consuming tasks using RabbitMQ or Kafka for real-time event processing.
- Dockerization of the entire application to simplify deployment.

If you'd like to get to know me and discuss the project in detail, I would be happy to prepare the missing points in a
separate pull request to the original solution, so we can go over them during a meeting.

