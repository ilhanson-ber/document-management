# Document and Author Management REST API

## Overview

The objective of this project is to create a web application for managing documents and authors. Each document has a
title, body, authors, and references. Authors have a first name and a last name. The application is built using the
Spring Framework and provides a set of RESTful services to perform CRUD operations on both documents and authors. The
project uses PostgreSQL for data persistence and Kafka for event-based communication and includes additional features
like Spring Security JWT Authentication and Role Based Authorization, Flyway for database migrations,
Testcontainers for dockerized test, and OpenAPI for API documentation.

The following sections will detail the project setup, technologies used, implementation details, testing strategies, and
potential future improvements.

## Table of Contents

- [Project Setup](#project-setup)
- [Implementation Details](#implementation-details)
- [Testing and Validation](#testing-and-validation)
- [Future Improvements](#future-improvements)

## Project Setup

This project is implemented using Spring Boot (v3.3.1) with Java 22 and PostgreSQL as the primary database and Kafka as
the event broker. The
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
authentication and authorization, event-based communication, a permanent datastore, validation, error handling, and
transaction management to ensure data consistency and robustness.

### Main Components

The application consists of the following features:

- **Author Management**: CRUD operations for authors, including associating them with documents.
- **Document Management**: CRUD operations for documents, including managing references between documents and
  associating authors with documents.
- **Security**: JWT-based security with stateless tokens. Users are assigned roles (`READER`, `EDITOR`) to control
  access to different operations.
- **Event System**: Kafka event broker is used to communicate updates to entities and trigger follow-up actions based on
  these updates.
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
endpoints to manage these relationships and ensures data consistency through proper validation and error handling. All
endpoints are secured using token-based authentication. Users with the READER role are authorized to perform GET
requests, while users with the EDITOR role are required for POST, PUT, and DELETE operations.

### Key Endpoints

- Authentication Endpoints
    - POST /api/v1/login: Login and get a JWT Bearer token
    - POST /api/v1/signup: Create an account (and get a token)

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

### Key Business Requirement Assumptions

- To add a document to an author, the document must already exist in the system. Similarly, to add an author or a
  reference to a document, the respective author or referenced document must already be present. The application does
  not create non-existing resources within the main create or update requests.

- An author can currently exist without being attached to any document, and a document can exist without an associated
  author. The application does not automatically remove or label these unattached entities.

- Tokens are not persisted in the database. They are created and provided to the user and reevaluated every time a user
  sends it within a request. Once published, a token cannot be revoked for access until it expires or the related user
  account is deleted.

- The event publishing and consuming system works as follows:
    - When there is a successful update or delete to an entity, a message is sent to a related topic (e.g., `
      author_updated`, `document_deleted`).
    - The author update message contains the state of the author **at the time of the update**.
    - The author update consumer service removes the documents that were associated with the author at the time of the
      update, along with the author itself. If the author was associated with documents (e.g., doc1 and doc2) at the
      time of the update but later got associated with another document (doc3) before the event was consumed, the
      consumer will only remove doc1 and doc2, not the latest state. This behavior can be changed to reflect the current
      state by reading the latest data from the database.

These limitations can be easily addressed within the current implementation once clarified with the business team.

## Testing and Validation

The project includes comprehensive unit and integration tests to ensure the correctness of its functionality. Unit tests
cover the service layer, helper utilities, and validation logic, while integration tests verify the correct operation of
the entire system using Testcontainers for the PostgreSQL database and the Kafka event broker.

To run the unit and integration tests, make sure Docker is running, and then:

```bash
mvn test
```

If you would like to test the system manually, you can sign up for an account and create all the entities from scratch.
Alternatively, the database includes some initial data to make the system easier to test, such as user accounts,
authors, and documents (see Flyway migration
script [V2__initialise_date.sql](src/main/resources/db/migration/V2__initialise_data.sql)).

There are two users available for testing:

- **With a READER role**: Username: `samiral`, Password: `password`
- **With an EDITOR role**: Username: `kaib`, Password: `password`

Additionally, the database is pre-populated with 3 authors (already associated with documents) and 6 documents (some of
which are associated with other documents as references).

## Future Improvements

The application is not yet production-ready for deployment, as the focus has been primarily on development rather than
deployment. To make the application more suitable for production, secrets should ideally be moved to separate
environment files rather than being stored in configuration files. Additionally, the application should be containerized
to simplify the building and deployment processes.

If you'd like to get to know me and discuss the project in detail, I would be happy to meet you soon! Have fun checking
the code!
