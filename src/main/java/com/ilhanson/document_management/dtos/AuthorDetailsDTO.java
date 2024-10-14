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
public class AuthorDetailsDTO {
  private Long id;
  private String firstName;
  private String lastName;
  private List<DocumentDTO> documents;
}
