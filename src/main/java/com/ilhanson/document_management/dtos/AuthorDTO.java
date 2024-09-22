package com.ilhanson.document_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthorDTO {
    private Long id;
    private String firstName;
    private String lastName;
}