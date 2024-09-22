package com.ilhanson.document_management.mappers;

import com.ilhanson.document_management.dtos.AuthorDTO;
import com.ilhanson.document_management.dtos.AuthorDetailsDTO;
import com.ilhanson.document_management.models.Author;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AuthorMapper {
    private final ModelMapper modelMapper;

    public AuthorDTO toDTO(Author author) {
        return modelMapper.map(author, AuthorDTO.class);
    }

    public AuthorDetailsDTO toDetailsDTO(Author author) {
        return modelMapper.map(author, AuthorDetailsDTO.class);
    }

    public Author toModel(AuthorDTO author) {
        return modelMapper.map(author, Author.class);
    }

    public Author toModel(AuthorDetailsDTO author) {
        return modelMapper.map(author, Author.class);
    }
}
