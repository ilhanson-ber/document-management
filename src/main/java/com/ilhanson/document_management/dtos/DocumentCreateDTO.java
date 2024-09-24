package com.ilhanson.document_management.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentCreateDTO {

    @Null(message = "ID should be null or missing when creating a document")
    private Long id;

    @NotBlank(message = "Title can not be empty")
    @Size(max = 200, message = "Title can not be longer than 200 characters")
    private String title;

    @NotBlank(message = "Body can not be empty")
    private String body;

    @Valid
    private List<IdInputDTO> references = new ArrayList<>();

    @Valid
    private List<IdInputDTO> authors = new ArrayList<>();
}
