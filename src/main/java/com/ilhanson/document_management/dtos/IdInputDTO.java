package com.ilhanson.document_management.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// Used to represent a referenced class for example
// in a request body of a POST or UPDATE request
// to associate new entities with the owner entity
public class IdInputDTO {
  @NotNull private Long id;
}
