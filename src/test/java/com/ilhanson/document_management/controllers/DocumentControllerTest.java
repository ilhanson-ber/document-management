package com.ilhanson.document_management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilhanson.document_management.dtos.*;
import com.ilhanson.document_management.services.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @MockBean
    private DocumentService documentService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGetAllDocuments() throws Exception {
        // Arrange
        DocumentDTO documentDTO = new DocumentDTO(1L, "Doc 1", "Body 1");
        when(documentService.getAllDocuments()).thenReturn(List.of(documentDTO));

        // Act & Assert
        mockMvc.perform(get("/api/v1/documents"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Doc 1"))
                .andExpect(jsonPath("$[0].body").value("Body 1"));
    }

    @Test
    void shouldCreateDocument() throws Exception {
        // Arrange
        DocumentCreateDTO documentCreateDTO = new DocumentCreateDTO(null, "Doc 1", "Body 1", null, null);
        DocumentDetailsDTO documentDetailsDTO = new DocumentDetailsDTO(1L, "Doc 1", "Body 1", null, null);

        when(documentService.createDocument(any(DocumentCreateDTO.class))).thenReturn(documentDetailsDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(documentCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Doc 1"))
                .andExpect(jsonPath("$.body").value("Body 1"));
    }

    @Test
    void shouldReturnBadRequestForInvalidDocumentCreateDTO() throws Exception {
        // Arrange
        DocumentCreateDTO invalidDocumentCreateDTO = new DocumentCreateDTO(null, "", "", null, null); // Invalid data

        // Act & Assert
        mockMvc.perform(post("/api/v1/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDocumentCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Validation failed for the request body. See errorDetails for more information."))
                .andExpect(jsonPath("$.status").value("Bad Request"))
                .andExpect(jsonPath("$.errorDetails.title").value("Title can not be empty"))
                .andExpect(jsonPath("$.errorDetails.body").value("Body can not be empty"));
    }

    @Test
    void shouldCreateDocumentWithAuthorsAndReferences() throws Exception {
        // Arrange
        IdInputDTO author1 = new IdInputDTO(1L);
        IdInputDTO author2 = new IdInputDTO(2L);
        IdInputDTO reference1 = new IdInputDTO(3L);
        IdInputDTO reference2 = new IdInputDTO(4L);

        DocumentCreateDTO documentCreateDTO = new DocumentCreateDTO(null, "Doc 1", "Body 1", List.of(reference1, reference2), List.of(author1, author2));
        
        AuthorDTO authorDTO1 = new AuthorDTO(1L, "John", "Doe");
        AuthorDTO authorDTO2 = new AuthorDTO(2L, "Jane", "Doe");

        DocumentDTO referenceDTO1 = new DocumentDTO(3L, "Ref Doc 1", "Ref Body 1");
        DocumentDTO referenceDTO2 = new DocumentDTO(4L, "Ref Doc 2", "Ref Body 2");

        DocumentDetailsDTO documentDetailsDTO = new DocumentDetailsDTO(1L, "Doc 1", "Body 1", List.of(authorDTO1, authorDTO2), List.of(referenceDTO1, referenceDTO2));

        when(documentService.createDocument(any(DocumentCreateDTO.class))).thenReturn(documentDetailsDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(documentCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Doc 1"))
                .andExpect(jsonPath("$.body").value("Body 1"))

                // Check the associated authors
                .andExpect(jsonPath("$.authors[0].id").value(1L))
                .andExpect(jsonPath("$.authors[0].firstName").value("John"))
                .andExpect(jsonPath("$.authors[0].lastName").value("Doe"))
                .andExpect(jsonPath("$.authors[1].id").value(2L))
                .andExpect(jsonPath("$.authors[1].firstName").value("Jane"))
                .andExpect(jsonPath("$.authors[1].lastName").value("Doe"))

                // Check the associated references
                .andExpect(jsonPath("$.references[0].id").value(3L))
                .andExpect(jsonPath("$.references[0].title").value("Ref Doc 1"))
                .andExpect(jsonPath("$.references[0].body").value("Ref Body 1"))
                .andExpect(jsonPath("$.references[1].id").value(4L))
                .andExpect(jsonPath("$.references[1].title").value("Ref Doc 2"))
                .andExpect(jsonPath("$.references[1].body").value("Ref Body 2"));
    }
}