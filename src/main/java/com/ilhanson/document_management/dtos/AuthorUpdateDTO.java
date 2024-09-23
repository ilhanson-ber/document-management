package com.ilhanson.document_management.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthorUpdateDTO {

    @NotNull(message = "ID should be provided")
    @Min(value = 1, message = "ID is not valid")
    private Long id;

    @NotBlank(message = "First name can not be empty")
    @Size(max = 100, message = "First name can not be longer than 100 characters")
    private String firstName;

    @NotBlank(message = "Last name can not be empty")
    @Size(max = 100, message = "Last name can not be longer than 100 characters")
    private String lastName;

    @Valid
    @NotNull(message = "List of document IDs should be provided")
    private List<IdInputDTO> documents;
}
