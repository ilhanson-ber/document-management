package com.ilhanson.document_management.mappers;

import com.ilhanson.document_management.dtos.SignupDTO;
import com.ilhanson.document_management.models.User;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserMapper {
  private final ModelMapper modelMapper;

  public User mapToModel(SignupDTO user) {
    return modelMapper.map(user, User.class);
  }
}
