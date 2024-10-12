package com.ilhanson.document_management.services;

import com.ilhanson.document_management.models.Role;
import com.ilhanson.document_management.models.User;
import com.ilhanson.document_management.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void shouldLoadUserByUsernameWhenUserExists() {
        // Given
        User user = User.builder()
                .id(1L)
                .firstName("Samira")
                .lastName("Lang")
                .username("samiral")
                .password("password")
                .role(Role.READER)
                .build();

        // When
        when(userRepository.findByUsername("samiral")).thenReturn(Optional.of(user));

        // Then
        UserDetails userDetails = userDetailsService.loadUserByUsername("samiral");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("samiral");
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo(Role.READER.getAuthority());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        String username = "unknownuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(username);
    }
}
