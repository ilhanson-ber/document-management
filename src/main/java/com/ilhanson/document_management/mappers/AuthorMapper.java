package com.ilhanson.document_management.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

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

    public String toJson(AuthorDetailsDTO authorDetailsDTO) throws JsonProcessingException {
        return objectMapper.writeValueAsString(authorDetailsDTO);
    }

    public AuthorDetailsDTO toDetailsDTO(String detailsJson) throws JsonProcessingException {
        return objectMapper.readValue(detailsJson, AuthorDetailsDTO.class);
    }
}
