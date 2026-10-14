package com.controllerassist.model;

public enum PunchType {
    CHECK_IN("Entrada"),
    CHECK_OUT("Salida");

    private final String description;

    PunchType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
