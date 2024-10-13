package com.ilhanson.document_management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilhanson.document_management.config.security.JwtAuthenticationFilter;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = AuthorController.class,
        excludeFilters =
        @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class))
class AuthorControllerTest {

    @MockBean
    private AuthorService authorService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void shouldGetAllAuthors() throws Exception {
        // Arrange
        AuthorDTO authorDTO = new AuthorDTO(1L, "John", "Doe");
        when(authorService.getAllAuthors()).thenReturn(List.of(authorDTO));

        // Act & Assert
        mockMvc.perform(get("/api/v1/authors").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"));
    }

    @Test
    @WithMockUser
    void shouldCreateAuthor() throws Exception {
        // Arrange
        AuthorCreateDTO authorCreateDTO = new AuthorCreateDTO(null, "John", "Doe", null);
        AuthorDetailsDTO authorDetailsDTO = new AuthorDetailsDTO(1L, "John", "Doe", null);

        when(authorService.createAuthor(any(AuthorCreateDTO.class))).thenReturn(authorDetailsDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/authors")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    @WithMockUser
    void shouldCreateAuthorWithDocuments() throws Exception {
        // Arrange
        IdInputDTO document1 = new IdInputDTO(1L);
        IdInputDTO document2 = new IdInputDTO(2L);
        AuthorCreateDTO authorCreateDTO = new AuthorCreateDTO(null, "John", "Doe", List.of(document1, document2));

        DocumentDTO docDTO1 = new DocumentDTO(1L, "Doc 1", "Content 1");
        DocumentDTO docDTO2 = new DocumentDTO(2L, "Doc 2", "Content 2");
        List<DocumentDTO> documentDTOs = List.of(docDTO1, docDTO2);

        AuthorDetailsDTO authorDetailsDTO = new AuthorDetailsDTO(1L, "John", "Doe", documentDTOs);

        when(authorService.createAuthor(any(AuthorCreateDTO.class))).thenReturn(authorDetailsDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/authors")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorCreateDTO)))
                .andExpect(status().isCreated())
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
    void shouldReturnBadRequestForInvalidAuthorCreateDTO() throws Exception {
        // Arrange
        AuthorCreateDTO invalidAuthorCreateDTO = new AuthorCreateDTO(null, "", "", null); // Invalid data

        // Act & Assert
        mockMvc.perform(post("/api/v1/authors")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAuthorCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Validation failed for the request body. See errorDetails for more information."))
                .andExpect(jsonPath("$.status").value("Bad Request"))
                .andExpect(jsonPath("$.errorDetails.firstName").value("First name can not be empty"))
                .andExpect(jsonPath("$.errorDetails.lastName").value("Last name can not be empty"));
    }

    @Test
    @WithMockUser
    void shouldHandleResourceNotFoundException() throws Exception {
        // Arrange
        when(authorService.createAuthor(any(AuthorCreateDTO.class))).thenThrow(new ResourceNotFoundException("Author", 1L));

        AuthorCreateDTO authorCreateDTO = new AuthorCreateDTO(null, "John", "Doe", null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/authors")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorCreateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Author with ID 1 does not exist"))
                .andExpect(jsonPath("$.status").value("Not Found"))
                .andExpect(jsonPath("$.statusCode").value(404));
    }
}
