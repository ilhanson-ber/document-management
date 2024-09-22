package com.ilhanson.document_management.dtos;

import java.util.List;

public record AuthorDetailsDTO(Long id, String firstName, String lastName, List<DocumentDTO> documents) {
}
