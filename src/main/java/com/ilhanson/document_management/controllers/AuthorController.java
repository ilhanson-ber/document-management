package com.ilhanson.document_management.controllers;

import com.ilhanson.document_management.dtos.AuthorCreateDTO;
import com.ilhanson.document_management.dtos.AuthorDTO;
import com.ilhanson.document_management.dtos.AuthorDetailsDTO;
import com.ilhanson.document_management.services.AuthorService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/authors")
@AllArgsConstructor
public class AuthorController {
  private final AuthorService authorService;

  @GetMapping
  public List<AuthorDTO> getAllAuthors() {
    return authorService.getAllAuthors();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AuthorDetailsDTO createAuthor(@Valid @RequestBody AuthorCreateDTO authorCreateDTO) {
    return authorService.createAuthor(authorCreateDTO);
  }
}
