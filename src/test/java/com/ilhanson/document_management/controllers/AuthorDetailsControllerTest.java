package com.ilhanson.document_management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilhanson.document_management.config.JwtAuthenticationFilter;
import com.ilhanson.document_management.dtos.*;
import com.ilhanson.document_management.exceptions.ResourceNotFoundException;
import com.ilhanson.document_management.services.AuthorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = AuthorDetailsController.class,
        excludeFilters =
        @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class))
class AuthorDetailsControllerTest {

    @MockBean
    private AuthorService authorService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void shouldGetAuthorDetailsWithDocuments() throws Exception {
        // Arrange
        DocumentDTO doc1 = new DocumentDTO(1L, "Doc 1", "Content 1");
        DocumentDTO doc2 = new DocumentDTO(2L, "Doc 2", "Content 2");
        List<DocumentDTO> documents = List.of(doc1, doc2);

        AuthorDetailsDTO authorDetailsDTO = new AuthorDetailsDTO(1L, "John", "Doe", documents);
        when(authorService.getAuthorDetails(1L)).thenReturn(authorDetailsDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/authors/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.documents[0].id").value(1L))
                .andExpect(jsonPath("$.documents[0].title").value("Doc 1"))
                .andExpect(jsonPath("$.documents[0].body").value("Content 1"))
                .andExpect(jsonPath("$.documents[1].id").value(2L))
                .andExpect(jsonPath("$.documents[1].title").value("Doc 2"))
                .andExpect(jsonPath("$.documents[1].body").value("Content 2"));
    }

    @Test
    @WithMockUser
    void shouldUpdateAuthorWithDocuments() throws Exception {
        // Arrange
        DocumentDTO doc1 = new DocumentDTO(1L, "Doc 1", "Content 1");
        DocumentDTO doc2 = new DocumentDTO(2L, "Doc 2", "Content 2");
        List<DocumentDTO> documents = List.of(doc1, doc2);
        IdInputDTO id1 = new IdInputDTO(1L);
        IdInputDTO id2 = new IdInputDTO(2L);
        List<IdInputDTO> ids = List.of(id1, id2);


        AuthorUpdateDTO authorUpdateDTO = new AuthorUpdateDTO(1L, "John", "Doe", ids);
        AuthorDetailsDTO authorDetailsDTO = new AuthorDetailsDTO(1L, "John", "Doe", documents);

        when(authorService.updateAuthor(any(AuthorUpdateDTO.class))).thenReturn(authorDetailsDTO);

        // Act & Assert
        mockMvc.perform(put("/api/v1/authors/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.documents[0].id").value(1L))
                .andExpect(jsonPath("$.documents[0].title").value("Doc 1"))
                .andExpect(jsonPath("$.documents[0].body").value("Content 1"))
                .andExpect(jsonPath("$.documents[1].id").value(2L))
                .andExpect(jsonPath("$.documents[1].title").value("Doc 2"))
                .andExpect(jsonPath("$.documents[1].body").value("Content 2"));
    }

    @Test
    @WithMockUser
    void shouldReturnUnprocessableEntityWhenPathAndBodyIdMismatch() throws Exception {
        // Arrange
        AuthorUpdateDTO authorUpdateDTO = new AuthorUpdateDTO(2L, "John", "Doe", new ArrayList<>()); // ID mismatch

        // Act & Assert
        mockMvc.perform(put("/api/v1/authors/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorUpdateDTO)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("ID in request path and ID in request body should match"))
                .andExpect(jsonPath("$.status").value("Unprocessable Entity"))
                .andExpect(jsonPath("$.statusCode").value(422));
    }

    @Test
    @WithMockUser
    void shouldDeleteAuthor() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/authors/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestForInvalidAuthorUpdateDTO() throws Exception {
        // Arrange
        AuthorCreateDTO invalidAuthorUpdateDTO = new AuthorCreateDTO(2L, "John", "Doe", null); // Invalid data

        // Act & Assert
        mockMvc.perform(put("/api/v1/authors/2")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAuthorUpdateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Validation failed for the request body. See errorDetails for more information."))
                .andExpect(jsonPath("$.status").value("Bad Request"))
                .andExpect(jsonPath("$.errorDetails.documents").value("List of document IDs should be provided"));
    }

    @Test
    @WithMockUser
    void shouldHandleResourceNotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Author", 1L)).when(authorService).deleteAuthor(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/authors/1").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Author with ID 1 does not exist"))
                .andExpect(jsonPath("$.status").value("Not Found"))
                .andExpect(jsonPath("$.statusCode").value(404));
    }
}
