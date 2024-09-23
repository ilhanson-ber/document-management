package com.ilhanson.document_management.mappers;

import com.ilhanson.document_management.dtos.DocumentCreateDTO;
import com.ilhanson.document_management.dtos.DocumentDTO;
import com.ilhanson.document_management.dtos.DocumentDetailsDTO;
import com.ilhanson.document_management.dtos.DocumentUpdateDTO;
import com.ilhanson.document_management.models.Document;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DocumentMapper {
    private final ModelMapper modelMapper;

    public DocumentDTO mapToDTO(Document document) {
        return modelMapper.map(document, DocumentDTO.class);
    }

    public DocumentDetailsDTO mapToDetailsDTO(Document document) {
        return modelMapper.map(document, DocumentDetailsDTO.class);
    }

    public Document mapToModel(DocumentCreateDTO document) {
        return modelMapper.map(document, Document.class);
    }

    public Document mapToModel(DocumentUpdateDTO document) {
        return modelMapper.map(document, Document.class);
    }
}
