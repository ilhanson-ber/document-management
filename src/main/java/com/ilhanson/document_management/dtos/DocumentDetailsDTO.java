package com.ilhanson.document_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
