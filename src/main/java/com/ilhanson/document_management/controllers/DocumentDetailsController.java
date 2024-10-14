package com.ilhanson.document_management.controllers;

import com.ilhanson.document_management.dtos.DocumentDetailsDTO;
import com.ilhanson.document_management.dtos.DocumentUpdateDTO;
import com.ilhanson.document_management.exceptions.UnprocessableContentException;
import com.ilhanson.document_management.services.DocumentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/documents/{id}")
@AllArgsConstructor
public class DocumentDetailsController {
  private final DocumentService documentService;

  @GetMapping
  public DocumentDetailsDTO getDocumentDetails(@PathVariable Long id) {
    return documentService.getDocumentDetails(id);
  }

  @PutMapping
  public DocumentDetailsDTO updateDocument(
      @PathVariable Long id, @Valid @RequestBody DocumentUpdateDTO documentUpdateDTO) {
    if (!id.equals(documentUpdateDTO.getId())) {
      throw new UnprocessableContentException();
    }
    return documentService.updateDocument(documentUpdateDTO);
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteDocument(@PathVariable Long id) {
    documentService.deleteDocument(id);
  }
}
