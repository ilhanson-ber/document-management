package com.ilhanson.document_management.services.helpers;

import com.ilhanson.document_management.models.Identifiable;

public class MockEntity implements Identifiable {
    private final Long id;

    public MockEntity(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }
}
