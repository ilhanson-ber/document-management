package com.ilhanson.document_management.dtos;

import java.util.List;

public record DocumentDetailsDTO(Long id, String title, String body, List<AuthorDTO> authors,
                                 List<DocumentDTO> references) {
}
