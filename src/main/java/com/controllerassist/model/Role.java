package com.controllerassist.model;

public enum Role {
    ADMIN("Administrador"),
    EMPLEADO("Empleado");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
