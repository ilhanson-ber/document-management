DROP TABLE IF EXISTS author_document;
DROP TABLE IF EXISTS document_reference;
DROP TABLE IF EXISTS author;
DROP TABLE IF EXISTS document;

CREATE TABLE author (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL
);

CREATE TABLE document (
    id SERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    body TEXT NOT NULL
);

CREATE TABLE author_document (
    author_id INT REFERENCES author(id),
    document_id INT REFERENCES document(id),
    PRIMARY KEY (author_id, document_id)
);

CREATE TABLE document_reference (
    document_id INT REFERENCES document(id),
    reference_id INT REFERENCES document(id),
    PRIMARY KEY (document_id, reference_id)
);