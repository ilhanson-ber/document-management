package com.ilhanson.document_management.mappers;

import com.ilhanson.document_management.dtos.*;
import com.ilhanson.document_management.models.Author;
import com.ilhanson.document_management.models.Document;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AuthorMapperTest {

    private final ModelMapper modelMapper = new ModelMapper();
    private final AuthorMapper authorMapper = new AuthorMapper(modelMapper);

    @Test
    void shouldMapAuthorToAuthorDTO() {
        Author author = new Author(1L, "John", "Doe", null);

        AuthorDTO authorDTO = authorMapper.mapToDTO(author);

        assertThat(authorDTO.getId()).isEqualTo(1L);
        assertThat(authorDTO.getFirstName()).isEqualTo("John");
        assertThat(authorDTO.getLastName()).isEqualTo("Doe");
    }

    @Test
    void shouldMapAuthorToAuthorDetailsDTO() {
        Document document1 = new Document(1L, "Doc 1", "Body 1", null, null, null);
        Document document2 = new Document(2L, "Doc 2", "Body 2", null, null, null);
        Set<Document> documents = Set.of(document1, document2);
        Author author = new Author(1L, "John", "Doe", documents);

        AuthorDetailsDTO authorDetailsDTO = authorMapper.mapToDetailsDTO(author);

        assertThat(authorDetailsDTO.getId()).isEqualTo(1L);
        assertThat(authorDetailsDTO.getFirstName()).isEqualTo("John");
        assertThat(authorDetailsDTO.getLastName()).isEqualTo("Doe");
        assertThat(authorDetailsDTO.getDocuments()).hasSize(2);
        assertThat(authorDetailsDTO.getDocuments()).extracting("id").containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void shouldMapAuthorCreateDTOToAuthorWithEmptyAssociations() {
        AuthorCreateDTO authorCreateDTO = new AuthorCreateDTO(null, "John", "Doe", new ArrayList<>());

        Author author = authorMapper.mapToModel(authorCreateDTO);

        assertThat(author.getDocuments()).hasSize(0);
    }

    @Test
    void shouldMapAuthorCreateDTOToAuthor() {
        List<IdInputDTO> documents = List.of(new IdInputDTO(1L), new IdInputDTO(2L));
        AuthorCreateDTO authorCreateDTO = new AuthorCreateDTO(null, "John", "Doe", documents);

        Author author = authorMapper.mapToModel(authorCreateDTO);

        assertThat(author.getFirstName()).isEqualTo("John");
        assertThat(author.getLastName()).isEqualTo("Doe");
        assertThat(author.getId()).isNull();  // As ID is null in CreateDTO
        assertThat(author.getDocuments()).hasSize(2);
        assertThat(author.getDocuments()).extracting("id").containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void shouldMapAuthorUpdateDTOToAuthor() {
        List<IdInputDTO> documents = List.of(new IdInputDTO(1L), new IdInputDTO(2L));
        AuthorUpdateDTO authorUpdateDTO = new AuthorUpdateDTO(1L, "John", "Doe", documents);

        Author author = authorMapper.mapToModel(authorUpdateDTO);

        assertThat(author.getId()).isEqualTo(1L);
        assertThat(author.getFirstName()).isEqualTo("John");
        assertThat(author.getLastName()).isEqualTo("Doe");
        assertThat(author.getDocuments()).hasSize(2);
        assertThat(author.getDocuments()).extracting("id").containsExactlyInAnyOrder(1L, 2L);
    }
}
