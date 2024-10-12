-- Insert users
INSERT INTO user_account (first_name, last_name, username, password, role) VALUES
('Samira', 'Lang', 'samiral', '$2a$10$cMzAnTS4kSfnuK6GL1Pshu20/DtyTsMqLmDeQNO273lAgCKJw/3N2', 'READER'),
('Kai', 'Bennett', 'kaib', '$2a$10$.dr1FlgCfdtsCn1YGaLDxutiIEw/d4EGUSSsSRHFNOL9.iFS7wC.K', 'EDITOR');
-- Insert authors
INSERT INTO author (first_name, last_name) VALUES
('John', 'Doe'),
('Jane', 'Smith'),
('Emily', 'Johnson');

-- Insert documents
INSERT INTO document (title, body) VALUES
('Document 1', 'Content of Document 1'),
('Document 2', 'Content of Document 2'),
('Document 3', 'Content of Document 3'),
('Document 4', 'Content of Document 4'),
('Document 5', 'Content of Document 5'),
('Document 6', 'Content of Document 6');

-- Associate authors with documents in the author_document table
INSERT INTO author_document (author_id, document_id) VALUES
(1, 1), (1, 2), (1, 3),
(2, 1), (2, 5),
(3, 4), (3, 5);

-- Set up document references in the document_reference table (document referencing other documents)
INSERT INTO document_reference (document_id, reference_id) VALUES
(1, 2), (1, 3), (1, 4),
(3, 4),
(4, 1), (4, 2),
(5, 1);
