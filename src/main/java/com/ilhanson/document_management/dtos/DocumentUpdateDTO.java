package com.ilhanson.document_management.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentUpdateDTO {

  @NotNull(message = "ID should be provided")
  @Min(value = 1, message = "ID is not valid")
  private Long id;

  @NotBlank(message = "Title can not be empty")
  @Size(max = 200, message = "Title can not be longer than 200 characters")
  private String title;

  @NotBlank(message = "Body can not be empty")
  private String body;

  @Valid
  @NotNull(message = "List of reference IDs should be provided")
  private List<IdInputDTO> references;

  @Valid private List<IdInputDTO> authors;
}
