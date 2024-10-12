package com.ilhanson.document_management.config;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilityTest {

    private JwtUtility jwtUtility;

    private final String secretKey = "C154617E4A4A2206AAE9F99B85EE57166F19A8ABC32179DBE90803214CBBFDD5";
    private final long jwtExpiration = 1000 * 60 * 60; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtility = new JwtUtility();
        // Use reflection to set @Value fields
        ReflectionTestUtils.setField(jwtUtility, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtUtility, "jwtExpiration", jwtExpiration);
    }

    @Test
    void shouldGenerateValidToken() {
        // Given
        UserDetails userDetails = new User("samiral", "password", new ArrayList<>());

        // When
        String token = jwtUtility.generateToken(userDetails);

        // Then
        assertThat(token).isNotNull();
        String usernameFromToken = jwtUtility.getUsernameFromToken(token);
        assertThat(usernameFromToken).isEqualTo("samiral");
    }

    @Test
    void shouldValidateTokenSuccessfully() {
        // Given
        UserDetails userDetails = new User("samiral", "password", new ArrayList<>());
        String token = jwtUtility.generateToken(userDetails);

        // When
        boolean isValid = jwtUtility.validateToken(token, userDetails);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldFailValidationForExpiredToken() {
        // Given
        ReflectionTestUtils.setField(jwtUtility, "jwtExpiration", -1000L);
        UserDetails userDetails = new User("samiral", "password", new ArrayList<>());
        String expiredToken = jwtUtility.generateToken(userDetails);

        assertThatThrownBy(() -> jwtUtility.validateToken(expiredToken, userDetails))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void shouldExtractUsernameFromToken() {
        // Given
        UserDetails userDetails = new User("samiral", "password", new ArrayList<>());
        String token = jwtUtility.generateToken(userDetails);

        // When
        String username = jwtUtility.getUsernameFromToken(token);

        // Then
        assertThat(username).isEqualTo("samiral");
    }

    @Test
    void shouldFailToValidateTokenWithWrongUserDetails() {
        // Given
        UserDetails userDetails = new User("samiral", "password", new ArrayList<>());
        String token = jwtUtility.generateToken(userDetails);

        // When
        UserDetails wrongUserDetails = new User("kaib", "password", new ArrayList<>());
        boolean isValid = jwtUtility.validateToken(token, wrongUserDetails);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldGetExpirationDateFromToken() {
        // Given
        UserDetails userDetails = new User("samiral", "password", new ArrayList<>());
        String token = jwtUtility.generateToken(userDetails);

        // When
        Date expirationDate = jwtUtility.getExpirationDateFromToken(token);

        // Then
        assertThat(expirationDate).isAfter(new Date());
    }
}