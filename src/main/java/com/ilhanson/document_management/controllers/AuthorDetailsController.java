package com.ilhanson.document_management.controllers;

import com.ilhanson.document_management.dtos.AuthorDetailsDTO;
import com.ilhanson.document_management.dtos.AuthorUpdateDTO;
import com.ilhanson.document_management.exceptions.UnprocessableContentException;
import com.ilhanson.document_management.services.AuthorService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/authors/{id}")
@AllArgsConstructor
public class AuthorDetailsController {
    private final AuthorService authorService;

    @GetMapping
    public AuthorDetailsDTO getAuthorDetails(@PathVariable Long id) {
        return authorService.getAuthorDetails(id);
    }

    @PutMapping
    public AuthorDetailsDTO updateAuthor(
            @PathVariable Long id,
            @Valid @RequestBody AuthorUpdateDTO authorUpdateDTO) {
        if (!id.equals(authorUpdateDTO.getId())) {
            throw new UnprocessableContentException("ID in request path and ID in request body should match");
        }
        return authorService.updateAuthor(authorUpdateDTO);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAuthor(@PathVariable Long id) {
        authorService.deleteAuthor(id);
    }
}
