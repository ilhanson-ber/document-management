package com.ilhanson.document_management.controllers;

import com.ilhanson.document_management.dtos.DocumentCreateDTO;
import com.ilhanson.document_management.dtos.DocumentDTO;
import com.ilhanson.document_management.dtos.DocumentDetailsDTO;
import com.ilhanson.document_management.services.DocumentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/documents")
@AllArgsConstructor
public class DocumentController {
    private final DocumentService documentService;

    @GetMapping
    public List<DocumentDTO> getAllDocuments() {
        return documentService.getAllDocuments();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentDetailsDTO createDocument(@Valid @RequestBody DocumentCreateDTO documentCreateDTO) {
        return documentService.createDocument(documentCreateDTO);
    }
}
