package com.ilhanson.document_management.models;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    READER,
    EDITOR;

    @Override
    public String getAuthority() {
        return "ROLE_" + name();
    }
}
