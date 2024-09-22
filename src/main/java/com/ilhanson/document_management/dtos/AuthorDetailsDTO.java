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
public class AuthorDetailsDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private List<DocumentDTO> documents;
}
