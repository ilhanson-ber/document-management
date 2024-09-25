package com.ilhanson.document_management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilhanson.document_management.dtos.*;
import com.ilhanson.document_management.exceptions.ResourceNotFoundException;
import com.ilhanson.document_management.services.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentDetailsController.class)
class DocumentDetailsControllerTest {

    @MockBean
    private DocumentService documentService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGetDocumentDetailsWithAuthorsAndReferences() throws Exception {
        // Arrange
        AuthorDTO author1 = new AuthorDTO(3L, "John", "Doe");
        AuthorDTO author2 = new AuthorDTO(4L, "Jane", "Doe");
        DocumentDTO reference1 = new DocumentDTO(3L, "Ref Doc 1", "Reference Body 1");
        DocumentDTO reference2 = new DocumentDTO(4L, "Ref Doc 2", "Reference Body 2");

        DocumentDetailsDTO documentDetailsDTO = new DocumentDetailsDTO(1L, "Doc 1", "Body 1",
                List.of(author1, author2), List.of(reference1, reference2));

        when(documentService.getDocumentDetails(1L)).thenReturn(documentDetailsDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/documents/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                // Check document details
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Doc 1"))
                .andExpect(jsonPath("$.body").value("Body 1"))

                // Check associated authors
                .andExpect(jsonPath("$.authors[0].id").value(3L))
                .andExpect(jsonPath("$.authors[0].firstName").value("John"))
                .andExpect(jsonPath("$.authors[0].lastName").value("Doe"))
                .andExpect(jsonPath("$.authors[1].id").value(4L))
                .andExpect(jsonPath("$.authors[1].firstName").value("Jane"))
                .andExpect(jsonPath("$.authors[1].lastName").value("Doe"))

                // Check associated references
                .andExpect(jsonPath("$.references[0].id").value(3L))
                .andExpect(jsonPath("$.references[0].title").value("Ref Doc 1"))
                .andExpect(jsonPath("$.references[0].body").value("Reference Body 1"))
                .andExpect(jsonPath("$.references[1].id").value(4L))
                .andExpect(jsonPath("$.references[1].title").value("Ref Doc 2"))
                .andExpect(jsonPath("$.references[1].body").value("Reference Body 2"));
    }

    @Test
    void shouldUpdateDocument() throws Exception {
        // Arrange
        DocumentUpdateDTO documentUpdateDTO = new DocumentUpdateDTO(1L, "Updated Doc", "Updated Body", new ArrayList<>(), new ArrayList<>());
        DocumentDetailsDTO documentDetailsDTO = new DocumentDetailsDTO(1L, "Updated Doc", "Updated Body", null, null);

        when(documentService.updateDocument(any(DocumentUpdateDTO.class))).thenReturn(documentDetailsDTO);

        // Act & Assert
        mockMvc.perform(put("/api/v1/documents/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(documentUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Updated Doc"))
                .andExpect(jsonPath("$.body").value("Updated Body"));
    }

    @Test
    void shouldReturnUnprocessableEntityWhenPathAndBodyIdMismatch() throws Exception {
        // Arrange
        DocumentUpdateDTO documentUpdateDTO = new DocumentUpdateDTO(2L, "Updated Doc", "Updated Body", new ArrayList<>(), new ArrayList<>()); // ID mismatch

        // Act & Assert
        mockMvc.perform(put("/api/v1/documents/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(documentUpdateDTO)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("ID in request path and ID in request body should match"))
                .andExpect(jsonPath("$.status").value("Unprocessable Entity"))
                .andExpect(jsonPath("$.statusCode").value(422));
    }

    @Test
    void shouldDeleteDocument() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/documents/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldHandleResourceNotFound() throws Exception {
        // Arrange
        when(documentService.getDocumentDetails(1L)).thenThrow(new ResourceNotFoundException("Document", 1L));

        // Act & Assert
        mockMvc.perform(get("/api/v1/documents/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Document with ID 1 does not exist"))
                .andExpect(jsonPath("$.status").value("Not Found"))
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    void shouldUpdateDocumentWithAuthorsAndReferences() throws Exception {
        // Arrange
        DocumentUpdateDTO documentUpdateDTO = new DocumentUpdateDTO(1L, "Updated Doc", "Updated Body",
                List.of(new IdInputDTO(1L), new IdInputDTO(2L)), // References
                List.of(new IdInputDTO(3L), new IdInputDTO(4L)) // Authors
        );

        AuthorDTO author1 = new AuthorDTO(3L, "John", "Doe");
        AuthorDTO author2 = new AuthorDTO(4L, "Jane", "Doe");
        DocumentDTO reference1 = new DocumentDTO(1L, "Ref Doc 1", "Reference Body 1");
        DocumentDTO reference2 = new DocumentDTO(2L, "Ref Doc 2", "Reference Body 2");

        DocumentDetailsDTO documentDetailsDTO = new DocumentDetailsDTO(1L, "Updated Doc", "Updated Body",
                List.of(author1, author2), List.of(reference1, reference2));

        when(documentService.updateDocument(any(DocumentUpdateDTO.class))).thenReturn(documentDetailsDTO);

        // Act & Assert
        mockMvc.perform(put("/api/v1/documents/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(documentUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                // Check document details
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Updated Doc"))
                .andExpect(jsonPath("$.body").value("Updated Body"))

                // Check associated authors
                .andExpect(jsonPath("$.authors[0].id").value(3L))
                .andExpect(jsonPath("$.authors[0].firstName").value("John"))
                .andExpect(jsonPath("$.authors[0].lastName").value("Doe"))
                .andExpect(jsonPath("$.authors[1].id").value(4L))
                .andExpect(jsonPath("$.authors[1].firstName").value("Jane"))
                .andExpect(jsonPath("$.authors[1].lastName").value("Doe"))

                // Check associated references
                .andExpect(jsonPath("$.references[0].id").value(1L))
                .andExpect(jsonPath("$.references[0].title").value("Ref Doc 1"))
                .andExpect(jsonPath("$.references[0].body").value("Reference Body 1"))
                .andExpect(jsonPath("$.references[1].id").value(2L))
                .andExpect(jsonPath("$.references[1].title").value("Ref Doc 2"))
                .andExpect(jsonPath("$.references[1].body").value("Reference Body 2"));
    }
}
