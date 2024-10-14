package com.ilhanson.document_management.dtos;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentDetailsDTO {
  private Long id;
  private String title;
  private String body;
  private List<AuthorDTO> authors;
  private List<DocumentDTO> references;
}
