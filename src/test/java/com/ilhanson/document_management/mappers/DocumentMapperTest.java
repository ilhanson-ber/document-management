package com.ilhanson.document_management.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.ilhanson.document_management.dtos.*;
import com.ilhanson.document_management.models.Author;
import com.ilhanson.document_management.models.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

class DocumentMapperTest {

  private final ModelMapper modelMapper = new ModelMapper();
  private final DocumentMapper documentMapper = new DocumentMapper(modelMapper);

  @Test
  void shouldMapDocumentToDocumentDTO() {
    Document document = new Document(1L, "Doc Title", "Doc Body", null, null, null);

    DocumentDTO documentDTO = documentMapper.mapToDTO(document);

    assertThat(documentDTO.getId()).isEqualTo(1L);
    assertThat(documentDTO.getTitle()).isEqualTo("Doc Title");
    assertThat(documentDTO.getBody()).isEqualTo("Doc Body");
  }

  @Test
  void shouldMapDocumentToDocumentDetailsDTO() {
    Author author1 = new Author(1L, "John", "Doe", null);
    Author author2 = new Author(2L, "Jane", "Doe", null);
    Set<Author> authors = Set.of(author1, author2);

    Document reference = new Document(2L, "Ref", "Ref Body", authors, null, null);
    Set<Document> references = Set.of(reference);

    Document document = new Document(1L, "Doc Title", "Doc Body", authors, references, null);

    DocumentDetailsDTO documentDetailsDTO = documentMapper.mapToDetailsDTO(document);

    assertThat(documentDetailsDTO.getId()).isEqualTo(1L);
    assertThat(documentDetailsDTO.getTitle()).isEqualTo("Doc Title");
    assertThat(documentDetailsDTO.getBody()).isEqualTo("Doc Body");
    assertThat(documentDetailsDTO.getAuthors()).hasSize(2);
    assertThat(documentDetailsDTO.getAuthors()).extracting("id").containsExactlyInAnyOrder(1L, 2L);
    assertThat(documentDetailsDTO.getReferences()).hasSize(1);
    assertThat(documentDetailsDTO.getReferences()).extracting("id").containsExactlyInAnyOrder(2L);
  }

  @Test
  void shouldMapDocumentCreateDTOToDocumentWithEmptyAssociations() {
    DocumentCreateDTO documentCreateDTO =
        new DocumentCreateDTO(null, "Doc Title", "Doc Body", new ArrayList<>(), new ArrayList<>());

    Document document = documentMapper.mapToModel(documentCreateDTO);

    assertThat(document.getAuthors()).isEmpty();
    assertThat(document.getReferences()).isEmpty();
  }

  @Test
  void shouldMapDocumentCreateDTOToDocument() {
    List<IdInputDTO> authors = List.of(new IdInputDTO(1L), new IdInputDTO(2L));
    List<IdInputDTO> references = List.of(new IdInputDTO(1L), new IdInputDTO(2L));
    DocumentCreateDTO documentCreateDTO =
        new DocumentCreateDTO(null, "Doc Title", "Doc Body", references, authors);

    Document document = documentMapper.mapToModel(documentCreateDTO);

    assertThat(document.getTitle()).isEqualTo("Doc Title");
    assertThat(document.getBody()).isEqualTo("Doc Body");
    assertThat(document.getId()).isNull();
    assertThat(document.getAuthors()).hasSize(2);
    assertThat(document.getAuthors()).extracting("id").containsExactlyInAnyOrder(1L, 2L);
    assertThat(document.getReferences()).hasSize(2);
    assertThat(document.getReferences()).extracting("id").containsExactlyInAnyOrder(1L, 2L);
  }

  @Test
  void shouldMapDocumentUpdateDTOToDocument() {
    List<IdInputDTO> authors = List.of(new IdInputDTO(1L), new IdInputDTO(2L));
    List<IdInputDTO> references = List.of(new IdInputDTO(1L), new IdInputDTO(2L));
    DocumentUpdateDTO documentUpdateDTO =
        new DocumentUpdateDTO(1L, "Doc Title", "Doc Body", references, authors);

    Document document = documentMapper.mapToModel(documentUpdateDTO);

    assertThat(document.getId()).isEqualTo(1L);
    assertThat(document.getTitle()).isEqualTo("Doc Title");
    assertThat(document.getBody()).isEqualTo("Doc Body");
    assertThat(document.getAuthors()).hasSize(2);
    assertThat(document.getAuthors()).extracting("id").containsExactlyInAnyOrder(1L, 2L);
    assertThat(document.getReferences()).hasSize(2);
    assertThat(document.getReferences()).extracting("id").containsExactlyInAnyOrder(1L, 2L);
  }
}
