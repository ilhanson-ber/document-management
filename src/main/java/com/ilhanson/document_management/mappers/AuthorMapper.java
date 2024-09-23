package com.ilhanson.document_management.mappers;

import com.ilhanson.document_management.dtos.AuthorCreateDTO;
import com.ilhanson.document_management.dtos.AuthorDTO;
import com.ilhanson.document_management.dtos.AuthorDetailsDTO;
import com.ilhanson.document_management.dtos.AuthorUpdateDTO;
import com.ilhanson.document_management.models.Author;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AuthorMapper {
    private final ModelMapper modelMapper;

    public AuthorDTO mapToDTO(Author author) {
        return modelMapper.map(author, AuthorDTO.class);
    }

    public AuthorDetailsDTO mapToDetailsDTO(Author author) {
        return modelMapper.map(author, AuthorDetailsDTO.class);
    }

    public Author mapToModel(AuthorCreateDTO author) {
        return modelMapper.map(author, Author.class);
    }

    public Author mapToModel(AuthorUpdateDTO author) {
        return modelMapper.map(author, Author.class);
    }
}
